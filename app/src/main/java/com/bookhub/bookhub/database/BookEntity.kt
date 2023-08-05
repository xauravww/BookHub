package com.bookhub.bookhub.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("books")
data class BookEntity(
    @PrimaryKey val book_id: Int,
    @ColumnInfo("book_name") val name: String,
    @ColumnInfo("book_author") val author: String,
    @ColumnInfo("book_price") val rating: String,
    @ColumnInfo("book_rating") val price: String,
    @ColumnInfo("book_description") val description: String,
    @ColumnInfo("book_image") val bookImageUrl: String
)