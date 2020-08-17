package com.martiandeveloper.muuvi.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.service.FIRST_PAGE
import com.martiandeveloper.muuvi.service.MovieApi
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MovieDataSource (private val movie:String,private val apiService : MovieApi, private val compositeDisposable: CompositeDisposable)
    : PageKeyedDataSource<Int, Movie>(){

    private var page = FIRST_PAGE

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Movie>) {

        compositeDisposable.add(
            apiService.getMovie(movie,page)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        callback.onResult(it.results!!, null, page+1)
                    },
                    {
                        Log.e("MovieDataSource", it.message)
                    }
                )
        )
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {
        compositeDisposable.add(
            apiService.getMovie(movie,params.key)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        if(it.totalPages!! >= params.key) {
                            callback.onResult(it.results!!, params.key+1)
                        }
                    },
                    {
                        Log.e("MovieDataSource", it.message)
                    }
                )
        )
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Movie>) {

    }
}