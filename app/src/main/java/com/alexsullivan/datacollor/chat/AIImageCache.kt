package com.alexsullivan.datacollor.chat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIImageCache @Inject constructor() {
    private val cacheMap = mutableMapOf<String, ByteArray>()

    fun getImage(fileId: String): ByteArray? {
        return cacheMap[fileId]
    }

    fun putImage(fileId: String, image: ByteArray) {
        cacheMap[fileId] = image
    }
}
