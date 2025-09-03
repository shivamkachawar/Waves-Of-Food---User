package com.example.wavesoffood.Model

data class CartItems (
    val foodName: String? = null,
    val foodPrice: String? = null,
    val foodDescription: String? = null,
    val foodImage: String? = null,
    val foodIngredients: String? = null,
    val foodQuantity: Int? = null,
    var uniqueKey: String? = null

)
