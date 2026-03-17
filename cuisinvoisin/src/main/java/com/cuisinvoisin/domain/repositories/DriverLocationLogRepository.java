// com/cuisinvoisin/domain/repositories/DriverLocationLogRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.DriverLocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DriverLocationLogRepository extends JpaRepository<DriverLocationLog, UUID> {}
