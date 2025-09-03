package com.example.wavesoffood.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wavesoffood.DetailsActivity
import com.example.wavesoffood.Model.MenuItem

import com.example.wavesoffood.databinding.PopularItemBinding


class PopularAdapter (private val menuItems : List<MenuItem>, private val requireContext: Context): RecyclerView.Adapter<PopularAdapter.PopularViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        return PopularViewHolder(PopularItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
//        val item = items[position]
//        val images = image[position]
//        val prices = price[position]
//        holder.bind(item,prices, images)
//
//        holder.itemView.setOnClickListener{
//            //to open details
//            val intent = Intent(requireContext, DetailsActivity :: class.java)
//            intent.putExtra("MenuItemName", items.get(position))
//            intent.putExtra("MenuImage", image.get(position))
//            requireContext.startActivity(intent)
//        }
    }
    override fun getItemCount(): Int {
        return menuItems.size
    }
    class PopularViewHolder (private val binding : PopularItemBinding) : RecyclerView.ViewHolder(binding.root){
        private val imageView = binding.foodPhoto

        fun bind(item: String, prices : String, images: Int) {
            binding.foodName.text = item
            binding.price.text = prices
            imageView.setImageResource(images)
        }
    }
}