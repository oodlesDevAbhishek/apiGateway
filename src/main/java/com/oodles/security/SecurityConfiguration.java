package com.oodles.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.oodles.service.impl.UserServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	@Autowired
	UserServiceImpl userDetailsService;
	@Autowired
	JwtUtil jwtUtil;
	@Autowired
	CryptionService cryptionService;

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.cors().and().csrf().disable().headers().frameOptions().disable();
//		http.authorizeRequests()
//				.antMatchers("/scaffold_communication/chat/meeting/**", "/scaffold_communication/chat/sockjs/**",
//						"/chat/businessLead/**", "/v1/public/**", "/v1/notification/message/**", "/monitoring",
//						"/chat/auth/authorize", "/zuul/**")
//				.permitAll().anyRequest().authenticated().and()
//				.addFilter(new ScaffoldAuthenticationFilter(authenticationManager(), userDetailsService, jwtUtil,
//						cryptionService))
//				.addFilterAfter(new PerRequestFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
//				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//	}

	/*
	 * @Override public void configure(WebSecurity web) throws Exception {
	 * web.ignoring().antMatchers("/swagger-ui.html/**", "/webjars/**", "/v2/**",
	 * "/swagger-resources/**", "/swagger.json",
	 * "/swagger-resources/configuration/ui"); }
	 */
	/*
	 * @Override protected void configure(AuthenticationManagerBuilder auth) throws
	 * Exception { auth.userDetailsService(userDetailsService)
	 * .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
	 * }
	 */

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable().headers().frameOptions().disable();
		http.authorizeRequests()
				.antMatchers("/scaffold_communication/chat/meeting/**", "/scaffold_communication/chat/sockjs/**",
						"/chat/businessLead/**", "/v1/public/**", "/v1/notification/message/**", "/monitoring",
						"/chat/auth/authorize")
				.permitAll().anyRequest().authenticated().and()
				.addFilter(new ScaffoldAuthenticationFilter(userDetailsService, jwtUtil, cryptionService))
				.addFilterAfter(new PerRequestFilter(jwtUtil, handlerExceptionResolver()), UsernamePasswordAuthenticationFilter.class)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().antMatchers("/swagger-ui.html/**", "/webjars/**", "/v2/**",
				"/swagger-resources/**", "/swagger.json", "/swagger-resources/configuration/ui");
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}


	@Bean
	public ExceptionHandlerExceptionResolver handlerExceptionResolver() {
		return new ExceptionHandlerExceptionResolver();
	}
}
