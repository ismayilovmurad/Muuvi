package com.martiandeveloper.muuvi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.martiandeveloper.muuvi.holder.RecyclerViewLanguageViewHolder
import com.martiandeveloper.muuvi.model.Language
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewLanguageAdapter(
    private val languageList: ArrayList<Language>,
    private val context: Context,
    private val itemCLickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerViewLanguageViewHolder>(), Filterable {

    var languageFilterList = ArrayList<Language>()

    init {
        languageFilterList = languageList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewLanguageViewHolder {
        val inflater = LayoutInflater.from(context)
        return RecyclerViewLanguageViewHolder(
            inflater,
            parent
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewLanguageViewHolder, position: Int) {
        val language: Language = languageFilterList[position]
        holder.bind(language, itemCLickListener)
    }

    override fun getItemCount(): Int = languageFilterList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                languageFilterList = if (charSearch.isEmpty()) {
                    languageList
                } else {
                    val resultList = ArrayList<Language>()
                    for (i in languageList) {
                        if (i.primaryName.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT)) || i.secondaryName
                                .toLowerCase(
                                    Locale.ROOT
                                ).contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(i)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = languageFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                languageFilterList = results?.values as ArrayList<Language>
                notifyDataSetChanged()
            }

        }
    }

    interface ItemClickListener {
        fun onItemClick(language: Language)
    }
}