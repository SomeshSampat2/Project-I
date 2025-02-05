package com.innovatelabs3.projectI2.utils

import android.content.Context
import android.provider.ContactsContract
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat

class ContactUtils {
    companion object {
        data class ContactInfo(
            val name: String,
            val phoneNumber: String
        )

        fun findContactByName(context: Context, name: String): ContactInfo? {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            val selection = """
                ${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} LIKE ?
                OR ${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_ALTERNATIVE} LIKE ?
            """.trimIndent()
            
            // Using % for partial name matching
            val selectionArgs = arrayOf("%$name%", "%$name%")
            
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
                    )
                    val numberIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    
                    val contactName = cursor.getString(nameIndex)
                    var phoneNumber = cursor.getString(numberIndex)
                    
                    // Clean the phone number
                    phoneNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
                    
                    // Add country code if missing
                    if (!phoneNumber.startsWith("+")) {
                        phoneNumber = "+91$phoneNumber"
                    }
                    
                    return ContactInfo(contactName, phoneNumber)
                }
            }
            return null
        }

        fun checkContactPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
} 