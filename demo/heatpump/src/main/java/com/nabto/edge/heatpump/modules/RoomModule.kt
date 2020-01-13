package com.nabto.edge.heatpump.modules

import android.app.Application
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesDao
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesRepository
import com.nabto.edge.heatpump.data.source.overview.PairedDevicesRoomDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Inject

@Module
class RoomModule @Inject constructor(val application : Application){

    @Provides
    fun providesPairedDevicesRoomDatabase() : PairedDevicesRoomDatabase {
        return PairedDevicesRoomDatabase.getDatabase(application)
    }

    @Provides
    fun pairedDevicesDao(db : PairedDevicesRoomDatabase) : PairedDevicesDao {
        return db.pairedDevicesDao();
    }

    @Provides
    fun pairedDevicesRepository(dao : PairedDevicesDao) : PairedDevicesRepository {
        return PairedDevicesRepository(dao)
    }
}