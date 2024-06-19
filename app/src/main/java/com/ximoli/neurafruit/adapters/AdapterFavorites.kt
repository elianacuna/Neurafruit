package com.ximoli.neurafruit.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ximoli.neurafruit.FilterFavorite
import com.ximoli.neurafruit.R
import com.ximoli.neurafruit.databinding.RowFruitFavoriteBinding
import com.ximoli.neurafruit.models.Favorites
import com.ximoli.neurafruit.user.FavoriteActivity

class AdapterFavorites(private val context: Context, var favoriteArrayList: ArrayList<Favorites>) :
    RecyclerView.Adapter<AdapterFavorites.HolderFruitFavorite>(), Filterable {

    private val filterList: ArrayList<Favorites> = ArrayList(favoriteArrayList)
    private var filter: FilterFavorite? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFruitFavorite {
        val binding = RowFruitFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderFruitFavorite(binding)
    }

    override fun getItemCount(): Int {
        return favoriteArrayList.size
    }

    override fun onBindViewHolder(holder: HolderFruitFavorite, position: Int) {
        val model = favoriteArrayList[position]
        val id = model.id
        val name = model.name
        val imageUrl = model.image_fruit

        // Log the image URL for debugging
        Log.d("AdapterFavorites", "Image URL for position $position: $imageUrl")

        // Set data
        holder.name.text = name

        // Load image using Glide with try-catch
        try {
            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.logo)  // Error image
                .into(holder.fruit)
        } catch (e: Exception) {
            e.printStackTrace()
            holder.fruit.setImageResource(R.drawable.logo)  // Set a default image in case of an error
        }

        // Handle click, navigate to FavoriteActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, FavoriteActivity::class.java)
            intent.putExtra("fruitId", id)
            context.startActivity(intent)
        }
    }

    inner class HolderFruitFavorite(binding: RowFruitFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        val name: TextView = binding.nameFruit
        val nextIv: ImageView = binding.nextIv
        val fruit: ImageView = binding.fruitIv
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterFavorite(filterList, this)
        }
        return filter as FilterFavorite
    }
}
