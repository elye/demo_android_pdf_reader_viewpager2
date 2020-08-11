package com.example.pdfreader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PageAdaptor: RecyclerView.Adapter<PageHolder>() {

    private var pdfReader: PdfReader? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PageHolder(view)
    }

    fun setupPdfRenderer(pdfReader: PdfReader) {
        this.pdfReader = pdfReader
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return pdfReader?.pageCount ?: 0
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        pdfReader?.let {
            holder.openPage(position, it)
        }
    }
}
