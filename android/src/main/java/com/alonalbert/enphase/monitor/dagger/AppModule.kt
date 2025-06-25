package com.alonalbert.enphase.monitor.dagger

import android.app.Application
import com.alonalbert.enphase.monitor.TheApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun providesEnphase(application: TheApplication) = application.enphase

  @Provides
  @Singleton
  fun providesApplication(application: Application) = application as TheApplication
}
