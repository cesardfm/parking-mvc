package com.g3.parking;

import com.g3.parking.model.Organization;
import com.g3.parking.model.Role;
import com.g3.parking.model.User;
import com.g3.parking.repository.OrganizationRepository;
import com.g3.parking.repository.RoleRepository;
import com.g3.parking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
public class ParkingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingApplication.class, args);
	}

	 @Bean
    CommandLineRunner init(RoleRepository roleRepo, 
                          UserRepository userRepo, 
                          OrganizationRepository orgRepo,
                          PasswordEncoder encoder) {
        return args -> {
            // Verificar si ya existen datos (para evitar duplicados)
            if (roleRepo.count() > 0) {
                System.out.println("==============================================");
                System.out.println("✅ Base de datos ya contiene datos");
                System.out.println("==============================================");
                return;
            }
            
            // ==================== CREAR ROLES ====================
            Role ownerRole = new Role();
            ownerRole.setName("ROLE_OWNER");
            roleRepo.save(ownerRole);
            
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepo.save(adminRole);
            
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepo.save(userRole);

            // ==================== CREAR ORGANIZACIONES ====================
            Organization org1 = new Organization();
            org1.setName("Parkings del Centro");
            org1.setDescription("Organización de parqueaderos del centro de la ciudad");
            org1.setTaxId("123456789-0");
            org1.setAddress("Calle Principal 123");
            org1.setPhone("+57 300 1234567");
            org1.setEmail("info@parkingcentro.com");
            org1.setActive(true);
            orgRepo.save(org1);

            Organization org2 = new Organization();
            org2.setName("Parkings Norte");
            org2.setDescription("Organización de parqueaderos zona norte");
            org2.setTaxId("987654321-0");
            org2.setAddress("Avenida Norte 456");
            org2.setPhone("+57 300 9876543");
            org2.setEmail("info@parkingnorte.com");
            org2.setActive(true);
            orgRepo.save(org2);

            // ==================== CREAR USUARIOS ====================
            // Owner de la Organización 1
            User owner1 = new User();
            owner1.setUsername("owner1");
            owner1.setPassword(encoder.encode("1234"));
            owner1.setRoles(Set.of(ownerRole));
            owner1.setOrganization(org1); // Asignar organización
            owner1.setActive(true);
            userRepo.save(owner1);

            // Admin de la Organización 1
            User admin1 = new User();
            admin1.setUsername("admin1");
            admin1.setPassword(encoder.encode("1234"));
            admin1.setRoles(Set.of(adminRole));
            admin1.setOrganization(org1); // Misma organización
            admin1.setActive(true);
            userRepo.save(admin1);

            // Usuario regular de la Organización 1
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(encoder.encode("1234"));
            user1.setRoles(Set.of(userRole));
            user1.setOrganization(org1); // Misma organización
            user1.setActive(true);
            userRepo.save(user1);

            // Owner de la Organización 2
            User owner2 = new User();
            owner2.setUsername("owner2");
            owner2.setPassword(encoder.encode("1234"));
            owner2.setRoles(Set.of(ownerRole));
            owner2.setOrganization(org1); // Otra organización
            owner2.setActive(true);
            userRepo.save(owner2);

            System.out.println("==============================================");
            System.out.println("✅ Datos iniciales creados correctamente");
            System.out.println("==============================================");
            System.out.println("Organizaciones creadas: 2");
            System.out.println("  - " + org1.getName() + " (ID: " + org1.getId() + ")");
            System.out.println("  - " + org2.getName() + " (ID: " + org2.getId() + ")");
            System.out.println("\nUsuarios creados:");
            System.out.println("  - owner1/1234 (OWNER - " + org1.getName() + ")");
            System.out.println("  - admin1/1234 (ADMIN - " + org1.getName() + ")");
            System.out.println("  - user1/1234  (USER - " + org1.getName() + ")");
            System.out.println("  - owner2/1234 (OWNER - " + org2.getName() + ")");
            System.out.println("==============================================");
        };
    }
}
