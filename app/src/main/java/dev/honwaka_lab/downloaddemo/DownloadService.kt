package dev.honwaka_lab.downloaddemo

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toFile

class DownloadService(private val context: Context) {

    private val downloadManager = checkNotNull(context.getSystemService<DownloadManager>())

    private val onDownloadCompleted = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            val mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId)

            val query = DownloadManager.Query().apply {
                setFilterById(downloadId)
            }
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst().not()) {
                return
            }

            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

            if (status != DownloadManager.STATUS_SUCCESSFUL) {
                return
            }

            val path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            // file:///storageから始まってしまうのでUriを経由して/storageから始まるようにする
            val uri = Uri.parse(path)
            val contentUri = FileProvider.getUriForFile(this@DownloadService.context, BuildConfig.FILE_PROVIDER, uri.toFile())

            sendIntent(contentUri, mimeType)
        }
    }

    init {
        context.registerReceiver(onDownloadCompleted, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun download(url: String, fileName: String) {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri).apply {
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            enableVisibleInDownloadsUi(this)
        }
        downloadManager.enqueue(request)
    }

    @Suppress("DEPRECATION")
    private fun enableVisibleInDownloadsUi(request: DownloadManager.Request) {
        // This method was deprecated in API level 29. Starting in Q, this value is ignored.
        // Only files downloaded to public Downloads directory
        request.setVisibleInDownloadsUi(true)
    }

    private fun sendIntent(contentUri: Uri, mimeType: String) {

        val packageManager = context.packageManager
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, mimeType)
        }

        if (packageManager.resolveActivity(intent, PackageManager.MATCH_ALL) == null) {
            return
        }

        val shareIntent = Intent.createChooser(intent, null)

        context.startActivity(shareIntent)
    }
}