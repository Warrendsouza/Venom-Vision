package com.surendramaran.yolov8tflite

data class Recog(
    var id: String = "",
    var title: String = "",
    var confidence: Float = 0F
) {
    override fun toString(): String {
        return "Title = $title, Confidence = $confidence)"
    }
}
