package com.example.richlinkpreviewdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.richlinkpreviewdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {

    private val AGENT = "Mozilla"
    private val REFERRER = "http://www.google.com"
    private val TIMEOUT = 10000
    private val DOC_SELECT_QUERY = "meta[property^=og:]"
    private val OPEN_GRAPH_KEY = "content"
    private val PROPERTY = "property"
    private val OG_IMAGE = "og:image"
    private val OG_DESCRIPTION = "og:description"
    private val OG_URL = "og:url"
    private val OG_TITLE = "og:title"
    private val OG_SITE_NAME = "og:site_name"
    private val OG_TYPE = "og:type"

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtLink.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                GlobalScope.launch(Dispatchers.IO) {
                    extractLinkData(p0.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    fun extractLinkData(link: String) {
        var url = link
        if (!url.contains("http")) {
            url = "http://$url"
        }

        val openGraphResult = OpenGraphResult()

        try {
            val response = Jsoup.connect(url)
                .ignoreContentType(true)
                .userAgent(AGENT)
                .referrer(REFERRER)
                .timeout(TIMEOUT)
                .followRedirects(true)
                .execute()

            val doc = response.parse()

            val ogTags = doc.select(DOC_SELECT_QUERY)
            when {
                ogTags.size > 0 ->
                    ogTags.forEachIndexed { index, _ ->
                        val tag = ogTags[index]
                        val text = tag.attr(PROPERTY)

                        when (text) {
                            OG_IMAGE -> {
                                openGraphResult!!.image = (tag.attr(OPEN_GRAPH_KEY))
                            }
                            OG_DESCRIPTION -> {
                                openGraphResult!!.description = (tag.attr(OPEN_GRAPH_KEY))
                            }
                            OG_URL -> {
                                openGraphResult!!.url = (tag.attr(OPEN_GRAPH_KEY))
                            }
                            OG_TITLE -> {
                                openGraphResult!!.title = (tag.attr(OPEN_GRAPH_KEY))
                            }
                            OG_SITE_NAME -> {
                                openGraphResult!!.siteName = (tag.attr(OPEN_GRAPH_KEY))
                            }
                            OG_TYPE -> {
                                openGraphResult!!.type = (tag.attr(OPEN_GRAPH_KEY))
                            }
                        }
                    }

            }
            GlobalScope.launch(Dispatchers.Main) {
                Glide.with(baseContext).load(openGraphResult.image).into(binding.img)
            }
        } catch (e: Exception) {
            e.printStackTrace()
//                Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
//                listener.onError(e.localizedMessage)
            }
    }
}