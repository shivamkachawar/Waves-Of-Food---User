package com.example.wavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.adapter.PopularAdapter.PopularViewHolder
import com.example.wavesoffood.databinding.BuyAgainItemBinding
import com.example.wavesoffood.databinding.MenuItemBinding
import com.example.wavesoffood.databinding.PopularItemBinding

class BuyAgainAdapter(private val buyAgainFoodName : MutableList<String> , private val buyAgainFoodPrice : MutableList<String>, private val buyAgainFoodImage : MutableList<String> , private var requireContext : Context) :
    RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(buyAgainFoodName[position], buyAgainFoodPrice[position], buyAgainFoodImage[position])
    }
    override fun getItemCount(): Int{
        return buyAgainFoodName.size
    }

    inner class BuyAgainViewHolder (private val binding: BuyAgainItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(foodName: String, foodPrice: String, foodImage: String) {
            binding.buyAgainFoodName.text = foodName
            binding.buyAgainFoodPrice.text = foodPrice
            val uri = Uri.parse(foodImage)
            Glide.with(requireContext).load(uri).into(binding.buyAgainFoodImage)


        }

    }

}