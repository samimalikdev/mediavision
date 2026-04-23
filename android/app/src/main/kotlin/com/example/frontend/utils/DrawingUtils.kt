package com.example.frontend.utils

import android.graphics.*

object DrawStyle {
    val HAND_CONNECTIONS = listOf(
        0 to 1, 1 to 2, 2 to 3, 3 to 4,
        0 to 5, 5 to 6, 6 to 7, 7 to 8,
        0 to 9, 9 to 10, 10 to 11, 11 to 12,
        0 to 13, 13 to 14, 14 to 15, 15 to 16,
        0 to 17, 17 to 18, 18 to 19, 19 to 20,
        5 to 9, 9 to 13, 13 to 17
    )

    val POSE_CONNECTIONS = listOf(
        11 to 12 to "#F59E0B",
        11 to 23 to "#A78BFA",
        12 to 24 to "#A78BFA",
        23 to 24 to "#A78BFA",
        11 to 13 to "#34D399",
        13 to 15 to "#34D399",
        12 to 14 to "#34D399",
        14 to 16 to "#34D399",
        23 to 25 to "#60A5FA",
        25 to 27 to "#60A5FA",
        27 to 29 to "#60A5FA",
        27 to 31 to "#60A5FA",
        24 to 26 to "#60A5FA",
        26 to 28 to "#60A5FA",
        28 to 30 to "#60A5FA",
        28 to 32 to "#60A5FA"
    )

    val FACE_REGIONS = mapOf(
        "nose" to listOf(1, 2, 5, 4, 195, 197, 6, 168, 8, 9)
    )

    val FACE_REGION_COLORS = mapOf(
        "nose" to Color.parseColor("#F59E0B")
    )
    val FACE_REGION_RADIUS = mapOf(
        "nose" to 3.5f
    )
}

fun linePaint(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    strokeWidth = width
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
}

fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    style = Paint.Style.FILL
}

fun jointPaint(borderColor: Int, fillColor: Int = Color.WHITE) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = fillColor
        style = Paint.Style.FILL
    } to Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

fun Canvas.drawPill(text: String, cx: Float, cy: Float, bgColor: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }
    val textWidth = paint.measureText(text)
    val padH = 18f
    val padV = 10f
    val half = textWidth / 2f + padH
    val top = cy - padV - 14f
    val bot = cy + padV + 2f

    drawRoundRect(RectF(cx - half + 2, top + 2, cx + half + 2, bot + 2), 30f, 30f, fillPaint(Color.argb(80, 0, 0, 0)))
    drawRoundRect(RectF(cx - half, top, cx + half, bot), 30f, 30f, fillPaint(bgColor))
    paint.color = Color.WHITE
    drawText(text, cx - textWidth / 2f, cy, paint)
}

fun Canvas.drawStylizedBox(rect: RectF, strokeColor: Int, accentLen: Float = 28f) {
    val fillPaint = fillPaint(Color.argb(20, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor)))
    drawRoundRect(rect, 8f, 8f, fillPaint)
    val p = linePaint(strokeColor, 3.5f)
    drawLine(rect.left, rect.top + accentLen, rect.left, rect.top, p)
    drawLine(rect.left, rect.top, rect.left + accentLen, rect.top, p)
    drawLine(rect.right - accentLen, rect.top, rect.right, rect.top, p)
    drawLine(rect.right, rect.top, rect.right, rect.top + accentLen, p)
    drawLine(rect.left, rect.bottom - accentLen, rect.left, rect.bottom, p)
    drawLine(rect.left, rect.bottom, rect.left + accentLen, rect.bottom, p)
    drawLine(rect.right - accentLen, rect.bottom, rect.right, rect.bottom, p)
    drawLine(rect.right, rect.bottom, rect.right, rect.bottom - accentLen, p)
}

fun Canvas.drawLabelBadge(text: String, x: Float, y: Float, bgColor: Int) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
    }
    val tw = paint.measureText(text)
    val padH = 12f; val padV = 6f
    drawRoundRect(RectF(x, y - padV - 11f, x + tw + padH * 2, y + padV - 2f), 20f, 20f, fillPaint(bgColor))
    paint.color = Color.WHITE
    drawText(text, x + padH, y, paint)
}
