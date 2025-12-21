package com.alonalbert.enphase.monitor.server.emporia

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface UsageRepository : JpaRepository<Usage, Long> {
  fun findByTimestampGreaterThanEqualAndTimestampLessThan(start: Instant, end: Instant): List<Usage>

  fun getUsages(start: Instant, end: Instant) =
    findByTimestampGreaterThanEqualAndTimestampLessThan(start, end)

  @Transactional
  fun deleteByTimestampGreaterThanEqualAndTimestampLessThan(start: Instant, end: Instant)

  fun deleteUsages(start: Instant, end: Instant) =
    deleteByTimestampGreaterThanEqualAndTimestampLessThan(start, end)
}