package com.odessy.srlaundry

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.odessy.srlaundry.sync.SyncManager
import com.odessy.srlaundry.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class App : Application() {

    // Create a CoroutineScope for global sync (use SupervisorJob to prevent cancellations)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Perform initial sync when the app starts
        applicationScope.launch {
            SyncManager(this@App, applicationScope).syncAllData()
        }

        // Schedule periodic sync (every hour)
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        // Build a periodic sync request for every 1 hour
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .build()

        // Enqueue the periodic work
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "SyncWork",
                ExistingPeriodicWorkPolicy.KEEP,  // Prevents duplicate work
                syncWorkRequest
            )
    }
}
