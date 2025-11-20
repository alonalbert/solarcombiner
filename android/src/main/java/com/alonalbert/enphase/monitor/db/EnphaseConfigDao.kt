package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Dao
interface EnphaseConfigDao: KeyValueDao {
  @Query(
    """
    SELECT 
      (SELECT value FROM KeyValue WHERE name='email') as email, 
      (SELECT value FROM KeyValue WHERE name='enphasePassword') as password, 
      (SELECT value FROM KeyValue WHERE name='mainSiteId') as mainSiteId, 
      (SELECT value FROM KeyValue WHERE name='mainSerialNumber') as mainSerialNumber, 
      (SELECT value FROM KeyValue WHERE name='mainHost') as mainHost, 
      (SELECT value FROM KeyValue WHERE name='mainPort') as mainPort, 
      (SELECT value FROM KeyValue WHERE name='exportSiteId') as exportSiteId, 
      (SELECT value FROM KeyValue WHERE name='exportSerialNumber') as exportSerialNumber, 
      (SELECT value FROM KeyValue WHERE name='exportHost') as exportHost, 
      (SELECT value FROM KeyValue WHERE name='exportPort') as exportPort
      WHERE TRUE 
        AND email IS NOT NULL
        AND password IS NOT NULL
        AND mainSiteId IS NOT NULL
        AND mainSerialNumber IS NOT NULL
        AND mainHost IS NOT NULL
        AND mainPort IS NOT NULL
        AND exportSiteId IS NOT NULL
        AND exportSerialNumber IS NOT NULL
        AND exportHost IS NOT NULL
        AND exportPort IS NOT NULL
    """
  )
  fun flow(): Flow<EnphaseConfig?>

  suspend fun get(): EnphaseConfig? = flow().firstOrNull()

  @Transaction
  suspend fun update(enphaseConfig: EnphaseConfig) {
    upsert("email", enphaseConfig.email)
    upsert("enphasePassword", enphaseConfig.password)
    upsert("mainSiteId", enphaseConfig.mainSiteId)
    upsert("mainSerialNumber", enphaseConfig.mainSerialNumber)
    upsert("mainHost", enphaseConfig.mainHost)
    upsert("mainPort", enphaseConfig.mainPort.toString())
    upsert("exportSiteId", enphaseConfig.exportSiteId)
    upsert("exportSerialNumber", enphaseConfig.exportSerialNumber)
    upsert("exportHost", enphaseConfig.exportHost)
    upsert("exportPort", enphaseConfig.exportPort.toString())
  }
}