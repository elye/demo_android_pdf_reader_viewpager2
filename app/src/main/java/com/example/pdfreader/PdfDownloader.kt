package com.example.pdfreader

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.text.TextUtils.isEmpty
import android.util.Log
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.closeQuietly
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class PdfDownloader(okHttpClient: OkHttpClient, private var downloadManager: DownloadManager) {

    companion object {
        private const val BUFFER_LENGTH_BYTES: Long = 1024 * 8
        private const val HTTP_TIMEOUT = 30
        private const val TAG = "PdfDownloader"
    }

    private var okHttpClient: OkHttpClient

    init {
        val okHttpBuilder = okHttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
        this.okHttpClient = okHttpBuilder.build()
    }


    @SuppressLint("CheckResult")
    fun download(url: String, file: File): Observable<Int> {
        Log.d(TAG, "download: " + url + " to path: " + file.absolutePath)
        return Observable.create<Int> { emitter ->
            var sink: BufferedSink? = null
            var source: BufferedSource? = null
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                val responseCode = response.code
                if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                    response.body?.let { body ->
                        val contentLength = body.contentLength()
                        source = body.source()
                        sink = file.sink().buffer()
                        sink?.let { sink ->
                            val sinkBuffer = sink.buffer()
                            var totalBytesRead: Long = 0
                            var bytesRead: Long = 0
                            while (source?.read(sinkBuffer, BUFFER_LENGTH_BYTES).also {
                                        if (it != null) {
                                            bytesRead = it
                                        }
                                    } != -1L) {
                                sink.emit()
                                totalBytesRead += bytesRead
                                val progress = (totalBytesRead * 100 / contentLength).toInt()
                                emitter.onNext(progress)
                            }
                            sink.flush()
                            emitter.onNext(100)
                        }
                    }
                } else {
                    throw java.lang.IllegalArgumentException("Error occurred when do http get $url")
                }
            } catch (exception: IOException) {
                emitter.onError(exception)
            } finally {
                sink?.closeQuietly()
                source?.closeQuietly()
            }
            emitter.onComplete()
        }
    }
}
