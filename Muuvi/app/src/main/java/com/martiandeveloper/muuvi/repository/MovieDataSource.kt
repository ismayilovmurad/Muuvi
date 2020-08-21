package com.martiandeveloper.muuvi.repository



import androidx.paging.PagingSource
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.service.MovieApi

class MovieDataSource(private val movie:String,private val apiService: MovieApi) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        try {
            val currentLoadingPageKey = params.key ?: 1
            val response = apiService.getMovie(movie,currentLoadingPageKey)
            val responseData = mutableListOf<Movie>()
            val data = response.body()?.results ?: emptyList()
            responseData.addAll(data)

            val prevKey = if (currentLoadingPageKey == 1) null else currentLoadingPageKey - 1

            return LoadResult.Page(
                data = responseData,
                prevKey = prevKey,
                nextKey = currentLoadingPageKey.plus(1)
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

}



/*
import android.util.Log
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
}*/
