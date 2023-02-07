package com.example.loggingapp

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LogFile {
    companion object {
        private val executor: ExecutorService = Executors.newSingleThreadExecutor()
        const val LOG_EXPIRED_DAY = 13
    }

    fun postLog(context: Context, message: String, date: LocalDateTime = LocalDateTime.now()) {
        createLogLine(message, date).let {
            executor.takeUnless { it.isShutdown }?.execute{
                flush(context, it)
            }
        }
    }

    private fun createLogLine(
        message: String, date: LocalDateTime
    ): String {
        return  "${date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))} $message"
    }

    private fun flush(context: Context, log: String) {
        val today = LocalDateTime.now()
        val fileName = "${today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.log"
        val file = File(context.filesDir, fileName)

        // まだ当日分のファイルが作成されていなかったら
        // 14日前までに作成されたファイルを削除する
        if (!file.exists()) deleteExpiredFiles(context)

        BufferedWriter(FileWriter(file, true)).use { writer ->
            writer.write(log)
            writer.newLine()
            writer.flush()
        }
    }

    private fun deleteExpiredFiles(context: Context) {
        context.filesDir
            .listFiles()
            ?.toList()
            ?.filter { file -> file.name.endsWith(".log") }
            ?.let { fileList ->
                fileList.map { file ->
                    if (expired(file) && file.exists()) file.delete()
                }
            }
    }

    private fun expired(file: File): Boolean {
        return toLocalDate(file.lastModified())
            .isBefore(LocalDate.now().minusDays(LOG_EXPIRED_DAY.toLong()))
    }
    private fun toLocalDate(lastModified: Long): LocalDate {
        return Instant
            .ofEpochMilli(lastModified)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}