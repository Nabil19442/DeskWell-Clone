package com.deskwell.web.repository;

import com.deskwell.web.model.BlockedApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlockedAppRepository extends JpaRepository<BlockedApp, Long> {
    List<BlockedApp> findByIsPermanentTrue();
}
