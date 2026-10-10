package com.eventhub.repository;
import com.eventhub.entity.Attendance; import org.springframework.data.jpa.repository.JpaRepository;
public interface AttendanceRepository extends JpaRepository<Attendance,Long>{boolean existsByRegistrationId(Long registrationId);long countByEventId(Long eventId);}
