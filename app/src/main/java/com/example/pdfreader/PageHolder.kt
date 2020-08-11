package com.example.pdfreader

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_page.view.*

class PageHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun openPage(page: Int, pdfReader: PdfReader) {
        pdfReader.openPage(page, itemView.pdf_image)
    }
}
