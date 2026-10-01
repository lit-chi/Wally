package com.example.wally

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.io.File
import java.io.IOException

class GoogleDriveClient(
    private val accessToken: String
) {

    companion object {
        private const val DRIVE_API =
            "https://www.googleapis.com/drive/v3"

        private const val DRIVE_UPLOAD_API =
            "https://www.googleapis.com/upload/drive/v3"

        private const val FILE_NAME =
            "Wally Expenses.xlsx"

        private const val MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }

    private val client = OkHttpClient()

    suspend fun uploadOrUpdate(
        xlsxFile: File
    ): String = withContext(Dispatchers.IO) {

        val existingFileId = findFileId()

        if (existingFileId == null) {
            createFile(xlsxFile)
        } else {
            updateFile(
                fileId = existingFileId,
                xlsxFile = xlsxFile
            )
        }
    }

    private fun findFileId(): String? {

        val url = "$DRIVE_API/files"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter(
                "q",
                "name = '$FILE_NAME' and trashed = false"
            )
            .addQueryParameter(
                "spaces",
                "drive"
            )
            .addQueryParameter(
                "fields",
                "files(id,name,mimeType)"
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .header(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()

        client.newCall(request).execute().use { response ->

            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw IOException(
                    "Drive search failed: ${response.code} $body"
                )
            }

            val json = JSONObject(body)
            val files = json.optJSONArray("files")

            if (files == null || files.length() == 0) {
                return null
            }

            return files
                .getJSONObject(0)
                .getString("id")
        }
    }

    private fun createFile(
        xlsxFile: File
    ): String {

        val metadata = JSONObject()
            .put("name", FILE_NAME)
            .put("mimeType", MIME_TYPE)

        val multipartBody = MultipartBody.Builder()
            .setType("related".toMediaType())

            .addPart(
                Headers.headersOf(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                ),
                metadata
                    .toString()
                    .toRequestBody(
                        "application/json; charset=UTF-8"
                            .toMediaType()
                    )
            )

            .addPart(
                Headers.headersOf(
                    "Content-Type",
                    MIME_TYPE
                ),
                xlsxFile.asRequestBody(
                    MIME_TYPE.toMediaType()
                )
            )

            .build()

        val url = "$DRIVE_UPLOAD_API/files"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter(
                "uploadType",
                "multipart"
            )
            .addQueryParameter(
                "fields",
                "id,name"
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(multipartBody)
            .header(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()

        client.newCall(request).execute().use { response ->

            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw IOException(
                    "Drive create failed: ${response.code} $body"
                )
            }

            return JSONObject(body)
                .getString("id")
        }
    }

    private fun updateFile(
        fileId: String,
        xlsxFile: File
    ): String {

        val url = "$DRIVE_UPLOAD_API/files/$fileId"
            .toHttpUrl()
            .newBuilder()
            .addQueryParameter(
                "uploadType",
                "media"
            )
            .addQueryParameter(
                "fields",
                "id,name"
            )
            .build()

        val requestBody = xlsxFile.asRequestBody(
            MIME_TYPE.toMediaType()
        )

        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .header(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()

        client.newCall(request).execute().use { response ->

            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw IOException(
                    "Drive update failed: ${response.code} $body"
                )
            }

            return JSONObject(body)
                .getString("id")
        }
    }
}