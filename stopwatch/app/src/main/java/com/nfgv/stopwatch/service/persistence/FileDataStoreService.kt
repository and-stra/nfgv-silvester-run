package com.nfgv.stopwatch.service.persistence

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FileDataStoreService {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cache")

    companion object {
        val instance: FileDataStoreService by lazy {
            FileDataStoreService()
        }
    }

    suspend fun writeInt(context: Context, key: String, value: Int) {
        context.dataStore.edit { cache -> cache[intPreferencesKey(key)] = value }
    }

    suspend fun writeString(context: Context, key: String, value: String) {
        context.dataStore.edit { cache -> cache[stringPreferencesKey(key)] = value }
    }

    suspend fun readInt(context: Context, key: String): Int? {
        return coroutineScope {
            async {
                context.dataStore.data.map { preferences ->
                    preferences[intPreferencesKey(key)]
                }.first()?.toInt()
            }.await()
        }
    }

    suspend fun readString(context: Context, key: String): String? {
        return coroutineScope {
            async {
                context.dataStore.data.map { preferences ->
                    preferences[stringPreferencesKey(key)]
                }.first()?.toString()
            }.await()
        }
    }
}