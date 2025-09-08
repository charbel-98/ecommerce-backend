package com.charbel.ecommerce.user.repository;

import com.charbel.ecommerce.user.entity.RefreshToken;
import com.charbel.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	@Query("SELECT rt FROM RefreshToken rt WHERE rt.isDeleted = false AND rt.token = :token AND rt.revokedAt IS NULL")
	Optional<RefreshToken> findByTokenAndRevokedAtIsNull(@Param("token") String token);

	@Query("SELECT rt FROM RefreshToken rt WHERE rt.isDeleted = false AND rt.user = :user AND rt.revokedAt IS NULL")
	List<RefreshToken> findByUserAndRevokedAtIsNull(@Param("user") User user);

	@Modifying
	@Transactional
	@Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.isDeleted = false AND rt.user = :user AND rt.revokedAt IS NULL")
	void revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

	@Modifying
	@Transactional
	@Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.isDeleted = false AND rt.token = :token")
	void revokeByToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

	@Modifying
	@Transactional
	@Query("DELETE FROM RefreshToken rt WHERE rt.isDeleted = false AND rt.expiresAt < :now")
	void deleteExpiredTokens(@Param("now") LocalDateTime now);

	@Query("SELECT rt FROM RefreshToken rt WHERE rt.isDeleted = false AND rt.id = :id")
	Optional<RefreshToken> findByIdAndNotDeleted(@Param("id") UUID id);
}