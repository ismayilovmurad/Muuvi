package com.martiandeveloper.muuvi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.service.MovieApi
import com.martiandeveloper.muuvi.service.POST_PER_PAGE
import io.reactivex.disposables.CompositeDisposable

class MoviePagedListRepository (private val apiService : MovieApi) {

    lateinit var moviePagedList: LiveData<PagedList<Movie>>
    lateinit var moviesDataSourceFactory: MovieDataSourceFactory

    fun fetchLiveMoviePagedList (movie:String,compositeDisposable: CompositeDisposable) : LiveData<PagedList<Movie>> {
        moviesDataSourceFactory = MovieDataSourceFactory(movie,apiService, compositeDisposable)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(POST_PER_PAGE)
            .build()

        moviePagedList = LivePagedListBuilder(moviesDataSourceFactory, config).build()

        return moviePagedList
    }

}