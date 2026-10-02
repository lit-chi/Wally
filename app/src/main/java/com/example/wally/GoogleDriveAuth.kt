package com.example.wally

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import android.accounts.Account
import com.google.android.gms.auth.api.identity.RevokeAccessRequest

class GoogleDriveAuth(
    private val activity: ComponentActivity
) {

    companion object {
        private const val DRIVE_FILE_SCOPE =
            "https://www.googleapis.com/auth/drive.file"
    }

    private val authorizationClient =
        Identity.getAuthorizationClient(activity)

    private var onSuccess: ((String) -> Unit)? = null
    private var onError: ((Exception) -> Unit)? = null

    private val authorizationLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode != Activity.RESULT_OK) {
                onError?.invoke(
                    Exception("Google Drive authorization was cancelled.")
                )
                return@registerForActivityResult
            }

            try {
                val authorizationResult =
                    authorizationClient.getAuthorizationResultFromIntent(
                        result.data
                    )

                handleAuthorizationResult(authorizationResult)

            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }

    fun authorize(
        onSuccess: (accessToken: String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        this.onSuccess = onSuccess
        this.onError = onError

        val request = AuthorizationRequest
            .builder()
            .setRequestedScopes(
                listOf(
                    Scope(DRIVE_FILE_SCOPE)
                )
            )
            .build()

        authorizationClient
            .authorize(request)
            .addOnSuccessListener { result ->
                handleAuthorizationResult(result)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    private fun handleAuthorizationResult(
        result: AuthorizationResult
    ) {
        if (result.hasResolution()) {

            val pendingIntent = result.pendingIntent

            if (pendingIntent == null) {
                onError?.invoke(
                    Exception("Google authorization requires a resolution, but none was provided.")
                )
                return
            }

            val intentSenderRequest =
                androidx.activity.result.IntentSenderRequest
                    .Builder(pendingIntent.intentSender)
                    .build()

            authorizationLauncher.launch(intentSenderRequest)

            return
        }

        val accessToken = result.accessToken

        if (accessToken.isNullOrEmpty()) {
            onError?.invoke(
                Exception("Google authorization succeeded, but no access token was returned.")
            )
            return
        }

        onSuccess?.invoke(accessToken)
    }

    fun revokeAccess(
        email: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val account = Account(
            email,
            "com.google"
        )

        val request = RevokeAccessRequest
            .builder()
            .setAccount(account)
            .setScopes(
                listOf(
                    Scope(DRIVE_FILE_SCOPE)
                )
            )
            .build()

        authorizationClient
            .revokeAccess(request)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}