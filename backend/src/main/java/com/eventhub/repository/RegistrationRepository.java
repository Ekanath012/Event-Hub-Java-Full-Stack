package com.eventhub.repository;
import com.eventhub.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface RegistrationRepository extends JpaRepository<Registration,Long>{
 boolean existsByEventIdAndUserIdAndStatusIn(Long eventId,Long userId,Collection<RegistrationStatus> statuses);
 long countByEventIdAndStatusIn(Long eventId,Collection<RegistrationStatus> statuses);
 Optional<Registration> findByEventIdAndUserId(Long eventId,Long userId);
 Page<Registration> findByUserIdOrderByRegisteredAtDesc(Long userId,Pageable pageable);
 List<Registration> findByEventIdAndStatusIn(Long eventId,Collection<RegistrationStatus> statuses);
 long countByStatus(RegistrationStatus status);
 @Query("select r from Registration r join fetch r.event e join fetch r.user u where r.id=:id") Optional<Registration> findDetailed(@Param("id") Long id);
}
