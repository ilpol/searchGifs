package com.example.searchgifs.network

import com.beust.klaxon.Klaxon
import com.example.searchgifs.GiphyResponse
import com.example.searchgifs.data.GifItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

class Network {
    companion object {
        val client: OkHttpClient = OkHttpClient();
        fun getRequest(sUrl: String, onError: ()-> Unit): String? {
            var result: String? = null
            try {
                val url = URL(sUrl)
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                result = response.body?.string()
            } catch (err: Exception) {
                onError()
            }

            return result
        }

        fun fetchGifs(sUrl: String, callback: (List<GifItem>, Int) -> Unit, onError: ()-> Unit): GiphyResponse? {
            var giphyGifsResponse: GiphyResponse? = null
            GlobalScope.launch(Dispatchers.IO) {
                val result = getRequest(sUrl, onError)
                if (result != null) {
                    try {
                            giphyGifsResponse = Klaxon().parse<GiphyResponse>(result)
                            withContext(Dispatchers.Main) {

                            var dataArr = giphyGifsResponse?.data
                            var totalCount = 0
                            val totalCountGot = giphyGifsResponse?.pagination?.total_count
                            if (totalCountGot != null) {
                                totalCount = totalCountGot
                            }
                            val gifs = dataArr?.map { "https://media.giphy.com/media/" + it?.id + "/giphy.gif" } as List<String>

                            val titles = dataArr?.map { it?.title } as List<String>

                            val dates = dataArr?.map { it?.import_datetime } as List<String>

                            var gifItemsToSet = listOf<GifItem>()

                            titles.forEachIndexed { index, element ->
                                gifItemsToSet += GifItem(url=gifs[index], title=element, date = dates[index])
                            }

                            callback(gifItemsToSet, totalCount)
                        }
                    }
                    catch(err:Error) {
                        onError()
                    }
                }
                else {
                    onError()
                }
            }
            return giphyGifsResponse
        }
    }
}