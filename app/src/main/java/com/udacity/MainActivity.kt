package com.udacity

import android.animation.ValueAnimator
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private val NOTIFICATION_ID = 0
    private val REQUEST_CODE = 0
    private val FLAGS = 0
    private var downloadID: Long = 0
    lateinit var customDownloadButton: LoadingButton

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        customDownloadButton = findViewById(R.id.custom_button)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            val optionsToDownload = findViewById<RadioGroup>(R.id.files_to_download)
            if (isRadioGroupOptionSelected(optionsToDownload)) {
                custom_button.buttonState = ButtonState.Loading
                download(optionsToDownload)
            } else {
                custom_button.buttonState = ButtonState.Loading
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

            val downloadTitleAndStatus = getDownloadTitleAndStatus(id!!)

            val detailActivityIntent = Intent(applicationContext, DetailActivity::class.java)
            detailActivityIntent.putExtra(
                DOWNLOAD_STATUS_KEY, downloadTitleAndStatus.get(
                    DOWNLOAD_STATUS_KEY
                )
            )

            detailActivityIntent.putExtra(
                DOWNLOAD_TITLE_KEY, downloadTitleAndStatus.get(
                    DOWNLOAD_TITLE_KEY
                )
            )

            pendingIntent = TaskStackBuilder.create(applicationContext).run {
                addNextIntentWithParentStack(detailActivityIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }


            val notification = NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                .setContentTitle(
                    applicationContext
                        .getString(R.string.notification_title)
                )
                .setContentText(
                    "File Downloaded:" + downloadTitleAndStatus.get(
                        DOWNLOAD_TITLE_KEY
                    )
                )
                .setStyle(NotificationCompat.BigTextStyle())
                .addAction(
                    R.drawable.ic_assistant_black_24dp,
                    getString(R.string.notification_action),
                    pendingIntent
                )
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            Toast.makeText(context, getString(R.string.download_completed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDownloadTitleAndStatus(downloadId: Long): Map<String, String> {
        val downloadManagerQuery = DownloadManager.Query()
        downloadManagerQuery.setFilterById(downloadId)
        val result = HashMap<String, String>()

        val queryResults =
            (applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(
                downloadManagerQuery
            )

        if (queryResults.moveToFirst()) {
            val status =
                queryResults.getInt(queryResults.getColumnIndex(DownloadManager.COLUMN_STATUS))

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                result.put(DOWNLOAD_STATUS_KEY, getString(R.string.success_status))
            } else {
                result.put(DOWNLOAD_STATUS_KEY, getString(R.string.failed_status))
            }
            result.put(
                DOWNLOAD_TITLE_KEY,
                queryResults.getString(queryResults.getColumnIndex(DownloadManager.COLUMN_TITLE))
            )
        }

        return result
    }

    private fun isDownloadCompleted(downloadId: Long?): Boolean {
        return downloadId != null && downloadId != -1L
    }

    private fun download(optionsToDownload: RadioGroup) {
        val request =
            DownloadManager.Request(Uri.parse(getDownloadURL(optionsToDownload)))
                .setTitle(getDownloadTitle(optionsToDownload))
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

    private fun getDownloadTitle(optionsToDownload: RadioGroup): String {
        return when (findViewById<RadioButton>(optionsToDownload.checkedRadioButtonId).id) {
            R.id.glide_download_button -> getString(R.string.glide_btn_label)
            R.id.retrofit_download_button -> getString(R.string.retrofit_download_btn_label)
            else -> getString(R.string.load_app_repo_btn_label)
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
        const val DOWNLOAD_STATUS_KEY = "downloadStatus"
        const val DOWNLOAD_TITLE_KEY = "downloadTitle"
    }

}
