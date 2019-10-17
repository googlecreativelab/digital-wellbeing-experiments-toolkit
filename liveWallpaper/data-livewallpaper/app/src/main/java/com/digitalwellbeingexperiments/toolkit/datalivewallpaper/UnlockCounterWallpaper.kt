// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.digitalwellbeingexperiments.toolkit.datalivewallpaper

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.preference.PreferenceManager
import android.service.wallpaper.WallpaperService
import android.text.TextPaint
import android.view.SurfaceHolder
import android.view.animation.PathInterpolator
import androidx.core.content.res.ResourcesCompat

private const val ANIMATION_DELAY = 1000L
private const val FRAME_RATE: Long = 60

class UnlockCounterWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine = UnlockClockWallpaperEngine(this)

    private inner class UnlockClockWallpaperEngine(val context: Context) : Engine() {

        private val prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == COUNT_PREFERENCE) handler.postDelayed( {
                    updateCounter(sharedPreferences.getInt(
                        COUNT_PREFERENCE,
                        COUNT_PREFERENCE_DEFAULT_VALUE
                    ),
                        sharedPreferences.getInt(
                            PREV_COUNT_PREFERENCE,
                            PREV_COUNT_PREFERENCE_DEFAULT_VALUE
                        )
                        )
                }, ANIMATION_DELAY)
            }

        private val handler = Handler()
        private var charHeight: Float = 0f
        private var charWidth: Float = 0f
        private var bottomMargin = 0f
        private var width: Int = 0
        private var height: Int = 0
        private var topMargin = 0f
        private val backgroundColor = context.getColor(R.color.background)
        private var count = 0
        private var previousCount = 0
        private val drawRunner = Runnable { draw() }

        // 880 ms
        private val interpolator = PathInterpolator(
            Path().apply {
                this.cubicTo(
                    0.94f, 0.0f,
                    0.5f, 1f,
                    1f, 1f)
            })

        private val letterboxPaint = Paint().apply {
            color = backgroundColor
        }

        private val counterTextPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER
            color = context.getColor(R.color.colorPrimary)
            typeface = ResourcesCompat.getFont(context,
                R.font.sixcaps
            )
            isAntiAlias = true
        }

        private val unlocksTodayTextPaint = TextPaint().apply {
            textAlign = Paint.Align.CENTER
            color = context.getColor(R.color.colorPrimary)
            typeface = ResourcesCompat.getFont(context,
                R.font.opensans_regular
            )
            letterSpacing = 0.05f
            isAntiAlias = true
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            if (isPreview.not()) { // live mode
                registerReceiver(UnlockBroadcastReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
                PreferenceManager.getDefaultSharedPreferences(this@UnlockCounterWallpaper).apply {
                    registerOnSharedPreferenceChangeListener(prefListener)
                    count = getInt(
                        COUNT_PREFERENCE,
                        COUNT_PREFERENCE_DEFAULT_VALUE
                    )

                }
            }
            else { // preview mode
                count = PREVIEW_COUNT
            }
        }

        private fun calculateCharWidth(): Float {
            val widths = arrayOf(0f).toFloatArray()
            counterTextPaint.getTextWidths( "0", 0, 1, widths)
            return widths.first()
        }


        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height

            counterTextPaint.textSize = dpToPx(context, 400f)
            unlocksTodayTextPaint.textSize = dpToPx(context, 14f)
            this.charHeight = counterTextPaint.textSize
            this.charWidth = calculateCharWidth()
            this.topMargin = (height / 2f) - (charHeight / 1.7f)
            this.bottomMargin = dpToPx(context,30f) + counterTextPaint.textSize + topMargin
        }

        private var movementPosition = 0f
        private var movementSpeed = 1f / (FRAME_RATE * (22f / 25f))
        
        private fun draw() {

            surfaceHolder.lockCanvas()?.apply {
                drawColor(backgroundColor)
                if (movementPosition < 1.0)
                    movementPosition = Math.min(1.0f, movementPosition + movementSpeed)

                drawCounter(this, count, previousCount)
                drawLetterbox(this)
                surfaceHolder.unlockCanvasAndPost(this)

                if (movementPosition < 1.0)
                    handler.postDelayed(drawRunner, 1000/ FRAME_RATE)
            }
        }

        private fun drawLetterbox(canvas: Canvas) {
            canvas.drawRect(Rect(0, 0, width, topMargin.toInt()), letterboxPaint)
            canvas.drawRect(Rect(0, bottomMargin.toInt(), width, height), letterboxPaint)
        }

        private fun drawCounter(
            canvas: Canvas,
            count: Int,
            previousCount: Int
        ) {
            val prevCount = Math.max(0, previousCount)

            val prevUnits = prevCount % 10
            val prevTens = (prevCount / 10) % 10
            val prevHundreds = (prevCount / 100) % 100

            val units = count % 10
            val tens = (count / 10) % 10
            val hundreds = (count / 100) % 100


            // calculate incoming row
            when {
                hundreds > 0 -> {
                    drawNumber(canvas, hundreds, prevHundreds, - charWidth)
                    drawNumber(canvas, tens, prevTens)
                    drawNumber(canvas, units, prevUnits, + charWidth)
                }
                tens > 0 -> {
                    drawNumber(canvas, tens, prevTens, - charWidth * 0.5f)
                    drawNumber(canvas, units, prevUnits, + charWidth * 0.5f)
                }
                else -> {
                    drawNumber(canvas, units, prevUnits)
                }
            }

            when {
                prevHundreds > 0 -> {
                    drawNumber(canvas, prevHundreds, hundreds, - charWidth, counterTextPaint.textSize)
                    drawNumber(canvas, prevTens, tens, 0f, counterTextPaint.textSize)
                    drawNumber(canvas, prevUnits, units, +charWidth, counterTextPaint.textSize)
                }
                prevTens > 0 -> {
                    drawNumber(canvas, prevTens, tens,  - charWidth*0.5f, counterTextPaint.textSize)
                    drawNumber(canvas, prevUnits, units, + charWidth*0.5f, counterTextPaint.textSize)
                }
                else -> {
                    drawNumber(canvas, prevUnits, units, 0f, counterTextPaint.textSize)
                }
            }
        }

        private fun drawNumber(canvas: Canvas, number: Int, comparisonNumber: Int, xOffset: Float = 0f, rowOffset: Float = 0f) {
            val positionMultiplier = if (number != comparisonNumber) movementPosition else 0f
            val yPosition = rowOffset + topMargin + charHeight * interpolator.getInterpolation(positionMultiplier)
            if ((yPosition <= topMargin) ||  yPosition >= bottomMargin + (charHeight * 0.85f)) return
            canvas.drawText(number.toString(), width / 2f + xOffset, yPosition, counterTextPaint)
        }

        private fun updateCounter(counter: Int, previous: Int) {
            count = counter
            previousCount = previous
            movementPosition = 0f
            draw()
        }

        private fun dpToPx(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density
    }
}