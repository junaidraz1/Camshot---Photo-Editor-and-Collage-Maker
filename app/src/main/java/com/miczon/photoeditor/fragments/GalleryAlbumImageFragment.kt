package com.miczon.photoeditor.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.miczon.photoeditor.adapter.GalleryAlbumImageAdapter
import com.miczon.photoeditor.R
import com.miczon.photoeditor.databinding.FragmentGalleryAlbumImageBinding

/**
 * A simple [Fragment] subclass.
 */
class GalleryAlbumImageFragment : Fragment() {

    companion object {
        val ALBUM_IMAGE_EXTRA = "albumImage"
        val ALBUM_NAME_EXTRA = "albumName"
    }

    var mImages: ArrayList<String> = ArrayList()
    lateinit var names: String
    lateinit var mListener: OnSelectImageListener

    private lateinit var binding: FragmentGalleryAlbumImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (activity is OnSelectImageListener) {
            mListener = activity as OnSelectImageListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGalleryAlbumImageBinding.inflate(inflater)

        if (arguments != null) {
            mImages = requireArguments().getStringArrayList(ALBUM_IMAGE_EXTRA)!!
            names = requireArguments().getString(ALBUM_NAME_EXTRA)!!

            Log.d("grid", "set adapter")
            val adapter = GalleryAlbumImageAdapter(requireContext(), mImages)
            binding.gridView.adapter = adapter
            binding.gridView.setOnItemClickListener { _, _, position, _ ->
                mListener.onSelectImage(mImages[position])
            }
            adapter.notifyDataSetChanged()
        } else
            Log.e("TAG", "onCreateView: arguments null")

        binding.rlBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, GalleryAlbumFragment()).commit()
        }

        return binding.root
    }

    interface OnSelectImageListener {
        fun onSelectImage(str: String)
    }
}
