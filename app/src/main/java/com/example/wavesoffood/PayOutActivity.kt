package com.example.wavesoffood

import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wavesoffood.Model.OrderDetails
import com.example.wavesoffood.databinding.ActivityPayOutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PayOutActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var binding : ActivityPayOutBinding

    private lateinit var name : String
    private lateinit var address : String
    private lateinit var phone : String
    private lateinit var totalAmount : String
    private lateinit var foodItemName : ArrayList<String>
    private lateinit var foodItemPrice : ArrayList<String>
    private lateinit var foodItemDescription : ArrayList<String>
    private lateinit var foodItemIngredient : ArrayList<String>
    private lateinit var foodItemImage : ArrayList<String>
    private  lateinit var foodItemQuantities : ArrayList<Int>

    private lateinit var database : DatabaseReference
    private lateinit var auth : FirebaseAuth
    private lateinit var userId : String

    private var isSubmitting  = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Initialise firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        //set user data
        setUserData()
        //get user details from firebase
        val intent = intent
        foodItemName = intent.getStringArrayListExtra("FoodItemName") as ArrayList<String>
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") as ArrayList<String>
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") as ArrayList<String>
        foodItemIngredient = intent.getStringArrayListExtra("FoodItemIngredient") as ArrayList<String>
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") as ArrayList<String>
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") as ArrayList<Int>

        totalAmount = "₹ " + calculateTotalAmount().toString() + " /-"
        binding.totalAmountField.isEnabled = false
        binding.totalAmountField.setText(totalAmount)
        binding.backButton.setOnClickListener{
            finish()
        }
        binding.placeOrderButton.setOnClickListener{
            if(isSubmitting)  return@setOnClickListener
            //get data from editTextView for sending to Admin app
            name = binding.nameField.text.toString().trim()
            address = binding.addressField.text.toString().trim()
            phone = binding.phoneField.text.toString().trim()
            if(name.isBlank() || address.isBlank() || phone.isBlank()){
                Toast.makeText(this, "Please enter all the details" , Toast.LENGTH_SHORT).show()
            }
            else{
                placeOrder()
            }

        }
        binding.onlinePayment.setOnClickListener{
            startPayment()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (data != null) {
                val response = data.getStringExtra("response")
                if (response != null && response.lowercase().contains("success")) {
                    Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
                    // Now place the order only after successful payment
                    placeOrder()
                } else {
                    Toast.makeText(this, "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_RD1inBhnPToBKH") // Test Key

        try {
            val options = JSONObject()
            options.put("name", "WavesOfFood")
            options.put("description", "Food Order Payment")
            options.put("currency", "INR")

            // Clean totalAmount (e.g., "₹ 20 /-")
            val cleanAmount = totalAmount.replace("₹", "")
                .replace("/-", "")
                .trim()

            val amountInRupees = cleanAmount.toInt()
            options.put("amount", amountInRupees * 100) // paise

            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email ?: "")
            prefill.put("contact", binding.phoneField.text.toString().trim())
            options.put("prefill", prefill)

            checkout.open(this, options)

        } catch (e: Exception) {
            Log.e("Payment", "Error: ${e.message}")
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ✅ Handle Payment Success
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        try {
            Toast.makeText(this, "Payment Successful! ID: $razorpayPaymentID", Toast.LENGTH_SHORT).show()
            name = binding.nameField.text.toString().trim()
            address = binding.addressField.text.toString().trim()
            phone = binding.phoneField.text.toString().trim()

            if(name.isBlank() || address.isBlank() || phone.isBlank()){
                Toast.makeText(this, "Please enter all the details before paying", Toast.LENGTH_LONG).show()
                return
            }

            placeOrder()
        } catch (e: Exception) {
            Log.e("Payment", "Exception in onPaymentSuccess", e)
        }
    }

    // ✅ Handle Payment Failure
    override fun onPaymentError(code: Int, response: String?) {
        try {
            Log.e("Payment", "Error response: $response")
            Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Log.e("Payment", "Exception in onPaymentError", e)
        }
    }


    private fun placeOrder() {
        isSubmitting = true // 🔒 block future clicks
        binding.placeOrderButton.isEnabled = false // 🚫 disable button

        userId = auth.currentUser?.uid?:""
        val time  = System.currentTimeMillis()
        val itemPushKey = database.child("orderDetails").push().key

        val orderDetails = OrderDetails(
            userId , name , foodItemName , foodItemImage ,
            foodItemPrice , foodItemQuantities , address ,
            totalAmount , phone , false, false, itemPushKey , time
        )

        val orderRef = database.child("orderDetails").child(itemPushKey!!)
        orderRef.setValue(orderDetails).addOnSuccessListener{
            val bottomSheet = CongratsBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, "Test")
            removeItemsFromCart()
            addOrderToHistory(orderDetails)
        }.addOnFailureListener {
            // If something goes wrong, allow retry
            isSubmitting = false
            binding.placeOrderButton.isEnabled = true
            Toast.makeText(this, "Failed to place order. Try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        database.child("user").child(userId)
            .child("buyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails)
            .addOnSuccessListener{

            }
    }

    private fun removeItemsFromCart() {
        val cartItemsRef = database.child("user").child(userId).child("cartItems")
        cartItemsRef.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for(i in 0 until foodItemPrice.size){
            var price = foodItemPrice[i]
            val lastChar = price.last()
            val priceIntValue = if(lastChar == '₹'){
                price.dropLast(1).toInt()
            }
            else{
                price.toInt()
            }
            var quantity = foodItemQuantities[i]
            totalAmount += (priceIntValue * quantity)
        }
        return totalAmount
    }

    private fun setUserData() {
        val user = auth.currentUser
        if(user != null){
            val userId = user.uid
            val userRef = FirebaseDatabase.getInstance().reference.child("user").child(userId)

            userRef.addListenerForSingleValueEvent(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val name = snapshot.child("name").getValue(String::class.java)?:""
                        val address = snapshot.child("address").getValue(String::class.java)?:""
                        val phone = snapshot.child("phone").getValue(String::class.java)?:""

                        binding.apply {
                            nameField.setText(name)
                            addressField.setText(address)
                            phoneField.setText(phone)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
}