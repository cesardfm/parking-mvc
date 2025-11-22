package com.g3.parking.service;

import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.model.User;
import com.g3.parking.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService extends BaseService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true) 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return new MainUser(convert(user, UserDTO.class));
    }
}
