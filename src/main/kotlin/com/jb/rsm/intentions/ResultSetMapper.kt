package com.jb.rsm.intentions

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaResolveResult
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiType
import com.intellij.util.IncorrectOperationException
import com.jb.rsm.intentions.codegen.CodeGenTask
import com.jb.rsm.intentions.codegen.GenerateFromRowMethod
import com.jb.rsm.intentions.codegen.GenerateHelperClass
import com.jb.rsm.intentions.codegen.GenerateMapperLambda
import com.jb.rsm.intentions.codegen.OverwriteStrategyException
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.FROMROW_MAPPER_TEMPLATE
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSET_MAPPER_TEMPLATE
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSET_WRAPPER_TEMPLATE
import com.jb.rsm.util.Messages.message
import com.jb.rsm.util.mapParentOfType
import com.jb.rsm.util.mapResolveMethod
import com.jb.rsm.util.methodName
import com.jb.rsm.util.notifyWarning
import com.jb.rsm.util.parameterIndexOf
import com.jb.rsm.util.parentOfType
import com.jb.rsm.util.resolveMethod
import com.jb.rsm.util.returnType


private val jdbcMapperInterfaces = setOf(
    "org.springframework.jdbc.core.RowMapper",
    "org.springframework.jdbc.core.ResultSetExtractor"
)

/**
 * An IntentionAction that generates a `ResultSetMapper` for a JDBC query method call.
 *
 * This intention is only available when the caret is inside a JDBC query method call. It will generate a
 * lambda for the query method call, add a `fromRow` method to the target record type class, and add a helper
 * java class named ResultSetWrapper to the project.
 */
class ResultSetMapper : BaseElementAtCaretIntentionAction() {
    private val log = logger<ResultSetMapper>()

    override fun getText(): String {
        return familyName
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return message("intention.resultSetMapper.familyName")
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val inQueryCall = isInJdbcQueryMethodCall(element)
        log.debug("$text isAvailable: $inQueryCall")
        return inQueryCall
    }


    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val context = getJdbcQueryCallContext(element) ?: return

        if (IntentionPreviewUtils.isPreviewElement(element)) {
            log.debug("Generating preview lambda")

            // In preview mode, we can't modify anything outside the current file, so we only show changes to the
            // lambda
            GenerateMapperLambda(
                project,
                element,
                context
            ).run()
        } else {
            val tasks = createCodeGenTasks(project, editor, element, context)

            tasks.forEach { task ->
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

    private fun createCodeGenTasks(
        project: Project,
        @Suppress("UNUSED_PARAMETER") editor: Editor,
        element: PsiElement,
        context: QueryCallContext
    ): List<CodeGenTask> {
        val tasks = mutableListOf<CodeGenTask>()
        tasks.add(GenerateHelperClass(project, element, FROMROW_MAPPER_TEMPLATE))
        tasks.add(GenerateHelperClass(project, element, RESULTSET_MAPPER_TEMPLATE))
        tasks.add(GenerateHelperClass(project, element, RESULTSET_WRAPPER_TEMPLATE))
        if (context.recordType != null) {
            tasks.add(GenerateFromRowMethod(project, element, context.recordType!!))
        }
        tasks.add(GenerateMapperLambda(project, element, context))
        return tasks

    }

    // Quick check to see if the element is inside a JdbcTemplate like query method call
    private fun isInJdbcQueryMethodCall(element: PsiElement): Boolean {
        return element.parentOfType(PsiMethodCallExpression::class.java) { callExpression ->
            val result = callExpression.resolveMethod { method, _ ->
                // Allow any method starting with the name "query" that also has a supported mapper lambda parameter
                method.name.startsWith("query") && method.parameterIndexOf(jdbcMapperInterfaces) != -1
            }

            result.isAccessible
        } != null
    }

    /**
     * Finds the context of a JDBC query method call.
     *
     * @param element The element to find the context for.
     * @return The context of the JDBC query method call, or null if no context is found.
     */
    private fun getJdbcQueryCallContext(element: PsiElement): QueryCallContext? {
        return element.mapParentOfType(PsiMethodCallExpression::class.java) { methodCallExpression ->
            methodCallExpression.mapResolveMethod { method, resolved ->
                if (method.name.startsWith("query")
                    && method.parameterIndexOf(jdbcMapperInterfaces) != -1
                    && methodCallExpression.returnType != null
                ) {
                    return@mapResolveMethod QueryCallContext(methodCallExpression, method, resolved)
                }

                null
            }
        }
    }


    @Suppress("MemberVisibilityCanBePrivate")
    data class QueryCallContext(
        val methodCallExpression: PsiMethodCallExpression,
        val method: PsiMethod,
        val resolved: JavaResolveResult
    ) {
        val returnType: PsiType by lazy {
            methodCallExpression.returnType!!
        }

        val methodName: String = methodCallExpression.methodName!!

        val isQueryObject: Boolean = methodName == "queryForObject"
        val isQueryArray: Boolean = methodName == "query" && returnType is PsiArrayType

        val recordType: PsiClassType?
            get() {
                if (returnType is PsiArrayType) {
                    return (returnType as PsiArrayType).componentType as PsiClassType
                }

                if (isQueryObject) {
                    return returnType as PsiClassType
                }

                return (returnType as PsiClassType).parameters.firstOrNull() as? PsiClassType
            }


        val lambdaParamIndex: Int = method.parameterIndexOf(jdbcMapperInterfaces)
    }
}
