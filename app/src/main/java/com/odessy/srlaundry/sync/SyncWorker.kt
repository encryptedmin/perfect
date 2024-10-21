package com.odessy.srlaundry.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        val syncManager = SyncManager(applicationContext, this)

        try {
            syncManager.syncAllData()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
