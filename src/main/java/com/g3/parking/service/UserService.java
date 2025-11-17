package com.g3.parking.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.model.Organization;
import com.g3.parking.model.Role;
import com.g3.parking.model.User;
import com.g3.parking.repository.OrganizationRepository;
import com.g3.parking.repository.RoleRepository;
import com.g3.parking.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static Integer counter = 0;
    private final OrganizationRepository organizationRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, OrganizationRepository organizationrRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationrRepository;
        
    }

    public User findByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return user;
    }
    
    public void createUser(String username, String password, String roleName, Organization organization) {
        // Verificar si el usuario ya existe
        // System.out.println("name: " + username + "Pass: " + password + " Role: " + roleName + " Contador: " + counter);
        // Organization organization = organizationRepository.findById(1L).get();

        if(organization == null || organization.getId() == null){
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // if (userRepository.findByUsername(username).isPresent()) {
        //     throw new RuntimeException("El nombre de usuario ya existe");
        // }
        
        Role role = roleRepository.findByName("ROLE_" + roleName);
        if (role == null) {
            throw new RuntimeException("Rol no encontrado: " + roleName);
        }
        // Crear nuevo usuario
        User newUser = new User();
        newUser.setUsername(username.trim().toLowerCase());
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRoles(Set.of(role));
        newUser.setOrganization(organization);

        userRepository.save(newUser);
    }
    
    public List<User> findByOrganization(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    public UserDTO findById(Long userId){
        User user = userRepository.getReferenceById(userId);
        return convert(user, UserDTO.class);
    }
}
