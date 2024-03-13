package com.hislbd.android.returnpayment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.opencsv.CSVReader
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.io.InputStreamReader

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var someDependency: SomeDependency
    private val REQUEST_CODE_PERMISSIONS = 101
    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    readContactsFromXlsxFile(uri)
                }
            } else {
                Toast.makeText(this, "File picking canceled.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Use someDependency here

        // Check and request permission
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_CONTACTS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.WRITE_CONTACTS),
//                REQUEST_CODE_PERMISSIONS
//            )
//        } else {
//            saveContact()
//        }

// Check and request permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            pickFile()
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
                //type = "text/csv"//for csv
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // XLSX MIME type
        }
        pickFileLauncher.launch(intent)
    }

    //    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                saveContact()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Permission denied. Cannot save contact.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFile()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot access files.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveContact() {
        val displayName = "John Doe"
        val phoneNumber = "1234567890"

        val contentValues = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, "Primary")
            put(ContactsContract.RawContacts.ACCOUNT_NAME, displayName)
        }

        val rawContactUri =
            contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)

        val rawContactId = rawContactUri?.lastPathSegment

        val phoneContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
            put(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneContentValues)

        val nameContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameContentValues)

        Toast.makeText(this, "Contact saved successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun saveContact(displayName: String?, phoneNumber: String?) {
        val contentValues = ContentValues().apply {
            put(ContactsContract.RawContacts.ACCOUNT_TYPE, "null")
            put(ContactsContract.RawContacts.ACCOUNT_NAME, "null")
        }

        val rawContactUri =
            contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
        val rawContactId = rawContactUri?.lastPathSegment

        val phoneContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
            put(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneContentValues)

        val nameContentValues = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameContentValues)
    }

    private fun saveContactsFromCSV() {
        try {
            val inputStream = assets.open("contacts.csv")
            val reader = CSVReader(InputStreamReader(inputStream))
            var nextLine: Array<String>?
            while (reader.readNext().also { nextLine = it } != null) {
                val displayName = nextLine?.get(0)
                val phoneNumber = nextLine?.get(1)
                saveContact(displayName, phoneNumber)
            }
            Toast.makeText(this, "Contacts saved successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading CSV file.", Toast.LENGTH_SHORT).show()
            Log.d("ErrorReadFile", "saveContactsFromCSV error: "+e.message)
        }
    }
    private fun readContactsFromXlsxFile(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)//assets.open("contacts.xlsx")
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0) // assuming the contacts are in the first sheet

            for (i in 1 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(i)
                val displayName = row.getCell(0)?.stringCellValue
                val phoneNumber = row.getCell(1)?.stringCellValue
                saveContact(displayName, phoneNumber)
            }

            Toast.makeText(this, "Contacts saved successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading XLSX file.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveContactsFromCSV(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = CSVReader(InputStreamReader(inputStream))
            var nextLine: Array<String>?
            while (reader.readNext().also { nextLine = it } != null) {
                val displayName = nextLine?.get(0)
                val phoneNumber = nextLine?.get(1)
                saveContact(displayName, phoneNumber)
            }
            Toast.makeText(this, "Contacts saved successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error reading CSV file.", Toast.LENGTH_SHORT).show()
        }
    }
}


