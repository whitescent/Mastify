/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.utils.ProgressRequestBody
import com.github.whitescent.mastify.utils.getMediaSize
import com.github.whitescent.mastify.utils.getServerErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

sealed class UploadEvent {
  data class ProgressEvent(val percentage: Int) : UploadEvent()
  data class FinishedEvent(val mediaId: String, val processed: Boolean) : UploadEvent()
  data class ErrorEvent(val error: Throwable) : UploadEvent()
}

data class UploadData(
  val flow: Flow<UploadEvent>,
  val scope: CoroutineScope
)

class FileRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val api: MastodonApi
) {

  val uploads = mutableMapOf<Uri, UploadData>()

  @OptIn(ExperimentalCoroutinesApi::class)
  fun addMediaToQueue(uri: Uri) {
    val uploadScope = CoroutineScope(Dispatchers.IO)
    uploads[uri] = UploadData(
      flow = flow { emit(uri) }
        .flatMapLatest { uploadMedia(it) }
        .catch { exception ->
          emit(UploadEvent.ErrorEvent(exception))
        }
        .shareIn(uploadScope, SharingStarted.Eagerly),
      scope = uploadScope
    )
  }

  fun cancelUpload(uri: Uri?) = uploads[uri]?.scope?.cancel()

  private suspend fun uploadMedia(uri: Uri): Flow<UploadEvent> {
    return callbackFlow {
      val contentResolver = context.contentResolver
      val mimeType = contentResolver.getType(uri)
      val suffix = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType ?: "tmp")
      val file = contentResolver.openInputStream(uri)?.buffered()?.use { input ->
        File.createTempFile("mastify_", suffix, context.cacheDir).apply {
          outputStream().buffered().use(input::copyTo)
        }
      }
      val fileBody = ProgressRequestBody(
        content = contentResolver.openInputStream(uri)!!,
        contentLength = getMediaSize(context.contentResolver, uri),
        mediaType = contentResolver.getType(uri)?.toMediaTypeOrNull()
          ?: "multipart/form-data".toMediaType()
      ) { trySend(UploadEvent.ProgressEvent(it)) }
      file?.let {
        val response = api.uploadMedia(
          file = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = fileBody
          )
        )
        val responseBody = response.body()
        if (response.isSuccessful && responseBody != null) {
          send(UploadEvent.FinishedEvent(responseBody.id, response.code() == 200))
        } else {
          val error = HttpException(response)
          val errorMessage = error.getServerErrorMessage()
          if (errorMessage == null) {
            throw error
          } else {
            throw Throwable(errorMessage)
          }
        }
      } ?: send(UploadEvent.ErrorEvent(Throwable("InputStream is null")))
      awaitClose()
    }
  }
}
