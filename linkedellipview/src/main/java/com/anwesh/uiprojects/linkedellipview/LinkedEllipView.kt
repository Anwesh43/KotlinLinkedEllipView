package com.anwesh.uiprojects.linkedellipview

/**
 * Created by anweshmishra on 14/06/18.
 */

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.MotionEvent
import android.graphics.*

val LE_NODES = 5

class LinkedEllipView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : LERenderer = LERenderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
            val gap : Float = (0.95f * w) / LE_NODES
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = Math.min(w, h) / 60
            paint.color = Color.parseColor("#9b59b6")
            canvas.save()
            canvas.translate(w/40 + gap * i + gap/2, h/2)
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

    data class LinkedEllip(var i : Int) {

        private var curr : LENode = LENode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class LERenderer (var view : LinkedEllipView) {

        private val animator : LEAnimator = LEAnimator(view)

        private val linkedEllip : LinkedEllip = LinkedEllip(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedEllip.draw(canvas, paint)
            animator.animate {
                linkedEllip.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedEllip.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) {
            activity.requestedOrientation =  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val view = LinkedEllipView(activity)
            activity.setContentView(view)
        }
    }
}