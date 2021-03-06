package me.fornever.avaloniarider.idea

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.jetbrains.rd.platform.util.application
import com.jetbrains.rd.platform.util.getComponent

class AvaloniaRiderNotifications {
    companion object {
        fun getInstance(): AvaloniaRiderNotifications = application.getComponent()
    }

    private val notificationGroup = NotificationGroup(
        "me.fornever.avaloniarider",
        NotificationDisplayType.BALLOON,
        true
    )

    fun showNotification(text: String) {
        val notification = notificationGroup.createNotification(text, NotificationType.ERROR)
        Notifications.Bus.notify(notification)
    }
}
