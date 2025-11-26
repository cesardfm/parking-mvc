package com.g3.parking;

import com.g3.parking.model.Level;
import com.g3.parking.model.Organization;
import com.g3.parking.model.Parking;
import com.g3.parking.model.Plan;
import com.g3.parking.model.Role;
import com.g3.parking.model.Site;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.model.Subscription;
import com.g3.parking.model.Vehicle;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.LevelRepository;
import com.g3.parking.repository.OrganizationRepository;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.repository.PlanRepository;
import com.g3.parking.repository.RoleRepository;
import com.g3.parking.repository.SiteRepository;
import com.g3.parking.repository.SubscriptionRepository;
import com.g3.parking.repository.TicketRepository;
import com.g3.parking.repository.UserRepository;
import com.g3.parking.repository.VehicleCategoryRepository;
import com.g3.parking.repository.VehicleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@SpringBootApplication
public class ParkingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingApplication.class, args);
	}


}