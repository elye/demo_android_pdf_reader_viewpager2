package com.example.pdfreader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val fileName = "TestPdf.pdf"
    }

    private val pdfReader by lazy {
        val file = File(cacheDir, fileName)
        file.outputStream().use { fileOut ->
            assets.open(MainActivity.fileName).copyTo(fileOut)
        }
        PdfReader(file)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pdf_view_pager.adapter = PageAdaptor().apply {
            setupPdfRenderer(pdfReader)
        }

        TabLayoutMediator(pdf_page_tab, pdf_view_pager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfReader.close()
    }
}
