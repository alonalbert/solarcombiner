package com.alonalbert.enphase.monitor.dagger

import android.app.Application
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.settings.dataStore
import com.alonalbert.enphase.monitor.settings.email
import com.alonalbert.enphase.monitor.settings.exportHost
import com.alonalbert.enphase.monitor.settings.exportPort
import com.alonalbert.enphase.monitor.settings.exportSerialNum
import com.alonalbert.enphase.monitor.settings.exportSiteId
import com.alonalbert.enphase.monitor.settings.mainHost
import com.alonalbert.enphase.monitor.settings.mainPort
import com.alonalbert.enphase.monitor.settings.mainSerialNum
import com.alonalbert.enphase.monitor.settings.mainSiteId
import com.alonalbert.enphase.monitor.settings.password
import com.alonalbert.solar.combiner.enphase.Enphase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun providesEnphase(application: TheApplication): Deferred<Enphase> {
    return MainScope().async {
      val preferences = application.dataStore.data.first()
      Enphase(
        preferences.email,
        preferences.password,
        preferences.mainSiteId,
        preferences.mainSerialNum,
        preferences.mainHost,
        preferences.mainPort,
        preferences.exportSiteId,
        preferences.exportSerialNum,
        preferences.exportHost,
        preferences.exportPort,
        application.cacheDir.toPath(),
      )
    }
  }

  @Provides
  @Singleton
  fun providesApplication(application: Application) = application as TheApplication
}
