package com.jb.rsm.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.jb.rsm.settings.ResultSetMapperSettingsUIConfigurable

fun notifyWarning(project: Project, title: String, content: String, showSettings: Boolean) {
    notify(project, title, content, NotificationType.WARNING, showSettings)
}

fun notify(project: Project, title: String, content: String, type: NotificationType, showSettings: Boolean) {
    var notification = NotificationGroupManager.getInstance()
        .getNotificationGroup("ResultSetMapper Notification Group")
        .createNotification(title, content, type)

    if (showSettings) {
        notification = notification.addAction(object: NotificationAction("Open settings") {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, ResultSetMapperSettingsUIConfigurable::class.java)
                    notification.expire()
                }

            })
    }

    notification.notify(project)
}

