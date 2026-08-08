package com.wasabi.grandmacompanion

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

object ButtonMotion {
    private const val PRESSED_SCALE = 0.965f
    private val interpolator = DecelerateInterpolator()

    @JvmStatic
    fun bind(vararg views: View?) {
        views.forEach { view ->
            view?.setOnTouchListener { target, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> animateScale(target, PRESSED_SCALE, 90L)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> animateScale(target, 1f, 170L)
                }
                false
            }
        }
    }

    private fun animateScale(view: View, scale: Float, duration: Long) {
        view.animate().cancel()
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, scale),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, scale)
            )
            this.duration = duration
            interpolator = ButtonMotion.interpolator
            start()
        }
    }
}
