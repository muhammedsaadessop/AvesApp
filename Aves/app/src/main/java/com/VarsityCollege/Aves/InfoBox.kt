package com.VarsityCollege.Aves

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.gson.Gson

class InfoBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
// this class populates the hotspots and info of other data

    private val birdDescriptionTextView: TextView
    private val sciNameTextView: TextView
    private val locNameTextView: TextView
    private val dateSightedTextView: TextView
    private val closeButton: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.description_bird, this, true)


        birdDescriptionTextView = findViewById(R.id.birdDescriptionTextView)
        sciNameTextView = findViewById(R.id.sciNameTextView)
        locNameTextView = findViewById(R.id.locNameTextView)
        dateSightedTextView = findViewById(R.id.dateSightedTextView)
        closeButton = findViewById(R.id.closeButton)


        closeButton.setOnClickListener {
            hideInfoBox()
        }


        hideInfoBox()
    }

    fun showInfoBox(jsonData: String) {

        val data = Gson()
        val hotspotData = data.fromJson(jsonData, HotspotData::class.java)



        sciNameTextView.text = "Scientific Name: ${hotspotData.sciName ?: "N/A"}"
        locNameTextView.text = "Location Name: ${hotspotData.locName ?: "N/A"}"
        dateSightedTextView.text = "Date: ${hotspotData.obsDt ?: "N/A"}"


        visibility = VISIBLE
    }

    fun hideInfoBox() {

        visibility = GONE
    }

    data class HotspotData(
        val comName: String?,
        val sciName: String?,
        val locName: String?,
        val obsDt: String?
    )

}