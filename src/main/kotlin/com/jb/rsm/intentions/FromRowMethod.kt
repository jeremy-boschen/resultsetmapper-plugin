package com.jb.rsm.intentions

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.IncorrectOperationException
import com.jb.rsm.intentions.codegen.GenerateFromRowMethod
import com.jb.rsm.intentions.codegen.GenerateHelperClass
import com.jb.rsm.intentions.codegen.OverwriteStrategyException
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.FROMROW_MAPPER_TEMPLATE
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSET_WRAPPER_TEMPLATE
import com.jb.rsm.settings.getResultMapperSettings
import com.jb.rsm.util.Messages.message
import com.jb.rsm.util.notifyWarning


/**
 * An IntentionAction that generates a `fromRow` method used by a `ResultSetMapper` to construct a record instance.
 */
class FromRowMethod : BaseElementAtCaretIntentionAction() {


    override fun getText(): String {
        return familyName
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return message("intention.fromRowMethod.familyName")
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        // If the element is inside or is a class within the source root, then it is available
        if (element.containingFile.virtualFile == null) {
            return false
        }

        if (!isInSupportedClass(project, element)) {
            return false
        }

        return ProjectFileIndex.getInstance(project).isInSourceContent(element.containingFile.virtualFile)
    }

    private fun isInSupportedClass(project: Project, element: PsiElement): Boolean {
        val clazz = getParentClass(element)
        if (null != clazz && null != clazz.name) {
            var pattern = getResultMapperSettings(project).state.fromRowClassFilter
            if (!pattern.contains(".")) {
                pattern = pattern.replace("*", ".*?")
            }

            return Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(clazz.name!!)
        }

        return true
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val clazz = getParentClass(element) ?: return

        if (IntentionPreviewUtils.isPreviewElement(element)) {
            GenerateFromRowMethod(project, element, PsiTypesUtil.getClassType(clazz)).run()
        } else {
            val tasks = listOf(
                GenerateHelperClass(project, element, FROMROW_MAPPER_TEMPLATE),
                GenerateHelperClass(project, element, RESULTSET_WRAPPER_TEMPLATE),
                GenerateFromRowMethod(project, element, PsiTypesUtil.getClassType(clazz)),
            )
            for (task in tasks) {
                WriteCommandAction.writeCommandAction(project)
                    .withName(task.name)
                    .run<IncorrectOperationException> {
                        try {
                            task.run()
                        } catch (e: OverwriteStrategyException) {
                            notifyWarning(project, e.title, e.message!!, e.showSettings)
                        }
                    }
            }
        }
    }

    private fun getParentClass(element: PsiElement): PsiClass? {
        return PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
    }
}