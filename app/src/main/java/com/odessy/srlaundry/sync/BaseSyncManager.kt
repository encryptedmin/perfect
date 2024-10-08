package com.odessy.srlaundry.sync

import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseSyncManager {

    /**
     * Generic sync function to sync a list of items to Firestore.
     * Assumes that the type T has proper structure for Firestore.
     */
    protected suspend fun <T : Any> syncDataToFirestore(
        firestoreCollection: CollectionReference,
        localData: List<T>,
        getId: (T) -> String
    ) {
        withContext(Dispatchers.IO) {
            localData.forEach { item ->
                val id = getId(item)
                firestoreCollection.document(id).set(item) // Sync entity to Firestore using its ID
            }
        }
    }
}
