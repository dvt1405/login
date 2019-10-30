package com.kt.altitudekotlin.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

class TextUtils {
    companion object {
        fun readFromFile(context: Context, fileName: String): String {
            var stringBuilder = StringBuilder()
            var inputStream: InputStream? = null
            var inputStreamReader: InputStreamReader? = null
            var bufferReader: BufferedReader? = null
            try {
                inputStream = context.resources.assets.open(fileName)
                inputStreamReader = InputStreamReader(inputStream)
                bufferReader = BufferedReader(inputStreamReader)
                bufferReader.forEachLine {
                    stringBuilder.append(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (inputStream != null)
                        inputStream.close()
                    if (inputStreamReader != null)
                        inputStreamReader.close()
                    if (bufferReader != null)
                        bufferReader.close()
                } catch (e2: Exception) {
                    e2.message
                }

            }
            return stringBuilder.toString()
        }

    }
}