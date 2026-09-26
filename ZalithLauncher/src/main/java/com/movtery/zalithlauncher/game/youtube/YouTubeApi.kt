package com.movtery.zalithlauncher.game.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object YouTubeApi {
    private const val CHANNEL_ID = "UCYrhGdSilHh7pg3C7o_E3UA"
    private const val RSS_URL = "https://www.youtube.com/feeds/videos.xml?channel_id=$CHANNEL_ID"

    private val client = OkHttpClient()

    suspend fun fetchVideos(): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(RSS_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(InputSource(StringReader(body)))
            val entries = doc.getElementsByTagName("entry")

            val videos = mutableListOf<YouTubeVideo>()
            for (i in 0 until entries.length) {
                val entry = entries.item(i) as Element
                val videoId = entry.getElementsByTagName("yt:videoId").item(0)?.textContent ?: continue
                val title = entry.getElementsByTagName("title").item(0)?.textContent ?: ""
                val published = entry.getElementsByTagName("published").item(0)?.textContent ?: ""
                val thumbnail = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                val url = "https://www.youtube.com/watch?v=$videoId"

                videos.add(
                    YouTubeVideo(
                        id = videoId,
                        title = title,
                        published = published,
                        thumbnailUrl = thumbnail,
                        videoUrl = url
                    )
                )
            }
            videos
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
