package com.github.whitescent.mastify

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MastifyApp : Application(), ImageLoaderFactory {
  override fun newImageLoader(): ImageLoader {
    val context = this.applicationContext
    return ImageLoader.Builder(context)
      .crossfade(true)
      .components {
        if (SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
        add(VideoFrameDecoder.Factory())
      }
      .build()
  }
}
