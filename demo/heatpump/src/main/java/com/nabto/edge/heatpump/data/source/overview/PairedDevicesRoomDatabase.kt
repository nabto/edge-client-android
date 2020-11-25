package com.nabto.edge.heatpump.data.source.overview

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PairedDevice::class], version = 3, exportSchema = false)
abstract class PairedDevicesRoomDatabase : RoomDatabase() {

    abstract fun pairedDevicesDao(): PairedDevicesDao

    companion object {
        @Volatile
        private var INSTANCE: PairedDevicesRoomDatabase? = null

        fun getDatabase(
                context: Context
        ): PairedDevicesRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE
                    ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PairedDevicesRoomDatabase::class.java,
                        "paired_devices_database")
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}