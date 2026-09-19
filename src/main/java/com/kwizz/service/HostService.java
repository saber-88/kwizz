package com.kwizz.service;

import com.kwizz.entity.Host;
import com.kwizz.repository.HostRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for Host accounts - registration, lookups. Parallel to
 * QuizService: AuthController should call this rather than talking to
 * HostRepository or PasswordEncoder directly.
 *
 * Not to be confused with HostUserDetailsService, which is a different,
 * Spring-Security-specific class - that one's only job is answering
 * "who is this username, for login purposes." This one is regular app logic.
 */
@Service
public class HostService {

    private final HostRepository hostRepository;
    private final PasswordEncoder passwordEncoder;

    public HostService(HostRepository hostRepository, PasswordEncoder passwordEncoder) {
        this.hostRepository = hostRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameTaken(String username) {
        return hostRepository.findByUsername(username).isPresent();
    }

    public Host registerHost(String username, String rawPassword) {
        if (usernameTaken(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        Host host = new Host(username, passwordEncoder.encode(rawPassword));
        return hostRepository.save(host);
    }

    public Host getHostByUsername(String username) {
        return hostRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No host with username " + username));
    }
}