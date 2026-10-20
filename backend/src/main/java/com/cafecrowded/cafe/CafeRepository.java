package com.cafecrowded.cafe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CafeRepository extends JpaRepository<Cafe, Long> {

	// All user input is bound as parameters. `pattern` must already have LIKE wildcards escaped with '\'.
	@Query("""
		select c from Cafe c
		where c.latitude between :minLat and :maxLat
		  and c.longitude between :minLng and :maxLng
		  and (lower(c.name) like :pattern escape '\\'
		       or lower(coalesce(c.address, '')) like :pattern escape '\\')
		""")
	Page<Cafe> search(@Param("pattern") String pattern,
			@Param("minLat") double minLat, @Param("maxLat") double maxLat,
			@Param("minLng") double minLng, @Param("maxLng") double maxLng,
			Pageable pageable);
}
