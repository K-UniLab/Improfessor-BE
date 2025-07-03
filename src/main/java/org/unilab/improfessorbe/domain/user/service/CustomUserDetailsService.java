package org.unilab.improfessorbe.domain.user.service;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.unilab.improfessorbe.domain.user.domain.User;
import org.unilab.improfessorbe.domain.user.repository.UserRepository;
import org.unilab.improfessorbe.global.exception.CustomException;
import org.unilab.improfessorbe.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		Long id = Long.parseLong(userId);
		User user = userRepository.findByUserIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		List<SimpleGrantedAuthority> authorities = Collections.singletonList(
			new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
		);

		return new org.springframework.security.core.userdetails.User(
			user.getUserId().toString(),
			user.getPassword(),
			authorities
		);
	}
}