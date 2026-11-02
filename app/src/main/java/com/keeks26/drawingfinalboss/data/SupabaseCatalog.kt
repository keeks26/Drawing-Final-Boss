package com.keeks26.drawingfinalboss.data

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

interface PhotoCatalog {
    suspend fun loadPage(difficulty: Int, cursor: String? = null): PhotoPage
}

interface CatalogCache {
    fun read(difficulty: Int): String?
    fun write(difficulty: Int, json: String)
}

class CatalogHttpException(val status: Int) : IOException("Catalog HTTP $status")
class InvalidCatalogException : IOException("Invalid reference catalog")

class SupabaseCatalog(
    baseUrl: String,
    private val publishableKey: String,
    private val cache: CatalogCache,
    private val fetch: suspend (Request) -> String = CatalogHttpClient()::get
) : PhotoCatalog {
    private val base = baseUrl.trimEnd('/').toHttpUrl()

    init {
        require(base.isHttps && base.encodedPath == "/") { "Use the HTTPS project base URL" }
        require(publishableKey.isNotBlank() && !publishableKey.startsWith("sb_secret_"))
    }

    override suspend fun loadPage(difficulty: Int, cursor: String?): PhotoPage =
        withContext(Dispatchers.IO) {
            require(difficulty in 1..3)
            val request = Request.Builder()
                .url(pageUrl(difficulty, cursor))
                // sb_publishable keys are not JWTs: use apikey, not Authorization: Bearer.
                .header("apikey", publishableKey)
                .header("Accept", "application/json")
                .build()
            try {
                val body = fetch(request)
                val page = decodePage(body, difficulty)
                if (cursor == null) {
                    // Cache only the first, bounded page; refreshes replace stale records.
                    try { cache.write(difficulty, body) } catch (_: IOException) { }
                }
                page
            } catch (error: CatalogHttpException) {
                throw error
            } catch (error: InvalidCatalogException) {
                throw error
            } catch (error: IOException) {
                if (cursor != null) throw error
                val saved = try { cache.read(difficulty) } catch (_: IOException) { null }
                val page = saved?.let {
                    try { decodePage(it, difficulty) } catch (_: InvalidCatalogException) { null }
                }
                if (page == null || page.photos.isEmpty()) throw error
                page.copy(nextCursor = null, fromCache = true)
            }
        }

    internal fun pageUrl(difficulty: Int, cursor: String?): HttpUrl = base.newBuilder()
        .addPathSegments("rest/v1/reference_photos")
        .addQueryParameter("select", "id,storage_path,title,pose_type,difficulty,is_full_body,attribution,license")
        .addQueryParameter("status", "eq.published")
        .addQueryParameter("difficulty", "eq.$difficulty")
        .addQueryParameter("order", "id.asc")
        .addQueryParameter("limit", (PAGE_SIZE + 1).toString())
        .apply {
            cursor?.let {
                UUID.fromString(it)
                addQueryParameter("id", "gt.$it")
            }
        }.build()

    companion object {
        const val PAGE_SIZE = 24

        internal fun decodePage(body: String, difficulty: Int): PhotoPage {
            try {
                val records = JSONArray(body)
                val count = minOf(records.length(), PAGE_SIZE)
                val photos = (0 until count).map { index ->
                    val row = records.getJSONObject(index)
                    val id = row.getString("id")
                    UUID.fromString(id)
                    val path = row.getString("storage_path")
                    requireValidStoragePath(path)
                    require(row.getInt("difficulty") == difficulty)
                    // Optional values may be null/blank while metadata is being curated.
                    ReferencePhoto(
                        id = id, storagePath = path, difficulty = difficulty,
                        title = row.optionalText("title"), poseType = row.optionalText("pose_type"),
                        isFullBody = if (row.isNull("is_full_body")) null else row.getBoolean("is_full_body"),
                        attribution = row.optionalText("attribution"), license = row.optionalText("license")
                    )
                }
                require(photos.map { it.id }.distinct().size == photos.size)
                return PhotoPage(photos, if (records.length() > PAGE_SIZE) photos.last().id else null)
            } catch (_: JSONException) {
                throw InvalidCatalogException()
            } catch (_: IllegalArgumentException) {
                throw InvalidCatalogException()
            }
        }
    }
}

private fun JSONObject.optionalText(key: String): String? =
    if (isNull(key)) null else getString(key).trim().takeIf { it.isNotEmpty() }

private fun requireValidStoragePath(path: String) {
    require(path.isNotBlank() && !path.startsWith('/') && '\\' !in path)
    require(path.none { it.isISOControl() })
    require(path.split('/').none { it.isEmpty() || it == "." || it == ".." })
}

fun publicPhotoUrl(baseUrl: String, storagePath: String): String {
    requireValidStoragePath(storagePath)
    return baseUrl.trimEnd('/').toHttpUrl().newBuilder()
        .addPathSegments("storage/v1/object/public/references")
        .apply { storagePath.split('/').forEach { addPathSegment(it) } }
        .build().toString()
}

private class CatalogHttpClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    suspend fun get(request: Request): String = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val result = runCatching {
                    response.use {
                        if (!it.isSuccessful) throw CatalogHttpException(it.code)
                        it.body?.string() ?: throw InvalidCatalogException()
                    }
                }
                if (continuation.isActive) continuation.resumeWith(result)
            }
        })
    }
}
