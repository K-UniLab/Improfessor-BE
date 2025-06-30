package org.unilab.improfessorbe.global.security.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;
import org.unilab.improfessorbe.global.util.RedisUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	private final RedisUtil redisUtil;
	private final Key key;

	public JwtTokenProvider(
		RedisUtil redisUtil,
		@Value("${spring.jwt.secret}") String secretKey
		) {
		this.redisUtil = redisUtil;
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public JwtToken generateToken(Authentication authentication) {

		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = System.currentTimeMillis();

		Map<String, Object> headers = new HashMap<>();
		headers.put("typ", "JWT");
		headers.put("alg", "HS256");

		String accessToken = Jwts.builder()
			.setHeader(headers)
			.setSubject("accessToken")
			.claim("iss", "off")
			.claim("aud", authentication.getName())
			.claim("auth", authorities)
			.setExpiration(new Date(now+18000000))
			.setIssuedAt(new Date())
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		String refreshToken = Jwts.builder()
			.setHeader(headers)
			.setSubject("refreshToken")
			.claim("iss", "off")
			.claim("aud", authentication.getName())
			.claim("auth", authorities)
			.claim("add", "ref")
			.setExpiration(new Date(now+604800000))
			.setIssuedAt(new Date())
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		return JwtToken.builder()
			.grantType("Bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	public Authentication getAuthentication(String token){
		Claims claims = parseClaims(token);

		if(claims.get("auth") == null){
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
			.map(SimpleGrantedAuthority::new).collect(Collectors.toList());

		UserDetails principal = new User((String)claims.get("aud"), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}


	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}


	public boolean validateToken(String token){
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

			if (redisUtil.existData(token)) {
				log.warn("Access Token was registed at the blacklist: {}", token);
				throw new CustomException(ErrorCode.ACCESS_TOKEN_BLACKLISTED);
			}

			return true;
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.TOKEN_EXPIRED);
		} catch (Exception e){
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}


	public JwtToken refreshToken(String refreshToken){
		try{
			validateToken(refreshToken);

			Authentication authentication = getAuthentication(refreshToken);
			String name = authentication.getName();

			if(!redisUtil.existData(name)){
				log.warn("Refresh Token not found");
				throw new CustomException(ErrorCode.INVALID_TOKEN);
			}
			else{
				if(!redisUtil.getData(name).equals(refreshToken)){
					log.warn("Refresh Token mismatch");
					throw new CustomException(ErrorCode.INVALID_TOKEN);
				}
			}

			redisUtil.deleteData(name);
			JwtToken newTokens = generateToken(authentication);

			long refreshTokenExpirationMillis = 604800000L;
			redisUtil.setDataExpire(name, newTokens.getRefreshToken(), refreshTokenExpirationMillis / 1000);

			return newTokens;
		} catch (CustomException e){
			throw e;
		} catch (Exception e) {
			log.error("An unexpected error occurred during token refresh: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	public Long getExpiration(String token) {
		try {
			Claims claims = parseClaims(token);
			Date expiration = claims.getExpiration();
			long now = (new Date()).getTime();
			long remaining = expiration.getTime() - now;
			return Math.max(0L, remaining);
		} catch (ExpiredJwtException e) {
			log.warn("Token already expired", e.getMessage());
			return 0L;
		} catch (Exception e) {
			log.error("An unexpected error occurred during get Expiration from token: {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
}
