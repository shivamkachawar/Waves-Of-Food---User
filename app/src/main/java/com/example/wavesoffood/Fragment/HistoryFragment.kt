package com.example.wavesoffood.Fragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wavesoffood.Model.OrderDetails
import com.example.wavesoffood.RecentOrderItemsActivity
import com.example.wavesoffood.adapter.BuyAgainAdapter
import com.example.wavesoffood.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryFragment : Fragment() {
    private lateinit var binding : FragmentHistoryBinding
    private lateinit var buyAgainAdapter : BuyAgainAdapter

    private lateinit var database : FirebaseDatabase
    private lateinit var auth : FirebaseAuth
    private lateinit var userId : String
    private var listOfOrderItem : MutableList<OrderDetails> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //retrieve and display the user order history
        retrieveBuyHistory()
        binding.recentBuyItem.setOnClickListener{
            seeItemsRecentBuy()
        }
        binding.receivedButton.setOnClickListener{
            updateOrderStatus()

        }
        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderRef = database.reference.child("completedOrder").child(itemPushKey!!)
        completeOrderRef.child("paymentReceived").setValue(true)
            .addOnCompleteListener() {
                Toast.makeText(requireContext() , "Order received" , Toast.LENGTH_SHORT).show()
            }
    }

    private fun seeItemsRecentBuy() {
        listOfOrderItem.firstOrNull()?.let { recentBuy ->
            val intent = Intent(requireContext() , RecentOrderItemsActivity::class.java)
            intent.putExtra("recentBuyOrderItem" , arrayListOf(recentBuy))
            startActivity(intent)
        }
    }

    private fun retrieveBuyHistory() {
        binding.recentBuyItem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid?:""

        val buyItemRef : DatabaseReference = database.reference.child("user").child(userId).child("buyHistory")
        val sortingQuery = buyItemRef.orderByChild("currentTime")

        sortingQuery.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (buySnapshot in snapshot.children){
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails :: class.java)
                    buyHistoryItem?.let { listOfOrderItem.add(it) }
                }
                listOfOrderItem.reverse()
                if(listOfOrderItem.isNotEmpty()){
                    setDataInRecentBuyItem()
                    setPreviouslyBuyItemRecyclerView()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun setDataInRecentBuyItem() {
        binding.recentBuyItem.visibility = View.VISIBLE
        val recentOrderItem = listOfOrderItem.firstOrNull()
        recentOrderItem?.let{
            with(binding){
                recentFoodName.text = it.foodNames?.firstOrNull()?:""
                recentFoodPrice.text = it.foodPrices?.firstOrNull()?:""
                val image = it.foodImages?.firstOrNull()?:""
                val uri = Uri.parse(image)
                Glide.with(requireContext()).load(uri).into(recentFoodImage)

                val isOrderIsAccepted = listOfOrderItem[0].orderAccepted
                if(isOrderIsAccepted){
                    receivedButton.visibility = View.VISIBLE
                    orderStatus.background.setTint(Color.GREEN)
                }

            }
        }
    }

    private fun setPreviouslyBuyItemRecyclerView() {
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()

        for(i in 1 until listOfOrderItem.size){
            listOfOrderItem[i].foodNames?.firstOrNull()?.let { buyAgainFoodName.add(it) }
            listOfOrderItem[i].foodPrices?.firstOrNull()?.let { buyAgainFoodPrice.add(it) }
            listOfOrderItem[i].foodImages?.firstOrNull()?.let { buyAgainFoodImage.add(it) }
        }
        val rv = binding.historyRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
        buyAgainAdapter = BuyAgainAdapter(buyAgainFoodName , buyAgainFoodPrice , buyAgainFoodImage , requireContext())
        rv.adapter = buyAgainAdapter
    }

}