package com.martiandeveloper.muuvi.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.adapter.RecyclerViewCountryAdapter
import com.martiandeveloper.muuvi.model.Country

class RecyclerViewCountryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.recyclerview_country_item, parent, false)) {
    private var recyclerviewCountryItemCountryNameAndCodeMTV: MaterialTextView? = null

    init {
        recyclerviewCountryItemCountryNameAndCodeMTV =
            itemView.findViewById(R.id.recyclerview_country_item_countryNameAndCodeMTV)
    }

    fun bind(country: Country, itemClickListener: RecyclerViewCountryAdapter.ItemClickListener) {

        val text: String = if (country.countryCode.contains("–")) {
            "${country.countryName} (+1)"
        } else {
            "${country.countryName} ${country.countryCode}"
        }

        recyclerviewCountryItemCountryNameAndCodeMTV?.text = text

        itemView.setOnClickListener {
            itemClickListener.onItemClick(country)
        }
    }
}