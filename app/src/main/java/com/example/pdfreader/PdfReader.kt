package com.example.pdfreader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import java.io.File

class PdfReader(file: File) {

    private var currentPage: PdfRenderer.Page? = null
    private val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val pdfRenderer = PdfRenderer(fileDescriptor)

    val pageCount = pdfRenderer.pageCount

    fun openPage(page: Int, pdfImage: ImageView) {
        if (page >= pageCount) return
        currentPage?.close()
        currentPage = pdfRenderer.openPage(page).apply {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfImage.setImageBitmap(bitmap)
        }
    }

    fun close() {
        currentPage?.close()
        fileDescriptor.close()
        pdfRenderer.close()
    }
}
