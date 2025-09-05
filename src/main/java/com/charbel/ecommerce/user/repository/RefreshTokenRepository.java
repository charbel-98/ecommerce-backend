package com.charbel.ecommerce.user.repository;

import com.charbel.ecommerce.user.entity.RefreshToken;
import com.charbel.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenAndRevokedAtIsNull(String token);

	List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

	@Modifying
	@Transactional
	@Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.revokedAt IS NULL")
	void revokeAllUserTokens(User user, LocalDateTime revokedAt);

	@Modifying
	@Transactional
	@Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.token = :token")
	void revokeByToken(String token, LocalDateTime revokedAt);

	@Modifying
	@Transactional
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
	void deleteExpiredTokens(LocalDateTime now);
}
