package com.kwizz.service;

import com.kwizz.entity.Host;
import com.kwizz.repository.HostRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security calls loadUserByUsername() during login. It compares the
 * password the user typed against getPassword() below using whatever
 * PasswordEncoder bean exists (BCryptPasswordEncoder, defined in
 * SecurityConfig) - we never compare passwords ourselves.
 */

@Service
public class HostUserDetailsService implements UserDetailsService {

    private final HostRepository hostRepository;

    public HostUserDetailsService(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Host host = hostRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No host with username " + username));


        return User.withUsername(host.getUsername())
                .password(host.getPassword())
                .roles("HOST")
                .build();
    }
}
