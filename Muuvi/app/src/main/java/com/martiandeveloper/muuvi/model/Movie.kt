package com.martiandeveloper.muuvi.model

import com.squareup.moshi.Json

data class Movie(
    @Json(name="original_name")
    val originalName: String?,
    @Json(name="genre_ids")
    val genreIds: List<Int>?,
    @Json(name="media_type")
    val mediaType: String?,
    @Json(name="name")
    val name: String?,
    @Json(name="origin_country")
    val originCountry: List<String>?,
    @Json(name="first_air_date")
    val firstAirDate: String?,
    @Json(name="original_language")
    val originalLanguage: String?,
    @Json(name="id")
    val id: Int?,
    @Json(name="vote_average")
    val voteAverage: Float?,
    @Json(name="overview")
    val overview: String?,
    @Json(name="poster_path")
    val posterPath: String?,
    @Json(name="title")
    val title: String?,
    @Json(name="release_date")
    val releaseDate: String?,
    @Json(name="original_title")
    val originalTitle: String?)