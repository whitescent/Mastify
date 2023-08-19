package com.github.whitescent.mastify.mapper.emoji

import com.github.whitescent.mastify.network.model.emoji.Emoji

fun List<Emoji>.toShortCode(): List<String> = this.map(Emoji::shortcode)
