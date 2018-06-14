package com.anwesh.uiprojects.linkedellipview

/**
 * Created by anweshmishra on 14/06/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.*

val LE_NODES = 5

class LinkedEllipView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class LEState (var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(prevScale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class LEAnimator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LENode (var i : Int, val state : LEState = LEState()) {

        private var next : LENode? = null

        private var prev : LENode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < LE_NODES - 1) {
                next = LENode(i +1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = w / LE_NODES
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = Math.min(w, h) / 60
            paint.color = Color.parseColor("#9b59b6")
            canvas.save()
            canvas.translate(w/2 + gap/2, h/2)
            canvas.drawArc(RectF(-gap/2, -gap/4, gap/2, gap/4), 180f - 180f * state.scale, 2 * 180f * state.scale, false,paint)
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LENode {
            var curr : LENode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }
}