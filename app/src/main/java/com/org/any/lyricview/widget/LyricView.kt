package com.org.any.lyricview.widget

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.org.any.lyricview.model.LyricBean
import com.org.any.lyricview.util.LyricHandle
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList


class LyricView : View, AnkoLogger {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val myPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    private var viewW = 0
    private var viewH = 0

    private var centerW = 0
    private var centerH = 0f

    private var lineH = 0f

    private var bigSize = 0f
    private var smallSize = 0f

    private var white: Int

    private var green: Int

    private var listLines = arrayListOf<LyricBean>()

    private var centerLine = 0

    private var offsetY = 0f

    private var downY = 0f
    private var tempY = 0f

    //播放进度
    private var progress = 0

    // 总时长
    private var duration = 0

    private val myBounds by lazy { Rect() }

    private var mTouch = false


    init {
        myPaint.textAlign = Paint.Align.CENTER
        lineH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics)
        bigSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, resources.displayMetrics)
        smallSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        white = Color.WHITE
        green = Color.GREEN
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (listLines.isEmpty()) {
            drawSingleLine(canvas)
        } else {
            drawMultiLine(canvas)
        }
    }

    /**
     * draw 加载  文本
     */
    private fun drawSingleLine(canvas: Canvas?) {
        val str = "正在加载歌词中..."
        myPaint.textSize = bigSize
        myPaint.color = white

        myPaint.getTextBounds(str, 0, str.length, myBounds)

        centerW = viewW / 2
        centerH = viewH / 2 - myBounds.height() / 2f

        canvas?.drawText(str, centerW.toFloat(), centerH, myPaint)
    }

    /**
     * draw 歌词
     */
    private fun drawMultiLine(canvas: Canvas?) {

        // 自动滚动
        if (!mTouch) {

            var currS = listLines[centerLine].startTime

            var  offsetTime = if (centerLine == listLines.size - 1) {
                // 偏移时长
                duration - currS
            } else {
                // 偏移时长
                listLines[centerLine + 1].startTime - currS
            }
            // 偏移百分比
            var percent = (progress - currS) / offsetTime.toFloat()
            // 偏移量
            var offY = percent * lineH

            offsetY = offY
        }


        for ((i, value) in listLines.withIndex()) {
            if (i == centerLine) {
                myPaint.color = green
                myPaint.textSize = bigSize
            } else {
                myPaint.color = white
                myPaint.textSize = smallSize
            }
            myPaint.getTextBounds(value.content, 0, value.content.length, myBounds)
            centerW = viewW / 2

            //中间行偏移 ，所有行都相对偏移
            centerH = viewH / 2 - myBounds.height() / 2 - offsetY

            val y = centerH + (i - centerLine) * lineH

            if (y < 0) continue

            if (y > viewH + lineH) break

            canvas?.drawText(value.content, centerW.toFloat(), y, myPaint)
        }

    }


    /**
     * 设置数据
     */
    fun setData(name: String) {
        doAsync {
            var inputStream = context.assets.open("$name.lrc")
            listLines = LyricHandle.loaderLyric(inputStream)
            listLines.forEach(::println)
            postInvalidate()
        }
    }

    fun setDuration(duration: Int) {
        this.duration = duration
        println("duration = $duration")
    }

    fun updateProgress(progress: Int) {
        this.progress = progress

        //触屏时不需要计算中间行
        if(mTouch) return

        // 计算出当前应该是哪一行被选中
        if (listLines.last().startTime <= progress) {
            centerLine = listLines.size - 1
        } else {
            for (i in 0 until listLines.size - 1) {
                if (listLines[i].startTime <= progress && listLines[i + 1].startTime > progress) {  // 1690    1691
                    centerLine = i                                                            // 4680    5000
                    break
                }
            }
        }
        invalidate()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        viewW = width
        viewH = height
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                tempY = offsetY
                mTouch = true

            }
            MotionEvent.ACTION_MOVE -> {
                val endY = event.y

                var offY = downY - endY

                offsetY = offY + tempY



                // 说明 达到 换行要求
                if (Math.abs(offsetY) > lineH) {

                    // 重新计算中间行
                    var lineN = (offsetY / lineH).toInt()

                    centerLine += lineN

                    // 重新计算偏移
                    offsetY %= lineH

                    tempY = offsetY

                    downY = endY
                }

                // 处理 边界
                centerLine = when {
                    centerLine <= 0 -> {
                        offsetY = 0f
                        tempY = 0f
                        0
                    }
                    centerLine >= listLines.size - 1 -> {
                        offsetY = 0f
                        tempY = 0f
                        listLines.size - 1
                    }
                    else -> centerLine
                }


                invalidate()

            }
            MotionEvent.ACTION_UP -> {

                // 处理拖动位置
                playerSeek?.let {
                    it(listLines[centerLine].startTime)
                }
                mTouch = false
            }
        }
        return true
    }


    private var playerSeek: ((progress: Int) -> Unit)? = null

    fun setPlayerSeek(player: ((progress: Int) -> Unit)?) {
        this.playerSeek = player
    }

}