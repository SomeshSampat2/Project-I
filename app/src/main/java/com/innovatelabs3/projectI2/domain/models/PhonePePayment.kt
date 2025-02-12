package com.innovatelabs3.projectI2.domain.models

data class PhonePePayment(
    val amount: String,
    val recipientUpiId: String,
    val recipientName: String = "Recipient"
)