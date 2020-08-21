package com.martiandeveloper.muuvi.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.service.MovieApi
import io.reactivex.disposables.CompositeDisposable

class MovieDataSourceFactory (private val movie:String,private val apiService : MovieApi, private val compositeDisposable: CompositeDisposable)
    : DataSource.Factory<Int, Movie>() {

    private val moviesLiveDataSource =  MutableLiveData<MovieDataSource>()

    override fun create(): DataSource<Int, Movie> {
        val movieDataSource = MovieDataSource(movie,apiService,compositeDisposable)

        moviesLiveDataSource.postValue(movieDataSource)
        return movieDataSource
    }
}