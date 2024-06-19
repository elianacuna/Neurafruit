package com.ximoli.neurafruit

import android.widget.Filter
import com.ximoli.neurafruit.adapters.AdapterFavorites
import com.ximoli.neurafruit.models.Favorites

class FilterFavorite(
    private var filterList: ArrayList<Favorites>,
    private var adapterFavorites: AdapterFavorites
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            val filterModel = ArrayList<Favorites>()
            val filterPattern = constraint.toString().uppercase().trim()

            for (item in filterList) {
                if (item.name.uppercase().contains(filterPattern)) {
                    filterModel.add(item)
                }
            }

            results.values = filterModel
            results.count = filterModel.size
        } else {
            results.values = filterList
            results.count = filterList.size
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterFavorites.favoriteArrayList = results.values as ArrayList<Favorites>
        adapterFavorites.notifyDataSetChanged()
    }
}
