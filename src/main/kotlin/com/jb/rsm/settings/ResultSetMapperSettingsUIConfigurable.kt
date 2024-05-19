package com.jb.rsm.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.jb.rsm.util.Messages.message

class ResultSetMapperSettingsUIConfigurable(private val project: Project) :
    BoundSearchableConfigurable(message("settings.configurable"), "preferences.tools.ResultSetMapperPlugin"),
    Configurable.VariableProjectAppLevel {
    override fun createPanel(): DialogPanel {
        return ResultSetMapperSettingsUIComponent(project).panel
    }

    override fun apply() {
        super.apply()

        project.save()

        ApplicationManager.getApplication().saveSettings()
    }

    override fun isProjectLevel(): Boolean {
        return true
    }
}
