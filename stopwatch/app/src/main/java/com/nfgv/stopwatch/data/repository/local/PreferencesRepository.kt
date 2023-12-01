package com.nfgv.stopwatch.data.repository.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nfgv.stopwatch.util.Constants.PREFERENCES_DATA_STORE_NAME
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_DATA_STORE_NAME
    )

    suspend fun writeString(key: String, value: String) {
        context.dataStore.edit { preferences -> preferences[stringPreferencesKey(key)] = value }
    }

    suspend fun readString(key: String): String? {
        return coroutineScope {
            async {
                context.dataStore.data.map { preferences ->
                    preferences[stringPreferencesKey(key)]
                }.first()?.toString()
            }.await()
        }
    }
}