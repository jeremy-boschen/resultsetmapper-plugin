@file:Suppress("MemberVisibilityCanBePrivate")

package com.jb.rsm.intentions.codegen

import kotlin.math.max
import com.intellij.codeInsight.AnnotationUtil
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.refactoring.suggested.startOffset
import com.jb.rsm.settings.OverwriteStrategy
import com.jb.rsm.settings.getResultMapperSettings
import com.jb.rsm.util.Messages.message
import com.jb.rsm.util.matches

/**
 * Exception throw when an operation's OverwriteStrategy prevents it from being overwritten
 */
class OverwriteStrategyException(val title: String, message: String, val showSettings: Boolean = true) :
    Exception(message)

data class OverwriteStrategySetting(val template: String, val strategy: OverwriteStrategy) {

    fun blocked(type: String, resource: String): OverwriteStrategyException {
        return OverwriteStrategyException(
            message("notification.overwriteBlocked.title"),
            message(
                "notification.overwriteBlocked.message",
                message("notification.overwriteBlocked.type.${type}"),
                resource,
                template
            ),
            true
        )
    }
}


/**
 * Base class for code generation tasks
 */
abstract class CodeGenTask(
    /**
     * The project where the code generation task will be performed.
     */
    protected val project: Project,

    /**
     * The PsiElement that will be used as the starting point for the code generation task.
     */
    protected val element: PsiElement
) :
    Runnable {

    abstract val name: String

    /**
     * Plugin settings for the project
     */
    protected val settings = getResultMapperSettings(project).state

    /**
     * The logger instance for this class.
     */
    protected val log = logger<CodeGenTask>()

    /**
     * The PsiJavaFile instance that contains the PsiElement used as the starting point for the code generation task.
     */
    protected val psiFile: PsiJavaFile = element.containingFile as PsiJavaFile

    /**
     * The JavaPsiFacade instance that provides access to Java-specific functionality.
     */
    protected val javaFacade: JavaPsiFacade by lazy {
        JavaPsiFacade.getInstance(project)
    }

    /**
     * The PsiParserFacade instance that provides access to parsing functionality.
     */
    protected val parserFacade: PsiParserFacade by lazy {
        PsiParserFacade.getInstance(project)
    }

    /**
     * The PsiElementFactory instance that provides access to creating PsiElements.
     */
    protected val elementFactory: PsiElementFactory by lazy {
        JavaPsiFacade.getElementFactory(project)
    }

    /**
     * The VirtualFile instance that represents the source root for the project.
     */
    protected val sourceRoot: VirtualFile by lazy {
        ProjectFileIndex.getInstance(project).getSourceRootForFile(psiFile.virtualFile)!!
    }

    protected open val generatorName: String
        get() = "ResultSetMapperIntention"

    /**
     * Adds or replaces a file in a project's source root.
     *
     * @param qualifiedName The fully qualified name of the class to add.
     * @param content The content of the file to add.
     * @return The VirtualFile instance that represents the added or replaced file.
     */
    protected fun addProjectClassFile(
        qualifiedName: String,
        content: String,
        overwrite: OverwriteStrategySetting
    ): PsiJavaFile {
        val filePath = qualifiedName.replace('.', '/') + ".java"

        val existingFile = sourceRoot.findFileByRelativePath(filePath)
        if (existingFile != null) {
            when (overwrite.strategy) {
                OverwriteStrategy.Never -> {
                    throw overwrite.blocked("class", qualifiedName)
                }

                OverwriteStrategy.OnlyIfGenerated -> {
                    if (existingFile is PsiJavaFile) {
                        if (existingFile.classes.firstOrNull()?.let { wasTaskGenerated(it) } != true) {
                            throw overwrite.blocked("class", qualifiedName)
                        }
                    }
                }

                else -> {
                    // Allow the operation to continue
                }
            }
        }

        val fileDir = filePath.substringBeforeLast('/')
        val fileName = filePath.substringAfterLast('/')

        val javaFile = PsiFileFactory.getInstance(project).createFileFromText(
            fileName, JavaFileType.INSTANCE, content
        ) as PsiJavaFile

        // Ensure the directory exists for the new class by creating it if necessary
        var directory = PsiManager.getInstance(project).findDirectory(sourceRoot)!!
        fileDir.split("/").map { dirName ->
            directory = (directory.findSubdirectory(dirName) ?: directory.createSubdirectory(dirName))
        }

        directory.findFile(fileName)?.delete()
        directory.add(cleanup(javaFile))

        return javaFile
    }

    /**
     * Creates a commented copy of the given method in the method's parent class.
     *
     * @param method The method to create a commented copy of.
     */
    protected fun createCommentedCopy(method: PsiMethod) {
        // method.text will trim the indent from the start of the method in the document, so we need to determine
        // what that indent is first.
        val document = PsiDocumentManager.getInstance(project).getDocument(method.containingFile) ?: return

        // Determine the indent of the comments
        val indent =
            max(method.startOffset - document.getLineStartOffset(document.getLineNumber(method.startOffset)), 0)

        val lines = (" ".repeat(indent) + method.text).trimIndent().lines()

        val parent = method.containingClass!!
        var anchor: PsiElement = method
        for (line in lines) {
            val commentedLine = "//" + " ".repeat(max(indent - 2, 0)) + line
            anchor = parent.addAfter(elementFactory.createCommentFromText(commentedLine, null), anchor)
        }

        // Put some space between the method and the comments
        parent.addAfter(createWhiteSpace("\n\n"), method)
    }

    /**
     * Adds or replaces a method in the given class.
     *
     * @param clazz The class where the method will be added or replaced.
     * @param method The method to add or replace.
     * @param overwrite The strategy to use when the method already exists
     */
    protected fun addOrReplaceMethod(clazz: PsiClass, method: PsiMethod, overwrite: OverwriteStrategySetting): PsiMethod {
        val existingMethod = clazz.findMethodBySignature(method, false)
        if (null != existingMethod) {
            when (overwrite.strategy) {
                OverwriteStrategy.Never -> {
                    throw overwrite.blocked("method", method.name)
                }

                OverwriteStrategy.OnlyIfGenerated -> {
                    if (!wasTaskGenerated(existingMethod)) {
                        throw overwrite.blocked("method", method.name)
                    }
                }

                else -> {
                    // Allow the operation to continue
                }
            }
        }

        val newMethod = format(method)

        if (existingMethod != null) {
            if (existingMethod.matches(newMethod)) {
                log.debug("Method already exists with matching signature and implementation, skipping replacement: ${existingMethod.name}")

                // Nothing to change
                return existingMethod
            }

            if (overwrite.strategy == OverwriteStrategy.CommentOut) {
                createCommentedCopy(existingMethod)
            }

            return cleanup(existingMethod.replace(newMethod) as PsiMethod)
        } else {
            log.debug("No existing method found, adding new method: ${method.name}")

            return cleanup(clazz.add(newMethod) as PsiMethod)
        }
    }

    protected val j2eNamespace: String =
        if (javaFacade.findPackage("jakarta.annotation") != null) "jakarta" else "javax"

    protected fun wasTaskGenerated(element: PsiElement): Boolean {
        val generatedBy = (element as? PsiModifierListOwner)?.annotations?.find {
            val annotationName = it.qualifiedName
            annotationName in setOf(
                "javax.annotation.Generated",
                "jakarta.annotation.Generated"
            )
        }

        if (generatedBy != null) {
            val value = AnnotationUtil.getDeclaredStringAttributeValue(generatedBy, null)
            return value == generatorName
        }

        return false
    }

    /**
     * Creates a white space PsiElement with the given text.
     *
     * @param text The text to use for the white space PsiElement.
     * @return The created white space PsiElement.
     */
    protected fun createWhiteSpace(text: String = "\n"): PsiElement {
        return parserFacade.createWhiteSpaceFromText(text)
    }

    protected fun <T: PsiElement> cleanup(element: T): T {
        return shortenClassReferences(format(element))
    }

    protected fun <T : PsiElement> shortenClassReferences(element: T): T {
        @Suppress("UNCHECKED_CAST")
        return JavaCodeStyleManager.getInstance(project).shortenClassReferences(element) as T
    }

    protected fun <T : PsiElement> format(element: T): T {
        @Suppress("UNCHECKED_CAST")
        return CodeStyleManager.getInstance(project).reformat(element) as T
    }

    protected fun generateJ2eeTemplate(name: String, properties: Map<String, Any?>): String {
        return generateTemplate("j2ee", name, properties)
    }

    protected fun generateCodeTemplate(name: String, properties: Map<String, Any?>): String {
        return generateTemplate("code", name, properties)
    }

    private fun generateTemplate(group: String, name: String, properties: Map<String, Any?>): String {
        val templateManager = FileTemplateManager.getInstance(project)

        val template = when (group) {
            "j2ee" -> templateManager.getJ2eeTemplate(name)
            "code" -> templateManager.getCodeTemplate(name)
            else -> throw IllegalArgumentException("Unknown group: $group")
        }

        val attributes = mutableMapOf<String, Any?>()

        // Add default CodeGenTask properties
        templateManager.defaultProperties.forEach { key, value ->
            attributes[key.toString()] = value
        }
        attributes["packageName"] = settings.packageName
        attributes["generatorName"] = generatorName
        attributes["j2eNamespace"] = j2eNamespace
        // Copy all the properties to the attributes
        attributes.putAll(properties)

        return template.getText(attributes)
    }
}