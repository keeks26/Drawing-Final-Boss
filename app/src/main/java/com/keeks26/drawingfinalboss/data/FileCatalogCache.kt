package com.keeks26.drawingfinalboss.data

import android.util.AtomicFile
import java.io.File
import java.io.IOException

class FileCatalogCache(private val directory: File) : CatalogCache {
    override fun read(difficulty: Int): String? {
        val file = file(difficulty)
        return if (file.baseFile.exists()) file.openRead().bufferedReader().use { it.readText() } else null
    }

    override fun write(difficulty: Int, json: String) {
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create photo cache")
        val file = file(difficulty)
        val stream = file.startWrite()
        try {
            stream.write(json.toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (error: IOException) {
            file.failWrite(stream)
            throw error
        }
    }

    private fun file(difficulty: Int): AtomicFile {
        require(difficulty in 1..3)
        return AtomicFile(File(directory, "level-$difficulty.json"))
    }
}
