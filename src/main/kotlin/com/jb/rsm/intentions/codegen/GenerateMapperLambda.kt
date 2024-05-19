package com.jb.rsm.intentions.codegen

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jb.rsm.intentions.ResultSetMapper
import com.jb.rsm.intentions.codegen.ResultSetMapperTemplateGroup.Companion.RESULTSETMAPPER_LAMBDA_ARGUMENT
import com.jb.rsm.util.Messages.message

class GenerateMapperLambda(
    project: Project,
    element: PsiElement,
    private val context: ResultSetMapper.QueryCallContext
) : CodeGenTask(project, element) {

    private val mapperType: String = when (context.isQueryObject) {
        true -> "FromRowMapper"
        false -> "ResultSetMapper"
    }

    override val name: String
        get() {
            return message("codegen.mapperLambda.title", mapperType)
        }

    private fun createTemplateProperties(): Map<String, Any?> {
        return mapOf(
            "className" to context.recordType?.className,
            "isQueryObject" to context.isQueryObject,
            "isQueryArray" to context.isQueryArray,
            "mapper" to mapperType
        )
    }

    override fun run() {
        val methodText = generateCodeTemplate(RESULTSETMAPPER_LAMBDA_ARGUMENT, createTemplateProperties())

        try {
            val lambda = shortenClassReferences(elementFactory.createExpressionFromText(methodText, element))

            val argumentList = context.methodCallExpression.argumentList
            if (argumentList.expressions.size > context.lambdaParamIndex) {
                argumentList.expressions[context.lambdaParamIndex].replace(lambda)
            } else {
                argumentList.add(lambda)
            }
        }
        catch(e: Exception) {
            log.error("Failed to generate lambda from text: $methodText", e)

            throw e
        }
    }
}