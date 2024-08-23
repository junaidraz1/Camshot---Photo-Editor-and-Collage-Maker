package com.miczon.photoeditor.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.makeramen.roundedimageview.RoundedImageView
import com.miczon.photoeditor.utils.AndroidUtils
import com.miczon.photoeditor.R

class GalleryAlbumImageAdapter(context: Context, list: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_gallery_photo, list) {

    private val mInflater = LayoutInflater.from(context)
    private val mList: List<String> = list

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val viewHolder: ViewHolder

        Log.d("grid", "gallery album img adapter getView()")

        if (convertView == null) {
            viewHolder = ViewHolder()
            view = mInflater.inflate(R.layout.item_gallery_photo, parent, false)
            viewHolder.imageView = view.findViewById(R.id.imageView)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        AndroidUtils.loadImageWithGlide(context, viewHolder.imageView, getItem(position))
        return view
    }

    override fun getCount(): Int {
        Log.d("grid", "list size: ${mList.size}")
        return mList.size
    }

    inner class ViewHolder {
        lateinit var imageView: RoundedImageView
    }
}