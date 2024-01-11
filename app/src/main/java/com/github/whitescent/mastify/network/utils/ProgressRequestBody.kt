package com.github.whitescent.mastify.network.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream

class ProgressRequestBody(
  private val content: InputStream,
  private val contentLength: Long,
  private val mediaType: MediaType,
  private val uploadListener: UploadCallback
) : RequestBody() {
  fun interface UploadCallback {
    fun onProgressUpdate(percentage: Int)
  }

  override fun contentType(): MediaType {
    return mediaType
  }

  override fun contentLength(): Long {
    return contentLength
  }

  @Throws(IOException::class)
  override fun writeTo(sink: BufferedSink) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var uploaded: Long = 0

    content.use { content ->
      var read: Int
      while (content.read(buffer).also { read = it } != -1) {
        uploadListener.onProgressUpdate((100 * uploaded / contentLength).toInt())
        uploaded += read.toLong()
        sink.write(buffer, 0, read)
      }
      uploadListener.onProgressUpdate((100 * uploaded / contentLength).toInt())
    }
  }

  companion object {
    private const val DEFAULT_BUFFER_SIZE = 2048
  }
}
