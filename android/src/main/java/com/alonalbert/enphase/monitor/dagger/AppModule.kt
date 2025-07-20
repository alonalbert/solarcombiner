package com.alonalbert.enphase.monitor.dagger

import android.app.Application
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.util.TimberLogger
import com.alonalbert.solar.combiner.enphase.Enphase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
  fun providesApplication(application: Application) = application as TheApplication
}
