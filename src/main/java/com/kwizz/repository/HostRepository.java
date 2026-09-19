package com.kwizz.repository;

import com.kwizz.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByUsername(String username);
}
