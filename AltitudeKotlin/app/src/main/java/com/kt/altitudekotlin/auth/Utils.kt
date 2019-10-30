package com.kt.altitudekotlin.auth

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Utils {
    companion object {
        private val INITIAL_READ_BUFFER_SIZE = 1024
        @Throws(IOException::class)
        fun readInputStream(inputStream: InputStream): String {
            val br = BufferedReader(InputStreamReader(inputStream))
            val buffer = CharArray(INITIAL_READ_BUFFER_SIZE)
            val sb = StringBuilder()
            var readCount: Int
//            while ((readCount = br.read(buffer)) != -1) {
//                sb.append(buffer, 0, readCount)
//            }
            br.forEachLine {
                sb.append(it)
            }
            return sb.toString()
    }

        fun closeQuietly(inputStream: InputStream?) {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
                // deliberately do nothing
            }

        }
    }
}