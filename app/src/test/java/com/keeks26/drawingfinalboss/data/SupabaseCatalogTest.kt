package com.keeks26.drawingfinalboss.data

import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class SupabaseCatalogTest {
    private val base = "https://example.supabase.co"

    @Test fun `request filters published level and sends publishable key without bearer`() = runBlocking {
        var sent: Request? = null
        val catalog = SupabaseCatalog(base, "sb_publishable_test", MemoryCache()) {
            sent = it
            "[]"
        }
        catalog.loadPage(2, id(12))
        val request = requireNotNull(sent)
        assertEquals("/rest/v1/reference_photos", request.url.encodedPath)
        assertEquals("eq.published", request.url.queryParameter("status"))
        assertEquals("eq.2", request.url.queryParameter("difficulty"))
        assertEquals("gt.${id(12)}", request.url.queryParameter("id"))
        assertEquals("25", request.url.queryParameter("limit"))
        assertEquals("id.asc", request.url.queryParameter("order"))
        assertEquals("sb_publishable_test", request.header("apikey"))
        assertNull(request.header("Authorization"))
    }

    @Test fun `URL preserves capitalization and encodes spaces and reserved characters`() {
        assertEquals(
            "$base/storage/v1/object/public/references/Boss%20Level/pose%20%231.jpg",
            publicPhotoUrl(base, "Boss Level/pose #1.jpg")
        )
        assertTrue(publicPhotoUrl(base, "Level-1/1.jpg").endsWith("/Level-1/1.jpg"))
        for (path in listOf("", "/1.jpg", "../1.jpg", "a/../1.jpg", "a//1.jpg", "a\\1.jpg")) {
            assertThrows(IllegalArgumentException::class.java) { publicPhotoUrl(base, path) }
        }
    }

    @Test fun `lookahead is not displayed and supplies cursor for next page`() {
        val json = JSONArray((1..25).map { row(it) }).toString()
        val page = SupabaseCatalog.decodePage(json, 1)
        assertEquals(24, page.photos.size)
        assertEquals(id(24), page.nextCursor)
        assertEquals(id(1), page.photos.first().id)
        val lastPage = SupabaseCatalog.decodePage(JSONArray(listOf(row(25))).toString(), 1)
        assertNull(lastPage.nextCursor)
    }

    @Test fun `null metadata is preserved without inventing labels`() {
        val row = row(1).put("title", JSONObject.NULL).put("pose_type", "   ")
            .put("is_full_body", JSONObject.NULL).put("license", JSONObject.NULL)
        val photo = SupabaseCatalog.decodePage(JSONArray(listOf(row)).toString(), 1).photos.single()
        assertNull(photo.title)
        assertNull(photo.poseType)
        assertNull(photo.isFullBody)
        assertEquals("Drawing reference", photo.description)
        assertEquals("", photo.caption)
    }

    @Test fun `invalid paths mismatched levels and invalid JSON are rejected`() {
        for (json in listOf(
            "<html>error</html>",
            JSONArray(listOf(row(1).put("storage_path", "../private.jpg"))).toString(),
            JSONArray(listOf(row(1).put("difficulty", 3))).toString(),
            JSONArray(listOf(row(1), row(1))).toString()
        )) {
            assertThrows(InvalidCatalogException::class.java) { SupabaseCatalog.decodePage(json, 1) }
        }
    }

    @Test fun `offline first page falls back to cache but pagination does not repeat cache`() = runBlocking {
        val cache = MemoryCache().apply { write(1, JSONArray(listOf(row(1))).toString()) }
        val catalog = SupabaseCatalog(base, "sb_publishable_test", cache) { throw IOException("offline") }
        val page = catalog.loadPage(1)
        assertTrue(page.fromCache)
        assertEquals(1, page.photos.size)
        assertNull(page.nextCursor)
        try {
            catalog.loadPage(1, id(1))
            fail("Pagination should report offline instead of repeating cached photos")
        } catch (_: IOException) { }
    }

    @Test fun `successful empty response replaces stale cache and authorization failure is visible`() = runBlocking {
        val cache = MemoryCache().apply { write(1, JSONArray(listOf(row(1))).toString()) }
        val online = SupabaseCatalog(base, "sb_publishable_test", cache) { "[]" }
        assertTrue(online.loadPage(1).photos.isEmpty())
        assertEquals("[]", cache.read(1))
        val denied = SupabaseCatalog(base, "sb_publishable_test", cache) { throw CatalogHttpException(403) }
        try {
            denied.loadPage(1)
            fail("Authorization failures must not be hidden by cached data")
        } catch (error: CatalogHttpException) { assertEquals(403, error.status) }
    }

    @Test fun `cache write failure does not discard a successful response`() = runBlocking {
        val brokenCache = object : CatalogCache {
            override fun read(difficulty: Int): String? = null
            override fun write(difficulty: Int, json: String) { throw IOException("disk full") }
        }
        val catalog = SupabaseCatalog(base, "sb_publishable_test", brokenCache) {
            JSONArray(listOf(row(1))).toString()
        }
        assertEquals(1, catalog.loadPage(1).photos.size)
    }

    private fun id(number: Int) = "00000000-0000-0000-0000-${number.toString().padStart(12, '0')}"
    private fun row(number: Int) = JSONObject().put("id", id(number))
        .put("storage_path", "Level-1/$number.jpg").put("difficulty", 1)

    private class MemoryCache : CatalogCache {
        private val entries = mutableMapOf<Int, String>()
        override fun read(difficulty: Int): String? = entries[difficulty]
        override fun write(difficulty: Int, json: String) { entries[difficulty] = json }
    }
}
