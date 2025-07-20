package com.alonalbert.enphase.monitor.dagger

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.util.TimberLogger
import com.alonalbert.solar.combiner.enphase.Enphase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun providesEnphase(application: TheApplication): Deferred<Enphase> {
    return MainScope().async {
      Enphase(
        application.cacheDir.toPath(),
        logger = TimberLogger(),
      )
    }
  }

  @Provides
  @Singleton
  fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
      context,
      AppDatabase::class.java,
      "enphase-monitor-database.db"
    ).build()
  }

  @Provides
  @Singleton
  fun providesApplication(application: Application) = application as TheApplication
}
