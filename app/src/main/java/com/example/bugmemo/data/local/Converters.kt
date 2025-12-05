// app/src/main/java/com/example/bugmemo/data/local/Converters.kt
package com.example.bugmemo.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

// ★ Added: List<String> などをRoomに保存するための変換機
class Converters {
    private val moshi = Moshi.Builder().build()
    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java),
    )

    @TypeConverter
    fun fromStringList(list: List<String>?): String = listStringAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toStringList(json: String?): List<String> = json?.let { listStringAdapter.fromJson(it) } ?: emptyList()
}
