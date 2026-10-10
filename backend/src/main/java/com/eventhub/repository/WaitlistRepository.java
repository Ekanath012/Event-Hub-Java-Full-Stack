package com.eventhub.repository;
import com.eventhub.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface WaitlistRepository extends JpaRepository<WaitlistEntry,Long>{
 Optional<WaitlistEntry> findByEventIdAndUserId(Long eventId,Long userId);
 @Query("select coalesce(max(w.position),0) from WaitlistEntry w where w.event.id=:eventId and w.status=com.eventhub.entity.WaitlistStatus.WAITING") int maxPosition(@Param("eventId") Long eventId);
 Optional<WaitlistEntry> findFirstByEventIdAndStatusOrderByPositionAsc(Long eventId,WaitlistStatus status);
 Page<WaitlistEntry> findByEventIdOrderByPositionAsc(Long eventId,Pageable pageable);
 long countByStatus(WaitlistStatus status);
}
