package com.g3.parking.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.g3.parking.datatransfer.OrganizationDTO;
import com.g3.parking.datatransfer.UserDTO;
import com.g3.parking.model.Organization;
import com.g3.parking.model.Role;
import com.g3.parking.model.User;
import com.g3.parking.repository.RoleRepository;
import com.g3.parking.repository.UserRepository;
import com.g3.parking.repository.OrganizationRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            OrganizationRepository organizationrRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        // this.organizationRepository = organizationrRepository;

    }

    public UserDTO findByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return convert(user, UserDTO.class);
    }

    public void createUser(String username, String password, String roleName, OrganizationDTO organization) {
        // Verificar si el usuario ya existe
        // System.out.println("name: " + username + "Pass: " + password + " Role: " +
        // roleName + " Contador: " + counter);
        // Organization organization = organizationRepository.findById(1L).get();

        if (organization == null || organization.getId() == null) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // if (userRepository.findByUsername(username).isPresent()) {
        // throw new RuntimeException("El nombre de usuario ya existe");
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
        newUser.setOrganization(convert(organization, Organization.class));

        userRepository.save(newUser);
    }

    public List<UserDTO> findByOrganization(Long organizationId) {
        List<User> users = userRepository.findByOrganizationId(organizationId);
        return users.stream()
                .map(user -> convert(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    public UserDTO findById(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return convert(user, UserDTO.class);
    }
}
