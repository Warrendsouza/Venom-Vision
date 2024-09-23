package com.surendramaran.yolov8tflite

import BoundingBox
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.surendramaran.yolov8tflite.bbxPoints.Companion

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var textPaint1 = Paint()
    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        textPaint.reset()
        textPaint1.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f
        textPaint1.color = Color.CYAN
        textPaint1.style = Paint.Style.FILL
        textPaint1.textSize = 50f
        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up resources when the view is detached from the window
        results = emptyList()
        // Ensure that BitmapHolder's reference is cleared to prevent memory leaks
        BitmapHolder.croppedBitmap?.recycle()
       // BitmapHolder.croppedBitmap = null
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

           /* val pts = bbxPoints()
            pts.x1=it.x1*Bit
            pts.y1=it.y1
            pts.x2=it.x2
            pts.y2=it.y2*/
            /*bbxPoints.staticX1=left
            bbxPoints.staticY1=top
            bbxPoints.staticX2=right
            bbxPoints.staticY2=bottom*/

           //to crop the image
            val croppedBitmap = Bitmap.createBitmap((it.x2*BitmapHolder.croppedBitmap.width - it.x1 * BitmapHolder.croppedBitmap.width).toInt(), (it.y2 *  BitmapHolder.croppedBitmap.height - it.y1 *BitmapHolder.croppedBitmap.height).toInt(), Bitmap.Config.ARGB_8888)
            val croppedCanvas = Canvas(croppedBitmap)
            croppedCanvas.drawBitmap(BitmapHolder.croppedBitmap, -(it.x1 * BitmapHolder.croppedBitmap.width), -(it.y1 * BitmapHolder.croppedBitmap.height), null)
            val resizebitmap=resizeBitmap(croppedBitmap,224,224)

            // Copy the cropped Bitmap to the finalBitmap.croppedBitmap variable
            //finalBitmap.croppedBitmap = croppedBitmap
            finalBitmap.croppedBitmap = resizebitmap






            canvas.drawRect(left, top, right, bottom, boxPaint)
            val drawableText = it.clsName
            val consfscore = it.cnf.toString() //confscore
            bbxPoints.clabel=drawableText
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left, top, left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING, textBackgroundPaint
            )
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
            canvas.drawText(consfscore, left + 10, top + bounds.height() - 40, textPaint1)
           // saveRegionAsImage(left.toInt(), top.toInt(), right.toInt(), bottom.toInt(), canvas)

        }
    }
    fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun saveRegionAsImage(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas) {
        val bitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.RGB_565)
        val regionCanvas = Canvas(bitmap)
        regionCanvas.drawBitmap(bitmap, 0f, 0f, null)
        regionCanvas.clipRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        // Draw bounding boxes and text directly on the regionCanvas
        results.forEach {
            if (it.x1 * width >= left && it.x2 * width <= right && it.y1 * height >= top && it.y2 * height <= bottom) {
                val boundingBoxLeft = (it.x1 * width - left).toFloat()
                val boundingBoxTop = (it.y1 * height - top).toFloat()
                val boundingBoxRight = (it.x2 * width - left).toFloat()
                val boundingBoxBottom = (it.y2 * height - top).toFloat()
                regionCanvas.drawRect(boundingBoxLeft, boundingBoxTop, boundingBoxRight, boundingBoxBottom, boxPaint)
                val drawableText = it.clsName
                val consfscore = it.cnf.toString()
                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
                val textWidth = bounds.width()
                val textHeight = bounds.height()
                regionCanvas.drawRect(
                    boundingBoxLeft, boundingBoxTop, boundingBoxLeft + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    boundingBoxTop + textHeight + BOUNDING_RECT_TEXT_PADDING, textBackgroundPaint
                )
                regionCanvas.drawText(drawableText, boundingBoxLeft, boundingBoxTop + bounds.height(), textPaint)
                regionCanvas.drawText(consfscore, boundingBoxLeft + 10, boundingBoxTop + bounds.height() - 40, textPaint1)
            }
        }

        // Pass the bitmap to BitmapHolder
       /* val scaleFactor = 0.60f // You can adjust the scale factor as needed
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scaleFactor).toInt(), (bitmap.height * scaleFactor).toInt(), true)
        BitmapHolder.croppedBitmap = bitmap*/

    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}