package com.example.wally

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

class GoogleDriveTokenProvider(
    context: Context
) {

    companion object {
        private const val DRIVE_FILE_SCOPE =
            "https://www.googleapis.com/auth/drive.file"
    }

    private val authorizationClient =
        Identity.getAuthorizationClient(context)

    suspend fun getAccessToken(): String {

        val request = AuthorizationRequest
            .builder()
            .setRequestedScopes(
                listOf(
                    Scope(DRIVE_FILE_SCOPE)
                )
            )
            .build()

        val result = authorizationClient
            .authorize(request)
            .await()

        val accessToken = result.accessToken

        if (accessToken.isNullOrEmpty()) {
            throw Exception(
                "Google authorization requires user interaction."
            )
        }

        return accessToken
    }
}