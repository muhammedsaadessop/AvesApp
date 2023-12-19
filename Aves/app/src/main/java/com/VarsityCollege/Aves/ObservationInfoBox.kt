package com.VarsityCollege.Aves

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import androidx.cardview.widget.CardView

class ObservationInfoBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
// this class displays all the user added observations
    private val birdName: TextView
    private val familyNameTextView: TextView
    private val colour_descriptionTextView: TextView
    private val bird_DescriptionTextView: TextView
    private val colorImageView: ImageView
    private val closeButton: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.observation_infobox, this, true)
        birdName = findViewById(R.id.birdName)
        familyNameTextView = findViewById(R.id.familyNameTextView)
        colour_descriptionTextView = findViewById(R.id.colour_descriptionTextView)
        bird_DescriptionTextView = findViewById(R.id.bird_DescriptionTextView)
        colorImageView = findViewById(R.id.colorImageView)
        closeButton = findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            hideObservationInfoBox()
        }
    }

    fun show() {
        visibility = View.VISIBLE
    }

    fun hideObservationInfoBox() {
        visibility = View.GONE
    }

    fun updateData(observationData: ObservationData) {
        birdName.text = observationData.birdname
        familyNameTextView.text = observationData.familyname
        colour_descriptionTextView.text = observationData.colourdesc
        bird_DescriptionTextView.text = observationData.description
        Picasso.get().load(observationData.imageurl).into(colorImageView)

    }
}

data class ObservationData(
    val birdname: String?,
    val familyname: String?,
    val colourdesc: String?,
    val description: String?,
    val imageurl: String
)
