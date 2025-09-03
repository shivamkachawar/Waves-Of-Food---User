package com.example.wavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import com.example.wavesoffood.databinding.RecentBuyItemBinding

class RecentBuyAdapter (private var context : Context , private var foodNameList : ArrayList<String> , private var foodImageList : ArrayList<String> , private var foodPriceList : ArrayList<String> , private var foodQuantityList : ArrayList<Int>) :
    RecyclerView.Adapter<RecentBuyAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentBuyAdapter.RecentViewHolder {
        val binding = RecentBuyItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return RecentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentBuyAdapter.RecentViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return foodNameList.size
    }
    inner class RecentViewHolder (private val binding : RecentBuyItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            binding.apply {
                foodName.text = foodNameList[position]
                foodPrice.text = foodPriceList[position]
                foodQuantity.text = foodQuantityList[position].toString()
                val uri = Uri.parse(foodImageList[position])
                Glide.with(context).load(uri).into(foodImage)
            }
        }

    }
}