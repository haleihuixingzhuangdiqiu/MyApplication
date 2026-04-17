package com.example.myapplication.game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.imageLoader
import com.example.myapplication.game.adapter.GameBannerUi

internal class GameBannerPagerAdapter(
    private val items: List<GameBannerUi>,
    private val onItemClick: (GameBannerUi) -> Unit,
) : RecyclerView.Adapter<GameBannerPagerAdapter.VH>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_banner_page, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.banner_image)
        private val title: TextView = itemView.findViewById(R.id.banner_title)
        private val sub: TextView = itemView.findViewById(R.id.banner_subtitle)

        fun bind(ui: GameBannerUi) {
            title.text = ui.title
            sub.text = ui.subtitle
            image.load(ui.imageUrl, itemView.context.imageLoader) {
                crossfade(true)
            }
            itemView.setOnClickListener { onItemClick(ui) }
        }
    }
}
