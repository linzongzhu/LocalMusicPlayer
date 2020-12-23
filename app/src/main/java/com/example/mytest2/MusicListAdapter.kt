package com.example.mytest2

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView

class MusicListAdapter(val musicList:List<Music>,val mainActivity: MainActivity) :
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    private var itemOnClickListener: View.OnClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var musicImage: ImageView = view.findViewById(R.id.musicImage)
        var musicName: TextView = view.findViewById(R.id.musicName)
        var authorName: TextView = view.findViewById(R.id.authorName)

//        fun setOnClickListener() {
//
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_list,parent,false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            mainActivity.changeMusic(position)
        }
        return viewHolder

    }

//    fun setOnClickListener(l: View.OnClickListener) {
//        itemOnClickListener = l
//    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        holder.musicImage.setImageResource(music.imageId)
        holder.musicName.text = music.name
        holder.authorName.text = music.author
//        if (itemOnClickListener != null) {
//            holder.itemView.setOnClickListener(itemOnClickListener)
//        }
    }

}
