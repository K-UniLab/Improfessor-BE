package org.unilab.improfessorbe.global.security.jwt;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (CustomException e) {
			log.error("JWT Exception: {}", e.getErrorCode().getMessage());
			throw new CustomException(e.getErrorCode());
		} catch (Exception e) {
			log.error("Filter Chain ERrir: {}", e.getMessage());
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
