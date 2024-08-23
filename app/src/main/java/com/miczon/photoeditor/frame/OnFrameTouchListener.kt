package com.miczon.photoeditor.frame

import android.view.MotionEvent

interface OnFrameTouchListener {
    fun onFrameTouch(event: MotionEvent)
    fun onFrameDoubleClick(event: MotionEvent)
}
