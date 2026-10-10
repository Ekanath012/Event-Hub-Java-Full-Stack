package com.eventhub.repository;

import com.eventhub.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("update PasswordResetToken t set t.usedAt = :usedAt where t.user.id = :userId and t.usedAt is null")
    int markUnusedTokensAsUsed(@Param("userId") Long userId, @Param("usedAt") Instant usedAt);

    @Modifying(flushAutomatically = true)
    @Query("delete from PasswordResetToken t where t.expiresAt < :cutoff or (t.usedAt is not null and t.usedAt < :cutoff)")
    int deleteExpiredOrOldUsed(@Param("cutoff") Instant cutoff);
}
