package com.martiandeveloper.muuvi.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("original_name")
    val originalName: String?,
    @SerializedName("genre_ids")
    val genreIds: List<Int>?,
    @SerializedName("media_type")
    val mediaType: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("origin_country")
    val originCountry: List<String>?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("original_language")
    val originalLanguage: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("vote_average")
    val voteAverage: Float?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("original_title")
    val originalTitle: String?)