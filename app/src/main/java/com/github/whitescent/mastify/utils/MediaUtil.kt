package com.github.whitescent.mastify.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

const val MEDIA_SIZE_UNKNOWN = -1L

/**
 * Fetches the size of the media represented by the given URI, assuming it is openable and
 * the ContentResolver is able to resolve it.
 *
 * @return the size of the media in bytes or {@link MediaUtils#MEDIA_SIZE_UNKNOWN}
 */
fun getMediaSize(contentResolver: ContentResolver, uri: Uri?): Long {
  if (uri == null) {
    return MEDIA_SIZE_UNKNOWN
  }

  var mediaSize = MEDIA_SIZE_UNKNOWN
  val cursor: Cursor?
  try {
    cursor = contentResolver.query(uri, null, null, null, null)
  } catch (e: SecurityException) {
    return MEDIA_SIZE_UNKNOWN
  }
  if (cursor != null) {
    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
    cursor.moveToFirst()
    mediaSize = cursor.getLong(sizeIndex)
    cursor.close()
  }
  return mediaSize
}
