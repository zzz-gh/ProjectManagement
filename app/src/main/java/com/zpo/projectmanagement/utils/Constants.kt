package com.zpo.projectmanagement.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterActivity
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object Constants {

    const val USERS: String = "users"
    const val BOARD:String = "boards"
    const val IMAGE: String = "image"
    const val MOBILE: String = "mobile"
    const val NAME: String = "name"
    const val ASSIGNED_TO: String = "assignedTo"
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST:String = "taskList"
    const val ID: String = "id"
    const val EMAIL: String = "email"
    const val BOARD_DETAIL: String = "board_detail"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"

    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"
    const val PJMANAGE_PREFERENCES: String = "PjmanagePrefs"
    const val FCM_TOKEN:String = "fcmToken"
    const val FCM_TOKEN_UPDATED:String = "fcmTokenUpdated"


    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "AAAA9rVAoZ8:APA91bHUCOLHt4yZOC84f7Sw7ad-xZ2f0BQYrH9-6PrKDR8odDSWFi5HKMB7I_Pa39D3n3VPKabbJqLQiUo5vLkwPhkaHgDQ0b28ZVeXnth0aUf1zf9DLD_rnNUhSqIB84WFwWssmmIt"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"


    fun imageLoad(activity: Activity) {

        Dexter.withContext(activity)
            .withPermissions(
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        showImageChooser(
                            activity
                        )
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(
                            activity,
                            "Oops, you just denied the permission for storage. You can also allow it from settings.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationalPermissionDialog(
                        activity
                    )
                }

            }).onSameThread().check()
    }
    fun showImageChooser(activity: Activity) {

        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent,
            PICK_IMAGE_REQUEST_CODE
        )
    }



    fun showRationalPermissionDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Permission")
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTING") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            }
            .setNegativeButton("Canceled") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


}

