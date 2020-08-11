package com.example.pdfreader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    companion object {
        private const val fileName = "TestPdf.pdf"
    }

    private val pdfRenderer by lazy {
        val file = File(cacheDir, fileName)
        file.outputStream().use { fileOut ->
            assets.open(fileName).copyTo(fileOut)
        }
        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(fileDescriptor)
    }
    private var currentPage: PdfRenderer.Page? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openPage(0)
    }

    private fun openPage(page: Int) {
        if (pdfRenderer.pageCount <= page) return

        currentPage?.close()
        currentPage = pdfRenderer.openPage(page).apply {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdf_image.setImageBitmap(bitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer.close()
    }
}
