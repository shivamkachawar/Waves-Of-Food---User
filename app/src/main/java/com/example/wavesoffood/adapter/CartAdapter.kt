package com.example.wavesoffood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wavesoffood.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter (private val context : Context, private val cartItems : MutableList<String>, private val cartItemPrices : MutableList<String>, private val cartImages : MutableList<String>, private val cartDescriptions : MutableList<String>,private val cartIngredients : MutableList<String>, private var cartQuantity : MutableList<Int>): RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init{
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid?:""
        val cartItemNumber = cartItems.size
        itemQuantities = IntArray(cartItemNumber){1}
        cartItemsReference = database.reference.child("user").child(userId).child("cartItems")
    }
    companion object{
        private var itemQuantities : IntArray = intArrayOf()
        private lateinit var cartItemsReference : DatabaseReference
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }
    override fun getItemCount(): Int {
        return cartItems.size
    }
    inner class CartViewHolder (private val binding : CartItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            binding.apply {
                val quantities = itemQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)
                cartItemQuantity.text = quantities.toString()

                minusButton.setOnClickListener {
                    decreaseQuantity(position)
                }
                plusButton.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteButton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if(itemPosition != RecyclerView.NO_POSITION){
                        deleteItem(position)
                    }
                }
            }
        }
        private fun decreaseQuantity(position : Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun increaseQuantity(position : Int){
            if(itemQuantities[position] < 10){
                itemQuantities[position]++
                cartQuantity[position] = itemQuantities[position]
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }
        }
        private fun deleteItem(position : Int){
            val positionRetrieve = position
            getUniqueKeyAtPosition(positionRetrieve){ uniqueKey ->
                if(uniqueKey != null){
                    removeItem(position, uniqueKey)
                }
            }
        }
        private fun removeItem(position: Int, uniqueKey: String) {
            if(uniqueKey != null){
                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
//
                    Toast.makeText(context , "Item removed from cart", Toast.LENGTH_SHORT).show()
//
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to remove item" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getUniqueKeyAtPosition(positionRetrieve: Int , onComplete:(String?) -> Unit){
        cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var uniqueKey : String? = null
                //Loop for snapshot children
                snapshot.children.forEachIndexed { index, dataSnapshot ->
                    if(index == positionRetrieve){
                        uniqueKey = dataSnapshot.key
                        return@forEachIndexed
                    }
                }
                onComplete(uniqueKey)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        itemQuantity.addAll(cartQuantity)
        return itemQuantity
    }
}