package com.oodles.service.impl;

import java.util.Objects;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oodles.domains.User;
import com.oodles.repository.UserRepository;



@Service
public class UserServiceImpl implements UserDetailsService {

	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	UserRepository userRepository;
	
	
	public User loadUserByEmail(String email) {
		return userRepository.findByEmailAndIsDeleted(email, false);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmailAndIsDeleted(username, false);
		if (Objects.isNull(user))
			throw new UsernameNotFoundException("Username not found");
		Set<GrantedAuthority> userRoles = user.getRoles().stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());
		return new org.springframework.security.core.userdetails.User(username, user.getPassword(), userRoles);
	}

}
