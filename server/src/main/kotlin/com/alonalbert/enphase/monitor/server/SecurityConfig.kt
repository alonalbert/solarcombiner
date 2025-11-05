package com.alonalbert.enphase.monitor.server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.get
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
        val al: UserDetails = User.builder()
            .username(environment["server.username"])
            .password(passwordEncoder().encode(environment["server.password"]))
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(al)
    }

    companion object {
        @Bean
        fun passwordEncoder(): PasswordEncoder {
            return BCryptPasswordEncoder()
        }
    }
}



