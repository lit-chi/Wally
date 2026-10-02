package com.example.wally.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
    }

    val googleEmail: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[GOOGLE_EMAIL]
        }

    val googleDriveFileId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[GOOGLE_DRIVE_FILE_ID]
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
}