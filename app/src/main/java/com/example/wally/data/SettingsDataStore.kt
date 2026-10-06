package com.example.wally.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey

private const val DATASTORE_NAME = "wally_settings"

private val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

class SettingsDataStore(
    private val context: Context
) {

    companion object {
        private val GOOGLE_EMAIL =
            stringPreferencesKey("google_email")

        private val GOOGLE_DRIVE_FILE_ID =
            stringPreferencesKey("google_drive_file_id")

        private val GOOGLE_DRIVE_NEEDS_RECONNECT =
            booleanPreferencesKey("google_drive_needs_reconnect")
    }

    val googleEmail: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[GOOGLE_EMAIL]
        }

    val googleDriveFileId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[GOOGLE_DRIVE_FILE_ID]
        }

    val googleDriveNeedsReconnect: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[GOOGLE_DRIVE_NEEDS_RECONNECT] ?: false
        }

    suspend fun saveGoogleAccount(
        email: String,
        fileId: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_EMAIL] = email
            preferences[GOOGLE_DRIVE_FILE_ID] = fileId
        }
    }

    suspend fun clearGoogleAccount() {
        context.dataStore.edit { preferences ->
            preferences.remove(GOOGLE_EMAIL)
            preferences.remove(GOOGLE_DRIVE_FILE_ID)
        }
    }

    suspend fun setGoogleDriveNeedsReconnect() {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_DRIVE_NEEDS_RECONNECT] = true
        }
    }

    suspend fun clearGoogleDriveNeedsReconnect() {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_DRIVE_NEEDS_RECONNECT] = false
        }
    }
}