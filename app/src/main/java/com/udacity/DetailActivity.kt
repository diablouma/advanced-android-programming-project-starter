package com.udacity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*


class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val extras = intent.extras
        val downloadStatus = extras?.get(MainActivity.DOWNLOAD_STATUS_KEY).toString()
        val downlodedFileName : String = extras?.get(MainActivity.DOWNLOAD_TITLE_KEY).toString()

        findViewById<TextView>(R.id.file_name).setText(downlodedFileName)
        findViewById<TextView>(R.id.file_download_status).setText(downloadStatus)
    }

    fun goToMainActivity(view: View) {
        val intent = Intent(this, MainActivity.javaClass)
        startActivity(intent)
    }

}
