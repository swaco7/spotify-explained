package com.example.spotifyexplained.model

import kotlin.math.abs


class MixableColor(
    var r : Int,
    var g: Int,
    var b: Int,
    var a: Double
) {
    fun mix (color: MixableColor) : MixableColor {
        val alpha = (a + color.a) / 2
        val red = ((r + color.r) / 2) + ((a - color.a) * (abs(r - color.r) / 2))
        val green = ((g + color.g) / 2) + ((a - color.a) * (abs(g - color.g) / 2))
        val blue = ((b + color.b) / 2) + ((a - color.a) * (abs(b - color.b) / 2))
        return MixableColor(red.toInt(), green.toInt(), blue.toInt(), alpha)
    }
}