package com.eventhub.repository;
import com.eventhub.entity.Ticket; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketRepository extends JpaRepository<Ticket,Long>{Optional<Ticket> findByTicketId(String ticketId);}
