package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val NOTIFICATION_ID = 0
    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            val optionsToDownload = findViewById<RadioGroup>(R.id.files_to_download)
            if (isRadioGroupOptionSelected(optionsToDownload)) {
                download(optionsToDownload)
            } else {
                Toast.makeText(
                    applicationContext,
                    R.string.please_select_an_option,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        createChannel(
            CHANNEL_ID,
            CHANNEL_NAME
        )
    }

    private fun isRadioGroupOptionSelected(optionsToDownload: RadioGroup): Boolean {
        return optionsToDownload.checkedRadioButtonId != -1
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (isDownloadCompleted(id)) {
                val notification = NotificationCompat.Builder(
                    applicationContext,
                    CHANNEL_ID
                )
                    .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                    .setContentTitle(
                        applicationContext
                            .getString(R.string.notification_title)
                    )
                    .setContentText("File Downloaded:" + id)
                    .setStyle(NotificationCompat.BigTextStyle()).build()

                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun isDownloadCompleted(downloadId: Long?): Boolean {
        return downloadId != -1L
    }

    private fun download(optionsToDownload: RadioGroup) {
        val request =
            DownloadManager.Request(Uri.parse(getDownloadURL(optionsToDownload)))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun getDownloadURL(optionsToDownload: RadioGroup): String {
        return when (findViewById<RadioButton>(optionsToDownload.checkedRadioButtonId).id) {
            R.id.glide_download_button -> GLIDE_URL
            R.id.retrofit_download_button -> RETROFIT_URL
            else -> APP_URL
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download Completed"

            notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }


    companion object {
        private const val APP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "downloadFilesNotificationChannel"
        private const val CHANNEL_NAME = "Load app Download Files Channel"
    }

}
