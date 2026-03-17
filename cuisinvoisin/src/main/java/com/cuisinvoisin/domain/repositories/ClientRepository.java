// com/cuisinvoisin/domain/repositories/ClientRepository.java
package com.cuisinvoisin.domain.repositories;

import com.cuisinvoisin.domain.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {}
