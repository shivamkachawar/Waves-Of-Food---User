package com.example.wavesoffood.Model

import java.io.Serializable

class OrderDetails(
    var userId: String? = null,
    var userName: String? = null,
    var foodNames: MutableList<String>? = null,
    var foodImages: MutableList<String>? = null,
    var foodPrices: MutableList<String>? = null,
    var foodQuantities: MutableList<Int>? = null,
    var address: String? = null,
    var totalPrice: String? = null,
    var phoneNumber: String? = null,
    var orderAccepted: Boolean = false,
    var paymentReceived: Boolean = false,
    var itemPushKey: String? = null,
    var currentTime: Long = 0
):Serializable