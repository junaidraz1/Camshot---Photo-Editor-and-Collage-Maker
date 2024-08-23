package com.miczon.photoeditor.multitouch

interface OnDoubleClickListener {
    fun onPhotoViewDoubleClick(view: PhotoView, entity: MultiTouchEntity)
    fun onBackgroundDoubleClick()
}
