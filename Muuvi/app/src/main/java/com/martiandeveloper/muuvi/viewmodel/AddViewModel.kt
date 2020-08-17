package com.martiandeveloper.muuvi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.martiandeveloper.muuvi.model.MovieResult
import com.martiandeveloper.muuvi.model.Movie
import com.martiandeveloper.muuvi.repository.MoviePagedListRepository
import com.martiandeveloper.muuvi.service.MovieService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class AddViewModel(private val movieRepository : MoviePagedListRepository) : ViewModel() {

    val isError = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()
    private val compositeDisposable = CompositeDisposable()
    val movieSeriesETContent = MutableLiveData<String>()

    val  moviePagedList : LiveData<PagedList<Movie>> by lazy {
        movieRepository.fetchLiveMoviePagedList("break",compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}