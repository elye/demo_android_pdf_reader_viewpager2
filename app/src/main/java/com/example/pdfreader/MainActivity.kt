package com.example.pdfreader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers.*
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FILE_NAME = "TestPdf.pdf"
        private const val URL = "https://www.entnet.org/sites/default/files/uploads/PracticeManagement/Resources/_files/instructions-for-adding-your-logo.pdf"
    }

    private var disposable = Disposables.disposed()

    private val fileDownloader by lazy {
        FileDownloader(
            OkHttpClient.Builder().build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdf_view_pager.adapter = PageAdaptor()

        val targetFile = File(cacheDir, FILE_NAME)

        disposable = fileDownloader.download(URL, targetFile)
            .throttleFirst(1, TimeUnit.SECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .subscribeOn(Schedulers.io())
            .observeOn(mainThread())
            .subscribe({

            }, {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }, {
                PdfReader(targetFile).apply {
                    (pdf_view_pager.adapter as PageAdaptor).setupPdfRenderer(this)
                }
            })

        TabLayoutMediator(pdf_page_tab, pdf_view_pager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
