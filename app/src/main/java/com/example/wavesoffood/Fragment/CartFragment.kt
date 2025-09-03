package com.example.wavesoffood.Fragment

import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wavesoffood.Model.CartItems
import com.example.wavesoffood.PayOutActivity
import com.example.wavesoffood.R
import com.example.wavesoffood.adapter.CartAdapter
import com.example.wavesoffood.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CartFragment : Fragment() {
    private lateinit var binding : FragmentCartBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames : MutableList<String>
    private lateinit var foodPrices : MutableList<String>
    private lateinit var foodDescriptions : MutableList<String>
    private lateinit var foodImagesUri : MutableList<String>
    private lateinit var foodIngredients : MutableList<String>
    private lateinit var quantity : MutableList<Int>
    private lateinit var cartAdapter : CartAdapter
    private lateinit var userId : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container,false)

        auth = FirebaseAuth.getInstance()
        retrieveCartItems()


        // going to checkout
        binding.proceedButton.setOnClickListener{
            //get cart item details before proceeding to check-out
            getOrderItemDetails()

        }
        return binding.root
    }

    private fun getOrderItemDetails() {
        val orderIdRef : DatabaseReference = database.reference.child("user").child(userId).child("cartItems")
        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()
        // get items quantities
        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()

        orderIdRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(foodSnapshot in snapshot.children){
                    //get the cart items to respective list
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)
                    //add item details into list
                    orderItems?.foodName?.let{foodName.add(it)}
                    orderItems?.foodPrice?.let{foodPrice.add(it)}
                    orderItems?.foodDescription?.let{foodDescription.add(it)}
                    orderItems?.foodIngredients?.let{foodIngredient.add(it)}
                    orderItems?.foodImage?.let{foodImage.add(it)}
                }
                orderNow(foodName , foodPrice , foodDescription , foodIngredient , foodImage , foodQuantities)
            }

            private fun orderNow(
                foodName: MutableList<String>,
                foodPrice: MutableList<String>,
                foodDescription: MutableList<String>,
                foodIngredient: MutableList<String>,
                foodImage: MutableList<String>,
                foodQuantities: MutableList<Int>
            ) {
                if(isAdded && context != null){
                    val intent = Intent(requireContext() , PayOutActivity::class.java)
                    intent.putExtra("FoodItemName" , foodName as ArrayList<String>)
                    intent.putExtra("FoodItemPrice" , foodPrice as ArrayList<String>)
                    intent.putExtra("FoodItemDescription" , foodDescription as ArrayList<String>)
                    intent.putExtra("FoodItemIngredient" , foodIngredient as ArrayList<String>)
                    intent.putExtra("FoodItemImage" , foodImage as ArrayList<String>)
                    intent.putExtra("FoodItemQuantities" , foodQuantities as ArrayList<Int>)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order failed, please try again" , Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun retrieveCartItems() {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid?:""
        val foodRef : DatabaseReference = database.reference.child("user").child(userId).child("cartItems")

        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        quantity = mutableListOf()


        //fetch data from the database
        foodRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                foodNames.clear()
                foodPrices.clear()
                foodDescriptions.clear()
                foodImagesUri.clear()
                foodIngredients.clear()
                quantity.clear()
                for (foodSnapShot in snapshot.children){
                    val cartItems = foodSnapShot.getValue(CartItems::class.java)
                    cartItems?.uniqueKey = foodSnapShot.key
                    cartItems?.foodName?.let{foodNames.add(it)}
                    cartItems?.foodPrice?.let{foodPrices.add(it)}
                    cartItems?.foodDescription?.let{foodDescriptions.add(it)}
                    cartItems?.foodImage?.let{foodImagesUri.add(it)}
                    cartItems?.foodIngredients?.let{foodIngredients.add(it)}
                    cartItems?.foodQuantity?.let{quantity.add(it)}
                }
                // Enable/Disable Proceed Button
                if (foodNames.isEmpty()) {
                    binding.proceedButton.isEnabled = false
                    binding.proceedButton.alpha = 0.5f   // visually indicate disabled
                } else {
                    binding.proceedButton.isEnabled = true
                    binding.proceedButton.alpha = 1f
                }
                setAdapter()
            }

            private fun setAdapter() {
                cartAdapter = CartAdapter(requireContext() , foodNames ,foodPrices, foodImagesUri , foodDescriptions, foodIngredients , quantity)
                binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.VERTICAL , false)
                binding.cartRecyclerView.adapter = cartAdapter
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context , "Data not fetched" , Toast.LENGTH_SHORT).show()
            }

        })
    }



}