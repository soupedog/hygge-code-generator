package hygge.plugin.generator.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.Notifications
import com.intellij.openapi.ui.MessageType

/**
 * 气泡提示小工具 , groupId 需要在 hygge.plugin.xml 中注册
 */
object NotificationsUtil {

    private fun notify(content: String, type: MessageType, groupId: String = "Global_NotificationsUtil") {
        val groupManager: NotificationGroupManager = NotificationGroupManager.getInstance()

        val notification: Notification = groupManager.getNotificationGroup(groupId).createNotification(content, type)

        Notifications.Bus.notify(notification)
    }

    fun info(content: String) {
        notify(content, MessageType.INFO)
    }

    fun warn(content: String) {
        notify(content, MessageType.WARNING)
    }

    fun error(content: String) {
        notify(content, MessageType.ERROR)
    }
}