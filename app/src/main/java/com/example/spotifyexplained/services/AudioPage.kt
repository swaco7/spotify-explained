package com.example.spotifyexplained.services

object AudioPage {
    fun getContent(source: String?, width: Int): String{
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<style>\n" +
                "audio { width: ${width + 80}px;}\n" +
                "</style>\n" +
                "<body>\n" +
                "\n" +
                "<audio controls autoplay>\n" +
                "  <source src=\"$source\" type=\"audio/mpeg\">\n" +
                "Your browser does not support the audio element.\n" +
                "</audio>\n" +
                "\n" +
                "</body>\n" +
                "</html>"
    }
}