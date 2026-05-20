package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import kotlin.math.floor

object PdfGenerator {

    // A4 sheet standard details (300 DPI):
    // 210mm x 297mm
    // 1 mm = 11.811 pixels (at 300 DPI)
    const val DPI = 300f
    const val MM_TO_PX = 300f / 25.4f

    const val PAGE_WIDTH_MM = 210f
    const val PAGE_HEIGHT_MM = 297f
    const val MARGIN_MM = 15f // 1.5 cm safe printable margin

    data class PrintPhoto(
        val bitmap: Bitmap,
        val copies: Int
    )

    data class LayoutSettings(
        val widthMm: Float,
        val heightMm: Float,
        val spacingMm: Float,
        val borderMm: Float,
        val layoutMode: String, // "grid" or "line"
        val gridAlignment: String // "normal", "right_blank", "left_blank", "lower_right_blank", "lower_left_blank"
    )

    fun generatePdf(
        context: Context,
        photos: List<PrintPhoto>,
        settings: LayoutSettings,
        outputFile: File
    ): File {
        // CRITICAL SECURED CREDIT VERIFICATION CHECK
        CreditSafetyCheck.performIntegrityCheck()

        val pdfDocument = PdfDocument()

        val pageWidthPx = floor(PAGE_WIDTH_MM * MM_TO_PX).toInt()
        val pageHeightPx = floor(PAGE_HEIGHT_MM * MM_TO_PX).toInt()

        val photoWidthPx = (settings.widthMm * MM_TO_PX).toInt()
        val photoHeightPx = (settings.heightMm * MM_TO_PX).toInt()
        val spacingPx = (settings.spacingMm * MM_TO_PX).toInt()
        val borderPx = (settings.borderMm * MM_TO_PX).toInt()
        val marginPx = (MARGIN_MM * MM_TO_PX).toInt()

        val printableWidthPx = pageWidthPx - 2 * marginPx
        val printableHeightPx = pageHeightPx - 2 * marginPx

        // Flatten all photos matching their desired copyship metrics
        val allCopies = mutableListOf<Bitmap>()
        for (photo in photos) {
            repeat(photo.copies) {
                allCopies.add(photo.bitmap)
            }
        }

        val totalImages = allCopies.size
        if (totalImages == 0) {
            // Generate empty page to prevent failures
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)
            
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 36f
                isAntiAlias = true
            }
            canvas.drawText("No Passport Photos Selected.", 100f, 100f, paint)
            pdfDocument.finishPage(page)
            
            val fos = FileOutputStream(outputFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            return outputFile
        }

        var currentImageIndex = 0
        var pageNum = 1

        val paintBorder = Paint().apply {
            color = Color.rgb(180, 180, 180) // Exact Light Gray specification: RGB (180, 180, 180)
            style = Paint.Style.STROKE
            strokeWidth = borderPx.toFloat().coerceAtLeast(1.5f)
            isAntiAlias = true
        }

        val paintCredit = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 28f
            isAntiAlias = true
        }

        while (currentImageIndex < totalImages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paint clean white print page
            canvas.drawColor(Color.WHITE)

            // Dynamic credits footer at bottom of sheet - system protection requirements
            val creditAuthor = CreditSafetyCheck.AUTHOR
            val userTelegram = CreditSafetyCheck.TELEGRAM_USER
            val creditFooter = "Generated via Passport Photo Pro ($creditAuthor) | Feedback & Support: Telegram @$userTelegram"
            canvas.drawText(creditFooter, marginPx.toFloat(), (pageHeightPx - 35).toFloat(), paintCredit)

            if (settings.layoutMode == "grid") {
                // Fixed 3 column layout
                val colCount = 3
                val gridTotalWidthPx = colCount * photoWidthPx + (colCount - 1) * spacingPx
                val startXPx = marginPx + (printableWidthPx - gridTotalWidthPx) / 2

                val rowHeightPx = photoHeightPx + spacingPx
                // Available rows that fit safely
                val maxRows = floor((printableHeightPx - 50f + spacingPx).toDouble() / rowHeightPx).toInt().coerceAtLeast(1)

                for (row in 0 until maxRows) {
                    if (currentImageIndex >= totalImages) break
                    for (col in 0 until colCount) {
                        if (currentImageIndex >= totalImages) break

                        // Skip grid cells depending on Alignment configuration
                        var skipThisCell = false
                        when (settings.gridAlignment) {
                            "right_blank" -> {
                                if (col == 2) skipThisCell = true
                            }
                            "left_blank" -> {
                                if (col == 0) skipThisCell = true
                            }
                            "lower_left_blank" -> {
                                if (row == maxRows - 1 && col == 0) skipThisCell = true
                            }
                            "lower_right_blank" -> {
                                if (row == maxRows - 1 && col == 2) skipThisCell = true
                            }
                        }

                        if (skipThisCell) {
                            continue // Leave slot blank and carry on flowing copies
                        }

                        val x = startXPx + col * (photoWidthPx + spacingPx)
                        val y = marginPx + row * (photoHeightPx + spacingPx)

                        val destRect = Rect(x, y, x + photoWidthPx, y + photoHeightPx)
                        val bitmap = allCopies[currentImageIndex]
                        canvas.drawBitmap(bitmap, null, destRect, null)

                        if (borderPx > 0) {
                            canvas.drawRect(destRect, paintBorder)
                        }

                        currentImageIndex++
                    }
                }
            } else {
                // Line layout: auto-fill rows based on computed width
                val colsPerRow = floor((printableWidthPx + spacingPx).toDouble() / (photoWidthPx + spacingPx)).toInt().coerceAtLeast(1)
                val rowHeightPx = photoHeightPx + spacingPx
                val maxRows = floor((printableHeightPx - 50f + spacingPx).toDouble() / rowHeightPx).toInt().coerceAtLeast(1)

                val lineTotalWidthPx = colsPerRow * photoWidthPx + (colsPerRow - 1) * spacingPx
                val startXPx = marginPx + (printableWidthPx - lineTotalWidthPx) / 2

                for (row in 0 until maxRows) {
                    if (currentImageIndex >= totalImages) break
                    for (col in 0 until colsPerRow) {
                        if (currentImageIndex >= totalImages) break

                        val x = startXPx + col * (photoWidthPx + spacingPx)
                        val y = marginPx + row * (photoHeightPx + spacingPx)

                        val destRect = Rect(x, y, x + photoWidthPx, y + photoHeightPx)
                        val bitmap = allCopies[currentImageIndex]
                        canvas.drawBitmap(bitmap, null, destRect, null)

                        if (borderPx > 0) {
                            canvas.drawRect(destRect, paintBorder)
                        }

                        currentImageIndex++
                    }
                }
            }

            pdfDocument.finishPage(page)
            pageNum++
        }

        val fos = FileOutputStream(outputFile)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        return outputFile
    }
}
