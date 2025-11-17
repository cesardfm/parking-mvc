package com.g3.parking;

import com.g3.parking.model.Level;
import com.g3.parking.model.Organization;
import com.g3.parking.model.Parking;
import com.g3.parking.model.Plan;
import com.g3.parking.model.Role;
import com.g3.parking.model.Site;
import com.g3.parking.model.Ticket;
import com.g3.parking.model.User;
import com.g3.parking.model.UserSubscription;
import com.g3.parking.model.Vehicle;
import com.g3.parking.model.VehicleCategory;
import com.g3.parking.repository.LevelRepository;
import com.g3.parking.repository.OrganizationRepository;
import com.g3.parking.repository.ParkingRepository;
import com.g3.parking.repository.PlanRepository;
import com.g3.parking.repository.RoleRepository;
import com.g3.parking.repository.SiteRepository;
import com.g3.parking.repository.UserSubscriptionRepository;
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

	 @Bean
    CommandLineRunner init(RoleRepository roleRepo, 
                          UserRepository userRepo, 
                          OrganizationRepository orgRepo,
						  ParkingRepository parkingRepo,
						  LevelRepository levelRepo,
						  VehicleCategoryRepository categoryRepo,
						  VehicleRepository vehicleRepo,
						  TicketRepository ticketRepo,
						  PlanRepository planRepo,
						  UserSubscriptionRepository userSubscriptionRepo,
						  SiteRepository siteRepo,
                          PasswordEncoder encoder) {
        return args -> {
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
            User owner1 = new User();
            owner1.setUsername("owner1");
            owner1.setPassword(encoder.encode("1234"));
            owner1.setRoles(Set.of(ownerRole));
            owner1.setOrganization(org1);
            owner1.setActive(true);
            userRepo.save(owner1);

            User admin1 = new User();
            admin1.setUsername("admin1");
            admin1.setPassword(encoder.encode("1234"));
            admin1.setRoles(Set.of(adminRole));
            admin1.setOrganization(org1);
            admin1.setActive(true);
            userRepo.save(admin1);

            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(encoder.encode("1234"));
            user1.setRoles(Set.of(userRole));
            user1.setOrganization(org1);
            user1.setActive(true);
            userRepo.save(user1);

            User owner2 = new User();
            owner2.setUsername("owner2");
            owner2.setPassword(encoder.encode("1234"));
            owner2.setRoles(Set.of(ownerRole));
            owner2.setOrganization(org2);
            owner2.setActive(true);
            userRepo.save(owner2);

			// ==================== CREAR PARQUEADEROS ====================
			Parking parking1 = new Parking();
			parking1.setName("Parking Central");
			parking1.setLat(12.0);
			parking1.setLng(21.0);
			parking1.setAddress("Calle 123 #45-67");
			parking1.setOrganization(org1);
			parking1.setCreatedBy(owner1);
			parking1.setAdmins(Set.of(admin1));
			parkingRepo.save(parking1);

			Parking parking2 = new Parking();
			parking2.setName("Parking Oriente");
			parking2.setLat(43.0);
			parking2.setLng(11.0);
			parking2.setAddress("Calle 35 #11-22");
			parking2.setOrganization(org1);
			parking2.setCreatedBy(owner1);
			parking2.setAdmins(Set.of(admin1));
			parkingRepo.save(parking2);

			// ==================== CREAR NIVELES ====================

			Level level1 = new Level();
			level1.setColumns(2);
			level1.setRows(2);
			level1.setParking(parking1);
			levelRepo.save(level1);

			// ==================== CREAR SITIOS ====================

			Site site_1_1 = new Site();
			site_1_1.setLevel(level1);
			site_1_1.setPosX(1);
			site_1_1.setPosY(1);
			site_1_1.setStatus("disabled");
			siteRepo.save(site_1_1);

			Site site_1_2 = new Site();
			site_1_2.setLevel(level1);
			site_1_2.setPosX(1);
			site_1_2.setPosY(2);
			site_1_2.setStatus("disabled");
			siteRepo.save(site_1_2);

			Site site_2_1 = new Site();
			site_2_1.setLevel(level1);
			site_2_1.setPosX(2);
			site_2_1.setPosY(1);
			site_2_1.setStatus("available");
			siteRepo.save(site_2_1);

			Site site_2_2 = new Site();
			site_2_2.setLevel(level1);
			site_2_2.setPosX(2);
			site_2_2.setPosY(2);
			site_2_2.setStatus("available");
			siteRepo.save(site_2_2);

			// ==================== CREAR PLANES ====================
			Plan planBasic = new Plan();
			planBasic.setName("Básico");
			planBasic.setDescription("Plan básico con tarifas estándar");
			planBasic.setPrice(new BigDecimal(50000));
			planBasic.setDiscountPercent(new BigDecimal(0.10));
			planRepo.save(planBasic);

			Plan planPremium = new Plan();
			planPremium.setName("Premium");
			planPremium.setDescription("Plan premium con mejores tarifas y beneficios");
			planPremium.setPrice(new BigDecimal(120000));
			planPremium.setDiscountPercent(new BigDecimal(0.20));
			planRepo.save(planPremium);

			Plan planEnterprise = new Plan();
			planEnterprise.setName("Enterprise");
			planEnterprise.setDescription("Plan empresarial para grandes organizaciones");
			planEnterprise.setPrice(new BigDecimal(300000));
			planEnterprise.setDiscountPercent(new BigDecimal(0.30));
			planRepo.save(planEnterprise);

			// ==================== CREAR SUSCRIPCIONES ====================
			UserSubscription subscription1 = new UserSubscription();
			subscription1.setUser(user1);
			subscription1.setPlan(planBasic);
			subscription1.setActivationDate(LocalDateTime.now().minusDays(21));
			subscription1.setDuracionMeses(2);
			subscription1.setPrecio(planBasic.getPrice().multiply(new BigDecimal(subscription1.getDuracionMeses())));
			subscription1.setStatus(com.g3.parking.model.SubscriptionStatus.ACTIVE);
			userSubscriptionRepo.save(subscription1);


			// ==================== CREAR CATEGORIAS ====================
			VehicleCategory category1 = new VehicleCategory();
			category1.setName("Carro");
			category1.setRatePerHour(new BigDecimal("3200.00"));
			categoryRepo.save(category1);

			VehicleCategory category2 = new VehicleCategory();
			category2.setName("Motocicleta");
			category2.setRatePerHour(new BigDecimal("2200.00"));
			categoryRepo.save(category2);

			VehicleCategory category3 = new VehicleCategory();
			category3.setName("Camión");
			category3.setRatePerHour(new BigDecimal("4500.00"));
			categoryRepo.save(category3);

			// ==================== CREAR VEHICULOS ====================
			Vehicle vehicle1 = new Vehicle();
			vehicle1.setLicensePlate("ABC123");
			vehicle1.setColor("Rojo");
			vehicle1.setOwner(user1);
			vehicle1.setCategory(category1);
			vehicleRepo.save(vehicle1);

			Vehicle vehicle2 = new Vehicle();
			vehicle2.setLicensePlate("XYZ789");
			vehicle2.setColor("Azul");
			vehicle2.setOwner(null);
			vehicle2.setCategory(category2);
			vehicleRepo.save(vehicle2);

			Vehicle vehicle3 = new Vehicle();
			vehicle3.setLicensePlate("LMN456");
			vehicle3.setColor("Blanco");
			vehicle3.setOwner(null);
			vehicle3.setCategory(category3);
			vehicleRepo.save(vehicle3);

			// ==================== CREAR TIQUETES ====================
			Ticket ticket1 = new Ticket();
			ticket1.setVehicle(vehicle1);
			ticket1.setSite(site_1_1);
			ticket1.setEntryTime(LocalDateTime.now().minusHours(3));
			ticket1.setExitTime(LocalDateTime.now());
			ticket1.setTotalAmount(new BigDecimal("9600.00"));
			ticket1.setPaid(true);
			ticketRepo.save(ticket1);

			Ticket ticket2 = new Ticket();
			ticket2.setVehicle(vehicle2);
			ticket2.setSite(site_1_2);
			ticket2.setEntryTime(LocalDateTime.now().minusHours(1));
			ticket2.setExitTime(null);
			ticket2.setPaid(false);
			ticketRepo.save(ticket2);

            System.out.println("==============================================");
            System.out.println(" Datos iniciales creados correctamente");
            System.out.println("Organizaciones creadas:");
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