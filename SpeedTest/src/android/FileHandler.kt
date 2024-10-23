package com.ncinga.speedtest
import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class FileHandler(private val context: Context) {
    private val TAG = "SpeedTest"

    fun writeFile(data: String, fileName: String) {
        Log.d(TAG, "execute writeFile() method")

        try {
            val dir = File(context.filesDir, "SpeedTest")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "$fileName.json")
            val fileWriter = FileWriter(file)
            val bufferedWriter = BufferedWriter(fileWriter)

            bufferedWriter.write(data)
            bufferedWriter.close()

            Log.i(TAG, "File write success: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "File write error", e)
        }
    }
}