package com.odessy.srlaundry.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odessy.srlaundry.dao.*
import com.odessy.srlaundry.entities.*
import com.odessy.srlaundry.converters.DateTypeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Accounts::class, Customer::class, JobOrder::class,
    StoreItem::class, LaundryPrice::class, LaundrySales::class, SmsMessage::class, Promotion::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountsDao(): AccountsDao
    abstract fun customerDao(): CustomerDao // Added CustomerDao
    abstract fun jobOrderDao(): JobOrderDao
    abstract fun promotionDao(): PromotionDao
    abstract fun laundryPriceDao(): LaundryPriceDao
    abstract fun laundrySalesDao(): LaundrySalesDao
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun storeItemDao(): StoreItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "laundry_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Callback for populating the database with initial data
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.accountsDao(), database.laundryPriceDao())
                    }
                }
            }
        }

        // Populate the database with an admin account and initial laundry prices
        suspend fun populateDatabase(accountsDao: AccountsDao, laundryPriceDao: LaundryPriceDao) {
            // Insert admin account
            val adminAccount = Accounts(
                username = "admin",
                password = "admin",
                role = "admin"
            )
            accountsDao.insert(adminAccount)

            // Insert initial laundry prices
            val initialPrices = LaundryPrice(
                regular = 180.0,
                bedSheet = 200.0,
                addOnDetergent = 12.0,
                addOnFabricConditioner = 12.0,
                addOnBleach = 12.0
            )
            laundryPriceDao.insertLaundryPrice(initialPrices)
        }

    }
}

