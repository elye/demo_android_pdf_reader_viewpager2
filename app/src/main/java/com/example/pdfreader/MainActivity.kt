package com.example.pdfreader

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val fileName = "TestPdf.pdf"
        private const val CACHE_FILE_NAME = "cache-document.pdf"
    }

    private val pdfReader by lazy {
        val file = File(cacheDir, fileName)
        file.outputStream().use { fileOut ->
            assets.open(fileName).copyTo(fileOut)
        }
        PdfReader(file)
    }

    private val fileDownloader by lazy {
        PdfDownloader(
            OkHttpClient.Builder().build(),
            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pdf_view_pager.adapter = PageAdaptor()
        downloadPdf(
            "https://www.entnet.org/sites/default/files/uploads/PracticeManagement/Resources/_files/instructions-for-adding-your-logo.pdf"
        ) { file ->
            file ?: return@downloadPdf

            runOnUiThread {
                (pdf_view_pager.adapter as PageAdaptor).setupPdfRenderer(PdfReader(file))
            }
        }

        TabLayoutMediator(pdf_page_tab, pdf_view_pager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfReader.close()
    }

    private fun downloadPdf(pdfUrl: String, completion: (File?) -> Unit) {
        val request = Request.Builder()
            .url(pdfUrl)
            .build()
        val client = OkHttpClient.Builder()
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                println("successful download")

                val pdfData = response.body?.byteStream()

                pdfData?.apply {
                    val file = File(cacheDir, fileName)
                    file.outputStream().use { fileOut ->
                        copyTo(fileOut)
                    }
                    completion(file)
                } ?: completion(null)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d("Error", e.message)
                completion(null)
            }
        })
    }
}
