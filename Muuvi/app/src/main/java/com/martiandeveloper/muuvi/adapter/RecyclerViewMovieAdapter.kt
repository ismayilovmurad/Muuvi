@file:Suppress("PropertyName")

package com.martiandeveloper.muuvi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.martiandeveloper.muuvi.R
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.service.POSTER_BASE_URL
import kotlinx.android.synthetic.main.recyclerview_movie_item.view.*

class RecyclerViewMovieAdapter(
    private val context: Context,
    private val itemCLickListener: ItemClickListener
) : PagingDataAdapter<Movie, RecyclerView.ViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recyclerview_movie_item, parent, false)
        return MovieItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MovieItemViewHolder).bind(getItem(position), context, itemCLickListener)
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }

    }


    class MovieItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(
            movie: Movie?,
            context: Context,
            itemClickListener: ItemClickListener
        ) {
            if (movie != null) {
                if (movie.mediaType != "person") {
                    if (movie.mediaType == "tv") {
                        val title =
                            movie.originalName + " (" + movie.firstAirDate?.split(
                                "-"
                            )?.get(0) + ")"
                        itemView.recyclerview_movie_item_titleMTV.text = title
                    } else {
                        val title =
                            movie.title + " (" + movie.releaseDate?.split(
                                "-"
                            )
                                ?.get(0) + ")"
                        itemView.recyclerview_movie_item_titleMTV.text = title
                    }
                    itemView.recyclerview_movie_item_voteAverageMTV.text =
                        movie.voteAverage.toString()
                    Glide.with(context)
                        .load("${POSTER_BASE_URL}${movie.posterPath}")
                        .placeholder(R.drawable.logo1)
                        .centerCrop()
                        .into(itemView.recyclerview_movie_item_posterIV)

                    itemView.setOnClickListener {
                        itemClickListener.onItemClick(movie)
                    }
                }
            }

        }

    }

    interface ItemClickListener {
        fun onItemClick(movie: Movie)
    }
}