import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable

class RepeatingNinePatchDrawable(
    private val bitmap: Bitmap
) : Drawable() {

    private val paint = Paint()

    // Updated sizes for corners and edges based on your feedback
    private var cornerSize = 168
    private var edgeThickness = 126

    init {
        // Disable anti-aliasing and filtering to preserve pixel art
        paint.isAntiAlias = false
        paint.isFilterBitmap = false
        paint.isDither = false
    }

    constructor(bitmap: Bitmap, cornerSize: Int, edgeThickness: Int) : this(bitmap) {
        this.cornerSize = cornerSize
        this.edgeThickness = edgeThickness
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds

        // 1. Draw center first
        drawCenter(canvas, bounds)

        // 2. Draw edges (without overlapping corners)
        drawEdges(canvas, bounds)

        // 3. Draw corners last (to ensure nothing overwrites them)
        drawCorners(canvas, bounds)
    }

    private fun drawCenter(canvas: Canvas, bounds: Rect) {
        // Tile center part first (without overlapping the edges)
        val center = Rect(cornerSize, cornerSize, cornerSize + edgeThickness, cornerSize + edgeThickness)
        for (x in cornerSize until (bounds.right - cornerSize) step edgeThickness) {
            for (y in cornerSize until (bounds.bottom - cornerSize) step edgeThickness) {
                val dest = Rect(x, y, x + edgeThickness, y + edgeThickness)
                canvas.drawBitmap(bitmap, center, dest, paint)
            }
        }
    }

    private fun drawEdges(canvas: Canvas, bounds: Rect) {
        // Tile top edge (without overlapping corners)
        val topEdge = Rect(cornerSize, 0, cornerSize + edgeThickness, cornerSize)
        for (x in cornerSize until (bounds.right - cornerSize) step edgeThickness) {
            val dest = Rect(x, bounds.top, x + edgeThickness, bounds.top + cornerSize)
            canvas.drawBitmap(bitmap, topEdge, dest, paint)
        }

        // Tile bottom edge (without overlapping corners)
        val bottomEdge = Rect(cornerSize, bitmap.height - cornerSize, cornerSize + edgeThickness, bitmap.height)
        for (x in cornerSize until (bounds.right - cornerSize) step edgeThickness) {
            val dest = Rect(x, bounds.bottom - cornerSize, x + edgeThickness, bounds.bottom)
            canvas.drawBitmap(bitmap, bottomEdge, dest, paint)
        }

        // Tile left edge (without overlapping corners)
        val leftEdge = Rect(0, cornerSize, cornerSize, cornerSize + edgeThickness)
        for (y in cornerSize until (bounds.bottom - cornerSize) step edgeThickness) {
            val dest = Rect(bounds.left, y, bounds.left + cornerSize, y + edgeThickness)
            canvas.drawBitmap(bitmap, leftEdge, dest, paint)
        }

        // Tile right edge (without overlapping corners)
        val rightEdge = Rect(bitmap.width - cornerSize, cornerSize, bitmap.width, cornerSize + edgeThickness)
        for (y in cornerSize until (bounds.bottom - cornerSize) step edgeThickness) {
            val dest = Rect(bounds.right - cornerSize, y, bounds.right, y + edgeThickness)
            canvas.drawBitmap(bitmap, rightEdge, dest, paint)
        }
    }

    private fun drawCorners(canvas: Canvas, bounds: Rect) {
        // Top-left corner
        val topLeftCorner = Rect(0, 0, cornerSize, cornerSize)
        canvas.drawBitmap(bitmap, topLeftCorner, Rect(bounds.left, bounds.top, bounds.left + cornerSize, bounds.top + cornerSize), paint)

        // Top-right corner
        val topRightCorner = Rect(bitmap.width - cornerSize, 0, bitmap.width, cornerSize)
        canvas.drawBitmap(bitmap, topRightCorner, Rect(bounds.right - cornerSize, bounds.top, bounds.right, bounds.top + cornerSize), paint)

        // Bottom-left corner
        val bottomLeftCorner = Rect(0, bitmap.height - cornerSize, cornerSize, bitmap.height)
        canvas.drawBitmap(bitmap, bottomLeftCorner, Rect(bounds.left, bounds.bottom - cornerSize, bounds.left + cornerSize, bounds.bottom), paint)

        // Bottom-right corner
        val bottomRightCorner = Rect(bitmap.width - cornerSize, bitmap.height - cornerSize, bitmap.width, bitmap.height)
        canvas.drawBitmap(bitmap, bottomRightCorner, Rect(bounds.right - cornerSize, bounds.bottom - cornerSize, bounds.right, bounds.bottom), paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}