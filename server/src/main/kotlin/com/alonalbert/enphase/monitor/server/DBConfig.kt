package com.alonalbert.enphase.monitor.server

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


@Configuration
class DBConfig {
  @Bean
  fun dataSource(): DataSource {
    val dataSourceBuilder = DataSourceBuilder.create()
    dataSourceBuilder.driverClassName("org.sqlite.JDBC")
    dataSourceBuilder.url("jdbc:sqlite:database.db")
    return dataSourceBuilder.build()
  }
}