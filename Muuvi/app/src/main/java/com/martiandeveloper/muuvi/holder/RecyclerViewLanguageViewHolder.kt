package com.martiandeveloper.muuvi.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewLanguageAdapter
import com.martiandeveloper.muuvi.model.Language

class RecyclerViewLanguageViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.recyclerview_language_item, parent, false)) {
    private var recyclerviewLanguageItemPrimaryNameMTV: MaterialTextView? = null
    private var recyclerviewLanguageItemSecondaryNameMTV: MaterialTextView? = null
    private var recyclerviewLanguageItemCheckIV: ImageView? = null

    init {
        recyclerviewLanguageItemPrimaryNameMTV =
            itemView.findViewById(R.id.recyclerview_language_item_primaryNameMTV)
        recyclerviewLanguageItemSecondaryNameMTV =
            itemView.findViewById(R.id.recyclerview_language_item_secondaryNameMTV)
        recyclerviewLanguageItemCheckIV =
            itemView.findViewById(R.id.recyclerview_language_item_checkIV)
    }

    fun bind(language: Language, itemClickListener: RecyclerViewLanguageAdapter.ItemClickListener) {

        recyclerviewLanguageItemPrimaryNameMTV?.text = language.primaryName

        if (language.secondaryName == "-") {
            recyclerviewLanguageItemSecondaryNameMTV?.visibility = View.GONE
        } else {
            recyclerviewLanguageItemSecondaryNameMTV?.text = language.secondaryName
        }

        if (language.isChecked) {
            recyclerviewLanguageItemCheckIV?.visibility = View.VISIBLE
        } else {
            recyclerviewLanguageItemCheckIV?.visibility = View.GONE
        }

        itemView.setOnClickListener {
            itemClickListener.onItemClick(language)
        }
    }
}