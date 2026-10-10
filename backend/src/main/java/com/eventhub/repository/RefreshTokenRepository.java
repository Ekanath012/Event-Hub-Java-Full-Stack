package com.eventhub.repository;

import com.eventhub.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.time.Instant;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t where t.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    void deleteAllByUserId(Long userId);

    // Use explicit bulk-delete queries rather than derived delete methods returning a
    // primitive. This avoids the null proxy result that caused HTTP 500 during login.
    @Modifying(flushAutomatically = true)
    @Query("delete from RefreshToken t where t.user.id = :userId and t.expiresAt < :cutoff")
    int deleteExpiredForUser(@Param("userId") Long userId, @Param("cutoff") Instant cutoff);

    @Modifying(flushAutomatically = true)
    @Query("delete from RefreshToken t where t.user.id = :userId and t.revoked = true and t.createdAt < :cutoff")
    int deleteOldRevokedForUser(@Param("userId") Long userId, @Param("cutoff") Instant cutoff);
}
