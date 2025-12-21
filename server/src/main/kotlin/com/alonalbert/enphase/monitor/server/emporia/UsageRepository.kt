package com.alonalbert.enphase.monitor.server.emporia

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface UsageRepository : JpaRepository<Usage, Long> {
  fun findByTimestampBetween(start: Instant, end: Instant): List<Usage>

  @Transactional
  fun deleteByTimestampBetween(start: Instant, end: Instant)
}