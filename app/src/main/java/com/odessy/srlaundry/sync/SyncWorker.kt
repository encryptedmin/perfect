package com.odessy.srlaundry.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    // Override doWork to perform the sync operations
    override suspend fun doWork(): Result = coroutineScope {
        // Pass the coroutine scope from the worker into the SyncManager
        val syncManager = SyncManager(applicationContext, this)

        try {
            syncManager.syncAllData() // Sync all the data
            Result.success() // Return success if sync is successful
        } catch (e: Exception) {
            Result.failure() // Return failure if something goes wrong
        }
    }
}
