package com.g3.parking;

import com.g3.parking.model.Role;
import com.g3.parking.model.User;
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
	CommandLineRunner init(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder encoder) {
		return args -> {
			Role owner = new Role();
			owner.setName("ROLE_OWNER");
			roleRepo.save(owner);
			Role admin = new Role();
			admin.setName("ROLE_ADMIN");
			roleRepo.save(admin);
			Role user = new Role();
			user.setName("ROLE_USER");
			roleRepo.save(user);

			User u1 = new User();
			u1.setUsername("owner");
			u1.setPassword(encoder.encode("1234"));
			u1.setRoles(Set.of(owner));
			userRepo.save(u1);

			User u2 = new User();
			u2.setUsername("admin");
			u2.setPassword(encoder.encode("1234"));
			u2.setRoles(Set.of(admin));
			userRepo.save(u2);

			User u3 = new User();
			u3.setUsername("user");
			u3.setPassword(encoder.encode("1234"));
			u3.setRoles(Set.of(user));
			userRepo.save(u3);
		};
	}
}
