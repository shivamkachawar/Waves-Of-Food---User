package com.example.wavesoffood

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.wavesoffood.Model.CartItems
import com.example.wavesoffood.databinding.ActivityDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private var foodName : String? = null
    private var foodImage : String? = null
    private var foodDescription : String? = null
    private var foodIngredients : String? = null
    private var foodPrice : String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        foodName = intent.getStringExtra("MenuItemName")!!
        foodDescription = intent.getStringExtra("MenuItemDescription")!!
        foodIngredients = intent.getStringExtra("MenuItemIngredients")!!
        foodPrice = intent.getStringExtra("MenuItemPrice")!!
        foodImage = intent.getStringExtra("MenuItemImage")!!

        with(binding){
            detailFoodName.text = foodName
            description.text = foodDescription
            ingredients.text = foodIngredients
            Glide.with(this@DetailsActivity).load(foodImage).into(detailFoodImage)
        }



        binding.backButton.setOnClickListener{
            finish()
        }
        binding.addToCartButton.setOnClickListener{
            addItemToCart()
        }

    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = Firebase.auth.currentUser?.uid?:""
        //Create a cart item object
        val cartItem = CartItems(foodName.toString() , foodPrice.toString() , foodDescription.toString() , foodImage.toString() , foodIngredients.toString() , 1)
        //save data to cart item to firebase
        database.child("user").child(userId).child("cartItems").push().setValue(cartItem).addOnSuccessListener{
            Toast.makeText(this, "Item added to cart" , Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener{
                Toast.makeText(this, "Failed adding to cart" , Toast.LENGTH_SHORT).show()
            }


    }
}