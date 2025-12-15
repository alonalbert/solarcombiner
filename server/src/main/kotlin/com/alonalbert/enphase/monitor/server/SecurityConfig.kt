package com.alonalbert.enphase.monitor.server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SpringSecurityConfig(private val environment: Environment) {
    @Bean
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity.also { security ->
            security
                .csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().authenticated() }
                .httpBasic(Customizer.withDefaults())

        }.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val username = environment.getRequiredProperty("server.username")
        val password = environment.getRequiredProperty("server.password")
        val user: UserDetails = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }

    companion object {
        @Bean
        fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }
    }
}




