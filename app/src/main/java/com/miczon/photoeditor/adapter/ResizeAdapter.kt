package com.miczon.photoeditor.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miczon.photoeditor.R

class ResizeAdapter(context: Context, resizeClickListener: OnResizeClickListener) :
    RecyclerView.Adapter<ResizeAdapter.ResizeHolder>() {

    var resizeListener: OnResizeClickListener = resizeClickListener

    interface OnResizeClickListener {
        fun onResizeClick(position: Int)
    }

    var mContext = context
    var texts = arrayOf(
        context.getString(R.string.original_lbl),
        context.getString(R.string.free_lbl),
        "1:1",
        "1:2",
        "2:1",
        "2:3",
        "3:2",
        "3:4",
        "4:5",
        "9:16",
        "16:9"
    )
    var icons = arrayOf(
        R.drawable.ic_original_ratio,
        R.drawable.ic_free_ratio,
        R.drawable.ic_ratio_1by1,
        R.drawable.ic_ratio_1by2,
        R.drawable.ic_ratio_2by1,
        R.drawable.ic_ratio_2by3,
        R.drawable.ic_ratio_3by2,
        R.drawable.ic_ratio_3by4,
        R.drawable.ic_ratio_4by5,
        R.drawable.ic_ratio_9by16,
        R.drawable.ic_ratio_16by9

    )
    var selectedindex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResizeHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_resize, parent, false)
        return ResizeHolder(view)
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    override fun onBindViewHolder(holder: ResizeHolder, position: Int) {
        holder.item_resize.text = texts[holder.absoluteAdapterPosition]
        holder.imagePreview.setImageResource(icons[holder.absoluteAdapterPosition])

        if (selectedindex == holder.absoluteAdapterPosition) {
            holder.item_resize.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent))
            holder.itemLayout.setBackgroundResource(R.drawable.bg_lightpurple_selected_bg)
        } else {
            holder.item_resize.setTextColor(ContextCompat.getColor(mContext, R.color.white))
            holder.itemLayout.setBackgroundResource(R.drawable.bg_lightgrey_unselected_bg)
        }

        holder.ll_resize.setOnClickListener {
            selectedindex = holder.absoluteAdapterPosition
            resizeListener.onResizeClick(holder.absoluteAdapterPosition)
            notifyDataSetChanged()
        }
    }

    class ResizeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var item_resize: TextView = itemView.findViewById(R.id.item_resize)
        var itemLayout: RelativeLayout = itemView.findViewById(R.id.rl_image_size)
        var ll_resize: LinearLayout = itemView.findViewById(R.id.ll_resize)
        var imagePreview: ImageView = itemView.findViewById(R.id.iv_image_preview)
    }
}