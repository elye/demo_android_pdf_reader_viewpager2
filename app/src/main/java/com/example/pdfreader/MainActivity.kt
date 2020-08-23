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
    private var dispoable = Disposables.disposed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdf_view_pager.adapter = PageAdaptor()

        val targetFile = File(cacheDir, CACHE_FILE_NAME)
        dispoable = fileDownloader.download(
            "https://www.entnet.org/sites/default/files/uploads/PracticeManagement/Resources/_files/instructions-for-adding-your-logo.pdf", targetFile)
            .throttleFirst(1, TimeUnit.SECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({  }, {  },
                {
                    PdfReader(targetFile).apply {
                        (pdf_view_pager.adapter as PageAdaptor).setupPdfRenderer(this)
                    }
                })

//        downloadPdf(this, "https://www.entnet.org/sites/default/files/uploads/PracticeManagement/Resources/_files/instructions-for-adding-your-logo.pdf") {
//            _, inputStream ->
//            inputStream?.let{
//                val file = File(cacheDir, fileName)
//                file.outputStream().use { fileOut ->
//                    inputStream.copyTo(fileOut)
//                }
//
//                (pdf_view_pager.adapter as PageAdaptor).setupPdfRenderer(PdfReader(file))
//            }
//        }

        TabLayoutMediator(pdf_page_tab, pdf_view_pager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfReader.close()
    }

    fun downloadPdf(context: Context, pdfUrl: String, completion: (Boolean, InputStream?) -> Unit) {

        val request = Request.Builder()
            .url(pdfUrl)
            .build()
        val client = OkHttpClient.Builder()
            .build()

        client.newCall(request).enqueue(object: Callback {

            override fun onResponse(call: Call, response: Response) {
                println("successful download")

                val pdfData = response.body?.byteStream()

                //At this point you can do something with the pdf data
                //Below I add it to internal storage

                if (pdfData != null) {

                    try {
                        context.openFileOutput("myFile.pdf", Context.MODE_PRIVATE).use { output ->
                            output.write(pdfData.readBytes())
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                completion(true, pdfData)
            }

            override fun onFailure(call: Call, e: IOException) {

                Log.d("Elisha", e.message)
                completion(true, null)
            }
        })
    }
}
