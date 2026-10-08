package com.example.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "craftmap_realms_invites"
    const val CHANNEL_NAME = "Invitaciones a Reinos Minecraft"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de invitaciones a servidores y reinos de amigos"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showRealmInviteNotification(
        context: Context,
        senderGamertag: String,
        targetGamertag: String,
        realmName: String,
        realmCode: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_REALM_CODE", realmCode)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            realmCode.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚔️ ¡Invitación a Reino de Minecraft!")
            .setContentText("@$senderGamertag invitó a @$targetGamertag al Reino '$realmName' (Código: $realmCode)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🎮 @$senderGamertag te ha enviado una invitación para unirte al Reino de Minecraft '$realmName'.\n\nCódigo de acceso rápido: $realmCode\n¡Toca aquí para conectarte al servidor y ver el World Spawn con tus amigos!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted on Android 13+
        }
    }
}
