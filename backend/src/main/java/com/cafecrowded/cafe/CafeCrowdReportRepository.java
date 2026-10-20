package com.cafecrowded.cafe;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeCrowdReportRepository extends JpaRepository<CafeCrowdReport, Long> {

	List<CafeCrowdReport> findByCafeIdInAndObservedAtAfter(Collection<Long> cafeIds, Instant since);

	boolean existsByCafeIdAndReporterHashAndObservedAtAfter(Long cafeId, String reporterHash, Instant since);

	long countByReporterHashAndObservedAtAfter(String reporterHash, Instant since);
}
