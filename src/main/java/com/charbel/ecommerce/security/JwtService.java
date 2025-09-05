package com.charbel.ecommerce.security;

import com.charbel.ecommerce.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

	@Value("${security.jwt.secret}")
	private String secret;

	@Value("${security.jwt.expiration}")
	private long jwtExpiration;

	@Value("${security.jwt.refresh-expiration}")
	private long refreshExpiration;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateAccessToken(User user) {
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("userId", user.getId().toString());
		extraClaims.put("role", user.getRole().name());
		extraClaims.put("firstName", user.getFirstName());
		extraClaims.put("lastName", user.getLastName());

		return generateToken(extraClaims, user.getEmail(), jwtExpiration);
	}

	public String generateRefreshToken(User user) {
		return generateToken(new HashMap<>(), user.getEmail(), refreshExpiration);
	}

	public String generateToken(Map<String, Object> extraClaims, String userEmail, long expiration) {
		Date now = new Date(System.currentTimeMillis());
		Date expiryDate = new Date(System.currentTimeMillis() + expiration);

		return Jwts.builder().setClaims(extraClaims).setSubject(userEmail).setIssuedAt(now).setExpiration(expiryDate)
				.signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	public boolean isTokenExpired(String token) {
		try {
			return extractExpiration(token).before(new Date());
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return true; // Token is expired
		}
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public LocalDateTime getAccessTokenExpiration() {
		return LocalDateTime.now().plusSeconds(jwtExpiration / 1000);
	}

	public LocalDateTime getRefreshTokenExpiration() {
		return LocalDateTime.now().plusSeconds(refreshExpiration / 1000);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = secret.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
