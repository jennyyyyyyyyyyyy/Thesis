package thesis.filconnected

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: GestureRecognizerResult? = null
    private val linePaint = Paint()
    private val pointPaint = Paint()
    private val borderPaint = Paint()
    private val textPaint = Paint()
    private val guidePaint = Paint() // New paint for the human figure guide
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private val precomputedLines = mutableListOf<Pair<Float, Float>>()
    private var isPoseAligned = false
    private var poseLandmarks: List<NormalizedLandmark>? = null

    // Bitmap for the human figure guide
    private var humanFigureBitmap: Bitmap? = null

    init {
        initPaints()
        loadHumanFigureGuide()
    }

    fun clear() {
        results = null
        poseLandmarks = null
        precomputedLines.clear()
        invalidate()
    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.MAGENTA
        pointPaint.strokeWidth = LANDMARK_POINT_SIZE
        pointPaint.style = Paint.Style.FILL

        borderPaint.color = Color.BLACK
        borderPaint.strokeWidth = BORDER_STROKE_WIDTH
        borderPaint.style = Paint.Style.STROKE

        textPaint.color = Color.WHITE
        textPaint.textSize = 50f
        textPaint.style = Paint.Style.FILL

        // Initialize paint for the human figure guide
        guidePaint.alpha = 100 // Set opacity (semi-transparent)
        guidePaint.isAntiAlias = true
    }

    private fun loadHumanFigureGuide() {
        // Load the human figure bitmap from resources
        humanFigureBitmap = BitmapFactory.decodeResource(resources, R.drawable.human_figure_guide)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the human figure guide if pose is not aligned
        if (!isPoseAligned && humanFigureBitmap != null) {
            // Define the scaling factor for the image
            val scale = 1.5f // Increase this value to make the image larger
            // Calculate the scaled width and height
            val scaledWidth = humanFigureBitmap!!.width * scale
            val scaledHeight = humanFigureBitmap!!.height * scale
            // Calculate the position to keep the image centered horizontally but lower vertically
            val centerX = (width - scaledWidth) / 2
            val centerYOffset = height * 0.2f // Adjust this value to move the image lower (e.g., 30% from the top)
            val centerY = centerYOffset
            // Create a matrix to apply scaling and translation
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            matrix.postTranslate(centerX, centerY)
            // Draw the scaled bitmap using the matrix
            canvas.drawBitmap(humanFigureBitmap!!, matrix, guidePaint)
        }

//        // Draw gesture recognition results (if any)
//        results?.let {
//            for (i in precomputedLines.indices step 2) {
//                val start = precomputedLines[i]
//                val end = precomputedLines[i + 1]
//                canvas.drawLine(start.first, start.second, end.first, end.second, linePaint)
//            }
//            for (landmarkList in it.landmarks()) {
//                for (landmark in landmarkList) {
//                    val x = landmark.x()
//                    val y = landmark.y()
//                    val scaledX = x * imageWidth * scaleFactor
//                    val scaledY = y * imageHeight * scaleFactor
//                    canvas.drawCircle(scaledX, scaledY, LANDMARK_RADIUS, borderPaint)
//                    canvas.drawCircle(scaledX, scaledY, LANDMARK_RADIUS - BORDER_STROKE_WIDTH / 2, pointPaint)
//                }
//            }
//        }

//        // Draw gesture recognition results (if any)
//        results?.let {
//            for (i in precomputedLines.indices step 2) {
//                val start = precomputedLines[i]
//                val end = precomputedLines[i + 1]
//                canvas.drawLine(start.first, start.second, end.first, end.second, linePaint)
//            }
//            for (landmarkList in it.landmarks()) {
//                for (landmark in landmarkList) {
//                    val x = landmark.x()
//                    val y = landmark.y()
//                    val scaledX = x * imageWidth * scaleFactor
//                    val scaledY = y * imageHeight * scaleFactor
//                    canvas.drawCircle(scaledX, scaledY, LANDMARK_RADIUS, borderPaint)
//                    canvas.drawCircle(scaledX, scaledY, LANDMARK_RADIUS - BORDER_STROKE_WIDTH / 2, pointPaint)
//                }
//            }
//        }

//        // Draw pose landmarks (if any)
//        poseLandmarks?.let { landmarks ->
//            val faceConnections = listOf(
//                Pair(0, 1), Pair(0, 2), Pair(1, 3), Pair(2, 4)
//            )
//            for ((startIdx, endIdx) in faceConnections) {
//                val startX = (1 - landmarks[startIdx].x()) * imageWidth * scaleFactor
//                val startY = landmarks[startIdx].y() * imageHeight * scaleFactor
//                val endX = (1 - landmarks[endIdx].x()) * imageWidth * scaleFactor
//                val endY = landmarks[endIdx].y() * imageHeight * scaleFactor
//                canvas.drawLine(startX, startY, endX, endY, linePaint)
//            }
//            val faceLandmarkIndices = listOf(0, 1, 2, 3, 4)
//            for (index in faceLandmarkIndices) {
//                val landmark = landmarks[index]
//                val x = (1 - landmark.x()) * imageWidth * scaleFactor
//                val y = landmark.y() * imageHeight * scaleFactor
//                canvas.drawCircle(x, y, LANDMARK_RADIUS, borderPaint)
//                canvas.drawCircle(x, y, LANDMARK_RADIUS - BORDER_STROKE_WIDTH / 2, pointPaint)
//            }
//        }

        // Draw pose alignment state
        if (isPoseAligned) {
            textPaint.color = Color.GREEN
            canvas.drawText("Start the hand gesture", 50f, 50f, textPaint)
        } else {
            textPaint.color = Color.RED
            canvas.drawText("Position the camera", 50f, 50f, textPaint)
        }
    }

    fun setResults(
        gestureRecognizerResult: GestureRecognizerResult,
        poseLandmarks: List<NormalizedLandmark>?,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = gestureRecognizerResult
        this.poseLandmarks = poseLandmarks
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> min(width * 1f / imageWidth, height * 1f / imageHeight)
            RunningMode.LIVE_STREAM -> max(width * 1f / imageWidth, height * 1f / imageHeight)
        }
        precomputedLines.clear()
        gestureRecognizerResult.landmarks().forEach { landmarks ->
            HandLandmarker.HAND_CONNECTIONS.forEach {
                val startX = (landmarks[it.start()].x()) * imageWidth * scaleFactor
                val startY = landmarks[it.start()].y() * imageHeight * scaleFactor
                val endX = (landmarks[it.end()].x()) * imageWidth * scaleFactor
                val endY = landmarks[it.end()].y() * imageHeight * scaleFactor
                precomputedLines.add(startX to startY)
                precomputedLines.add(endX to endY)
            }
        }
        invalidate()
    }

    fun setPoseAligned(isAligned: Boolean) {
        this.isPoseAligned = isAligned
        invalidate() // Trigger redraw
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
        private const val LANDMARK_POINT_SIZE = 20F
        private const val LANDMARK_RADIUS = 12F
        private const val BORDER_STROKE_WIDTH = 4F
    }
}
