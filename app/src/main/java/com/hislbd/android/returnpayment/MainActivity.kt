package com.hislbd.android.returnpayment

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var someDependency: SomeDependency
    private val REQUEST_CODE_PERMISSIONS = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use someDependency here

        // Check and request permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            saveContactToPhone()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveContactToPhone()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot save contact.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveContactToPhone() {
        val displayName = "John Doe"
        val phoneNumber = "1234567890"

        val contentValues = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, "Primary")
            put(ContactsContract.RawContacts.ACCOUNT_NAME, displayName)
        }

        val rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)

        val rawContactId = rawContactUri?.lastPathSegment

        val phoneContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
            put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneContentValues)

        val nameContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameContentValues)

        Toast.makeText(this, "Contact saved successfully.", Toast.LENGTH_SHORT).show()
    }
}