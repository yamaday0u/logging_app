package com.example.loggingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag("LoggingAppTag").i("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        Timber.tag("LoggingAppTag").i("onStart")
        super.onStart()
    }

    override fun onResume() {
        Timber.tag("LoggingAppTag").i("onResume")
        super.onResume()
        setLogMenu()
    }

    override fun onPause() {
        Timber.tag("LoggingAppTag").i("onPause")
        super.onPause()
    }

    override fun onStop() {
        Timber.tag("LoggingAppTag").i("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Timber.tag("LoggingAppTag").i("onDestroy")
        deleteZipFiles()
        super.onDestroy()
    }

    private fun setLogMenu() {
        val view = findViewById<TextView>(R.id.logging)
        view.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add("ログファイルを共有する").setOnMenuItemClickListener {
                shareLogFile()
                true
            }
        }
    }

    private fun shareLogFile() {
        val files: List<File> = filesDir.listFiles()?.toList() ?: listOf()
        val targetFiles = mutableListOf<File>().apply {
            files.forEach { this.add(it) }
        }
        if (targetFiles.isNotEmpty()) {
            val zipFileName = "${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}.zip"
            val zipFile = File(filesDir, zipFileName)
            toZip(targetFiles, zipFile)

            val uri = FileProvider.getUriForFile(this, "com.example.loggingapp.fileprovider", zipFile)
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/zip"
            }.run {
                val shareIntent = Intent.createChooser(this, null)
                startActivity(shareIntent)
            }
            Toast.makeText(this, "ログファイルを共有します", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toZip(targetFiles: List<File>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { output ->
            targetFiles.forEach { file ->
                if (file.length() > 1) {
                    BufferedInputStream(FileInputStream(file)).use { input ->
                        output.putNextEntry(ZipEntry(file.name))
                        input.copyTo(output, 1024)
                    }
                }
            }
        }
    }

    private fun deleteZipFiles() {
        filesDir
            .listFiles()
            ?.toList()
            ?.filter { it.name.endsWith(".zip") }
            ?.let { files ->
                files.forEach { file ->
                    if (file.exists()) file.delete()
                }
            }
    }
}