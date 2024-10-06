package com.example.dailyquest.utils

import LocalTimeAdapter
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.dailyquest.models.DataContainer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class JsonManager(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .setPrettyPrinting()
        .create()

    private val fileName = "data_container.json"
    private val file: File = File(context.filesDir, fileName)

    // Serialize DataContainer to JSON and write to file
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveData(dataContainer: DataContainer) {
        val jsonString = gson.toJson(dataContainer)
        try {
            file.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Deserialize JSON from file into DataContainer object
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData(): DataContainer? {
        return try {
            if (file.exists()) {
                val jsonString = file.readText()

                // Try to deserialize the JSON
                val type: Type = object : TypeToken<DataContainer>() {}.type
                gson.fromJson<DataContainer>(jsonString, type)
            } else {
                null
            }
        } catch (e: JsonSyntaxException) {
            // Catch JSON parsing errors
            e.printStackTrace()
            handleInvalidFile()
        } catch (e: ClassCastException) {
            // Catch class cast errors
            e.printStackTrace()
            handleInvalidFile()
        } catch (e: IOException) {
            // Catch IO errors (file read errors)
            e.printStackTrace()
            null
        }
    }

    // Helper function to delete the file and return null
    private fun handleInvalidFile(): DataContainer? {
        // If there's an error, delete the invalid file
        if (file.exists()) {
            file.delete()
        }
        return null
    }
}
