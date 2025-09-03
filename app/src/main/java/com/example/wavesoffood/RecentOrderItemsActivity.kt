package com.example.wavesoffood

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Model.OrderDetails
import com.example.wavesoffood.adapter.RecentBuyAdapter
import com.example.wavesoffood.databinding.ActivityRecentOrderItemsBinding

class RecentOrderItemsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRecentOrderItemsBinding

    private lateinit var allFoodNames : ArrayList<String>
    private lateinit var allFoodImages : ArrayList<String>
    private lateinit var allFoodPrices : ArrayList<String>
    private lateinit var allFoodQuantities : ArrayList<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backNavigation.setOnClickListener{
            finish()
        }

        val recentOrderItems = intent.getSerializableExtra("recentBuyOrderItem") as? ArrayList<OrderDetails>
        recentOrderItems?.let{ orderDetails ->
            if(orderDetails.isNotEmpty()){
                val recentOrderItem = orderDetails[0]
                allFoodNames = recentOrderItem.foodNames as ArrayList<String>
                allFoodPrices = recentOrderItem.foodPrices as ArrayList<String>
                allFoodImages = recentOrderItem.foodImages as ArrayList<String>
                allFoodQuantities = recentOrderItem.foodQuantities as ArrayList<Int>
            }
        }
        setAdapter()

    }

    private fun setAdapter() {
        val rv = binding.recentBuyRecyclerView
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this , allFoodNames , allFoodImages , allFoodPrices , allFoodQuantities)
        rv.adapter = adapter
    }
}