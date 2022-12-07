package com.example.spotifyexplained.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotifyexplained.R
import com.google.android.material.slider.Slider
import de.hdodenhof.circleimageview.CircleImageView


@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
        Glide.with(view)
            .load("$url")
            .fallback(R.drawable.placeholder_image)
            .placeholder(R.drawable.placeholder_image)
            .centerCrop()
            .into(view)
}

@BindingAdapter("circleImageUrl")
fun loadCircleImage(view: CircleImageView, url: String?) {
    Glide.with(view)
        .load("$url")
        .fallback(R.drawable.placeholder_image)
        .placeholder(R.drawable.placeholder_image)
        .centerCrop()
        .into(view)
}

@BindingAdapter("imageColor")
fun loadImageColor(view: ImageView, color: String?) {
    view.setBackgroundColor(Color.parseColor(color))
}

@BindingAdapter("circleSrc")
fun loadCircleImageColor(view: CircleImageView, color: ArrayList<Float>) {
    val hex = java.lang.String.format("#%02X%02X%02X%02X", (color[0]*255).toInt(), color[1].toInt(), color[2].toInt(), color[3].toInt())
    view.setImageDrawable(ColorDrawable(Color.parseColor(hex)))
}

@BindingAdapter("adapter")
fun setRecyclerViewAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?) {
    view.adapter = adapter
}

@BindingAdapter("onValueChangeListener")
fun setOnValueChangeListener(slider: Slider, listener: OnValueChangeListener) {
    slider.addOnChangeListener { _: Slider?, value: Float, _: Boolean ->
        listener.onValueChanged(value)
    }
}


@BindingAdapter(
    value = [
        "textViewLabel",
        "valueFormat"
    ], requireAll = false
)
fun setTextViewLabel(
    slider: Slider,
    textViewLabel: TextView,
    valueFormat: String?
) {
    slider.addOnChangeListener { _: Slider?, value: Float, _: Boolean ->
        if (valueFormat != null) {
            valueFormat
                .let { format ->
                    textViewLabel.text = String.format(format, value)
                }
        } else {
            textViewLabel.text = value.toString()
        }
    }
}



@BindingAdapter("onMyLongClick")
fun setMyOnLongClickListener(
    view: View,
    func : () -> Unit
) {
    view.setOnLongClickListener {
        func()
        return@setOnLongClickListener true
    }
}

interface OnValueChangeListener {
    fun onValueChanged(value: Float)
}