package com.jb.rsm.intentions.codegen

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jb.rsm.settings.OverwriteStrategy
import com.jb.rsm.util.Messages.message

class GenerateHelperClass(
    project: Project,
    element: PsiElement,
    private val template: String,
) : CodeGenTask(project, element) {
    private val className: String = template.removeSuffix(".java")

    override val name: String
        get() {
            return message("codegen.helperClass.title","${settings.packageName}.${className}")
        }


    override fun run() {
        val templateSettings = settings.getTemplateNamed(template)
        if (templateSettings?.generate == false) {
            log.info("Skipping generate task due to $template settings: generate=false")

            return
        }

        val content = generateJ2eeTemplate(
            template, mapOf(
                "packageName" to settings.packageName,
            )
        )

        addProjectClassFile(
            "${settings.packageName}.${className}",
            content,
            OverwriteStrategySetting(template, templateSettings?.overwriteStrategy ?: OverwriteStrategy.Always)
        )
    }
}