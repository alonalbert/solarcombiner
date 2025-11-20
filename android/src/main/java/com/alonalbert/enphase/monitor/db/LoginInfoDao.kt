package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginInfoDao: KeyValueDao {
  @Query(
    """
    SELECT 
      (SELECT value FROM KeyValue WHERE name='server') as server, 
      (SELECT value FROM KeyValue WHERE name='username') as username, 
      (SELECT value FROM KeyValue WHERE name='password') as password
      WHERE TRUE 
        AND server IS NOT NULL
        AND username IS NOT NULL
        AND password IS NOT NULL
    """
  )
  fun flow(): Flow<LoginInfo?>

  @Transaction
  suspend fun update(loginInfo: LoginInfo) {
    upsert("server", loginInfo.server)
    upsert("username", loginInfo.username)
    upsert("password", loginInfo.password)
  }
}