package com.martiandeveloper.muuvi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.martiandeveloper.muuvi.holder.RecyclerViewCountryViewHolder
import com.martiandeveloper.muuvi.model.Country
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewCountryAdapter(
    private val countryList: ArrayList<Country>,
    private val context: Context,
    private val itemCLickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerViewCountryViewHolder>(), Filterable {

    var countryFilterList = ArrayList<Country>()

    init {
        countryFilterList = countryList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewCountryViewHolder {
        val inflater = LayoutInflater.from(context)
        return RecyclerViewCountryViewHolder(
            inflater,
            parent
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewCountryViewHolder, position: Int) {
        val country: Country = countryFilterList[position]
        holder.bind(country, itemCLickListener)
    }

    override fun getItemCount(): Int = countryFilterList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                countryFilterList = if (charSearch.isEmpty()) {
                    countryList
                } else {
                    val resultList = ArrayList<Country>()
                    for (i in countryList) {
                        if (i.countryName.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT)) || i.countryCode
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
                filterResults.values = countryFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                countryFilterList = results?.values as ArrayList<Country>
                notifyDataSetChanged()
            }

        }
    }

    interface ItemClickListener {
        fun onItemClick(country: Country)
    }
}