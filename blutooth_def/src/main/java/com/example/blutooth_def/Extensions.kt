package com.example.blutooth_def

import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.ImageButton
import androidx.core.graphics.drawable.DrawableCompat

fun changeButtonColor(button: ImageButton, color: Int) {
    val drawable = button.drawable
    DrawableCompat.setTint(drawable, color)
    button.setImageDrawable(drawable)
}