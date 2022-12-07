package com.example.spotifyexplained.general

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class GestureListener(val onExpandClick: (Boolean) -> Unit) : GestureDetector.SimpleOnGestureListener() {
    private val velocityThreshold = 10

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(velocityY) > velocityThreshold) {
            if (e2.y > 0) {
                onExpandClick(false)
            } else {
                onExpandClick(true)
            }
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }
}