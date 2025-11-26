package com.g3.parking.config;

import com.g3.parking.model.*;
import com.g3.parking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(RoleRepository roleRepo,
                                UserRepository userRepo,
                                OrganizationRepository orgRepo,
                                ParkingRepository parkingRepo,
                                LevelRepository levelRepo,
                                VehicleCategoryRepository categoryRepo,
                                VehicleRepository vehicleRepo,
                                TicketRepository ticketRepo,
                                PlanRepository planRepo,
                                SubscriptionRepository subscriptionRepo,
                                SiteRepository siteRepo,
                                PasswordEncoder encoder) {
        return args -> {
            // ==================== CREAR ROLES ====================
            Role ownerRole = createRole(roleRepo, "ROLE_OWNER");
            Role adminRole = createRole(roleRepo, "ROLE_ADMIN");
            Role userRole = createRole(roleRepo, "ROLE_USER");

            // ==================== CREAR ORGANIZACIONES ====================
            Organization org1 = createOrganization(orgRepo, "Parkings del Centro",
                    "Organización de parqueaderos del centro de la ciudad",
                    "123456789-0", "Calle Principal 123", "+57 300 1234567",
                    "info@parkingcentro.com");

            Organization org2 = createOrganization(orgRepo, "Parkings Norte",
                    "Organización de parqueaderos zona norte",
                    "987654321-0", "Avenida Norte 456", "+57 300 9876543",
                    "info@parkingnorte.com");

            // ==================== CREAR USUARIOS ====================
            User owner1 = createUser(userRepo, encoder, "owner1", "1234", Set.of(ownerRole), org1);
            User admin1 = createUser(userRepo, encoder, "admin1", "1234", Set.of(adminRole), org1);
            User user1 = createUser(userRepo, encoder, "user1", "1234", Set.of(userRole), org1);
            User owner2 = createUser(userRepo, encoder, "owner2", "1234", Set.of(ownerRole), org2);

            // ==================== CREAR PARQUEADEROS ====================
            Parking parking1 = createParking(parkingRepo, "Parking Central", 4.1420, -73.6266,
                    "Calle 123 #45-67", org1, owner1, Set.of(admin1));

            Parking parking2 = createParking(parkingRepo, "Parking Oriente", 4.1520, -73.6166,
                    "Calle 35 #11-22", org1, owner1, Set.of(admin1));

            // ==================== CREAR NIVELES ====================
            Level level1 = createLevel(levelRepo, 2, 2, parking1);

            // ==================== CREAR SITIOS ====================
            Site site_1_1 = createSite(siteRepo, level1, 1, 1, "occupied");
            Site site_1_2 = createSite(siteRepo, level1, 1, 2, "occupied");
            Site site_2_1 = createSite(siteRepo, level1, 2, 1, "available");
            Site site_2_2 = createSite(siteRepo, level1, 2, 2, "available");

            // ==================== CREAR PLANES ====================
            Plan planBasic = createPlan(planRepo, "Básico",
                    "Plan básico con descuentos en tarifas económicas estándar",
                    new BigDecimal(50000), new BigDecimal(0.10));

            Plan planPremium = createPlan(planRepo, "Premium",
                    "Plan premium con mejores tarifas y beneficios",
                    new BigDecimal(120000), new BigDecimal(0.20));

            Plan planEnterprise = createPlan(planRepo, "Enterprise",
                    "Plan empresarial para grandes organizaciones",
                    new BigDecimal(300000), new BigDecimal(0.30));

            // ==================== CREAR SUSCRIPCIONES ====================
            createSubscription(subscriptionRepo, user1, planBasic,
                    LocalDateTime.now().minusDays(21), 2, SubscriptionStatus.ACTIVE);

            createSubscription(subscriptionRepo, user1, planPremium,
                    LocalDateTime.now().minusDays(32), 1, SubscriptionStatus.EXPIRED);

            // ==================== CREAR CATEGORÍAS ====================
            VehicleCategory category1 = createCategory(categoryRepo, "Carro", new BigDecimal("3200.00"));
            VehicleCategory category2 = createCategory(categoryRepo, "Motocicleta", new BigDecimal("2200.00"));
            VehicleCategory category3 = createCategory(categoryRepo, "Camión", new BigDecimal("4500.00"));

            // ==================== CREAR VEHÍCULOS ====================
            Vehicle vehicle1 = createVehicle(vehicleRepo, "ABC123", "Rojo", user1, category1);
            Vehicle vehicle2 = createVehicle(vehicleRepo, "XYZ789", "Azul", null, category2);
            Vehicle vehicle3 = createVehicle(vehicleRepo, "LMN456", "Blanco", null, category3);
            Vehicle vehicle4 = createVehicle(vehicleRepo, "DEF789", "Negro", user1, category1);
            Vehicle vehicle5 = createVehicle(vehicleRepo, "GHI012", "Gris", null, category2);
            Vehicle vehicle6 = createVehicle(vehicleRepo, "JKL345", "Verde", null, category1);
            Vehicle vehicle7 = createVehicle(vehicleRepo, "MNO678", "Amarillo", null, category3);
            Vehicle vehicle8 = createVehicle(vehicleRepo, "PQR901", "Plata", null, category2);
            Vehicle vehicle9 = createVehicle(vehicleRepo, "STU234", "Rojo", null, category1);
            Vehicle vehicle10 = createVehicle(vehicleRepo, "VWX567", "Azul", null, category2);

            // ==================== CREAR TICKETS DE AYER ====================
            createTicket(ticketRepo, vehicle1, site_1_1,
                    LocalDateTime.now().minusDays(1).minusHours(5),
                    LocalDateTime.now().minusDays(1).minusHours(2),
                    new BigDecimal("9600.00"), true);

            createTicket(ticketRepo, vehicle2, site_1_2,
                    LocalDateTime.now().minusDays(1).minusHours(4),
                    LocalDateTime.now().minusDays(1).minusHours(1),
                    new BigDecimal("6600.00"), true);

            createTicket(ticketRepo, vehicle3, site_2_1,
                    LocalDateTime.now().minusDays(1).minusHours(6),
                    LocalDateTime.now().minusDays(1).minusHours(1),
                    new BigDecimal("22500.00"), true);

            createTicket(ticketRepo, vehicle4, site_2_2,
                    LocalDateTime.now().minusDays(1).minusHours(3),
                    LocalDateTime.now().minusDays(1),
                    new BigDecimal("9600.00"), true);

            createTicket(ticketRepo, vehicle5, site_1_1,
                    LocalDateTime.now().minusDays(1).minusHours(2),
                    LocalDateTime.now().minusDays(1).plusHours(1),
                    new BigDecimal("6600.00"), true);

            // ==================== CREAR TICKETS DE HOY (PAGADOS) ====================
            createTicket(ticketRepo, vehicle6, site_1_1,
                    LocalDateTime.now().minusHours(4),
                    LocalDateTime.now().minusHours(1),
                    new BigDecimal("9600.00"), true);

            createTicket(ticketRepo, vehicle7, site_1_2,
                    LocalDateTime.now().minusHours(3),
                    LocalDateTime.now().minusMinutes(30),
                    new BigDecimal("13500.00"), true);

            createTicket(ticketRepo, vehicle8, site_2_1,
                    LocalDateTime.now().minusHours(5),
                    LocalDateTime.now().minusHours(2),
                    new BigDecimal("6600.00"), true);

            createTicket(ticketRepo, vehicle9, site_2_2,
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusMinutes(15),
                    new BigDecimal("6400.00"), true);

            // ==================== CREAR TICKETS ACTIVOS (SIN PAGAR) ====================
            createTicket(ticketRepo, vehicle10, site_1_1,
                    LocalDateTime.now().minusHours(1), null, null, false);

            createTicket(ticketRepo, vehicle1, site_1_2,
                    LocalDateTime.now().minusMinutes(45), null, null, false);

            // ==================== CREAR TICKETS DEL MES ====================
            createTicket(ticketRepo, vehicle2, site_2_1,
                    LocalDateTime.now().minusDays(5).minusHours(3),
                    LocalDateTime.now().minusDays(5).minusHours(1),
                    new BigDecimal("4400.00"), true);

            createTicket(ticketRepo, vehicle3, site_2_2,
                    LocalDateTime.now().minusDays(8).minusHours(4),
                    LocalDateTime.now().minusDays(8).minusHours(1),
                    new BigDecimal("13500.00"), true);

            createTicket(ticketRepo, vehicle4, site_1_1,
                    LocalDateTime.now().minusDays(12).minusHours(2),
                    LocalDateTime.now().minusDays(12).minusMinutes(30),
                    new BigDecimal("4800.00"), true);

            createTicket(ticketRepo, vehicle5, site_1_2,
                    LocalDateTime.now().minusDays(15).minusHours(5),
                    LocalDateTime.now().minusDays(15).minusHours(2),
                    new BigDecimal("6600.00"), true);

            createTicket(ticketRepo, vehicle6, site_2_1,
                    LocalDateTime.now().minusDays(20).minusHours(6),
                    LocalDateTime.now().minusDays(20).minusHours(1),
                    new BigDecimal("16000.00"), true);

            createTicket(ticketRepo, vehicle7, site_2_2,
                    LocalDateTime.now().minusDays(25).minusHours(4),
                    LocalDateTime.now().minusDays(25),
                    new BigDecimal("18000.00"), true);

            System.out.println("==============================================");
            System.out.println(" Datos iniciales creados correctamente");
            System.out.println("Organizaciones: " + org1.getName() + ", " + org2.getName());
            System.out.println("Usuarios: owner1, admin1, user1, owner2 (password: 1234)");
            System.out.println("==============================================");
        };
    }

    // ==================== MÉTODOS HELPER ====================

    private Role createRole(RoleRepository repo, String name) {
        Role role = new Role();
        role.setName(name);
        return repo.save(role);
    }

    private Organization createOrganization(OrganizationRepository repo, String name, String description,
                                            String taxId, String address, String phone, String email) {
        Organization org = new Organization();
        org.setName(name);
        org.setDescription(description);
        org.setTaxId(taxId);
        org.setAddress(address);
        org.setPhone(phone);
        org.setEmail(email);
        org.setActive(true);
        return repo.save(org);
    }

    private User createUser(UserRepository repo, PasswordEncoder encoder, String username,
                            String password, Set<Role> roles, Organization org) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRoles(roles);
        user.setOrganization(org);
        user.setActive(true);
        return repo.save(user);
    }

    private Parking createParking(ParkingRepository repo, String name, double lat, double lng,
                                  String address, Organization org, User createdBy, Set<User> admins) {
        Parking parking = new Parking();
        parking.setName(name);
        parking.setLat(lat);
        parking.setLng(lng);
        parking.setAddress(address);
        parking.setOrganization(org);
        parking.setCreatedBy(createdBy);
        parking.setAdmins(admins);
        return repo.save(parking);
    }

    private Level createLevel(LevelRepository repo, int columns, int rows, Parking parking) {
        Level level = new Level();
        level.setColumns(columns);
        level.setRows(rows);
        level.setParking(parking);
        return repo.save(level);
    }

    private Site createSite(SiteRepository repo, Level level, int posX, int posY, String status) {
        Site site = new Site();
        site.setLevel(level);
        site.setPosX(posX);
        site.setPosY(posY);
        site.setStatus(status);
        return repo.save(site);
    }

    private Plan createPlan(PlanRepository repo, String name, String description,
                           BigDecimal price, BigDecimal discountPercent) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setDescription(description);
        plan.setPrice(price);
        plan.setDiscountPercent(discountPercent);
        return repo.save(plan);
    }

    private void createSubscription(SubscriptionRepository repo, User user, Plan plan,
                                    LocalDateTime activationDate, int monthsDuration, SubscriptionStatus status) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setActivationDate(activationDate);
        subscription.setMonthsDuration(monthsDuration);
        subscription.setPrice(plan.getPrice().multiply(new BigDecimal(monthsDuration)));
        subscription.setStatus(status);
        repo.save(subscription);
    }

    private VehicleCategory createCategory(VehicleCategoryRepository repo, String name, BigDecimal ratePerHour) {
        VehicleCategory category = new VehicleCategory();
        category.setName(name);
        category.setRatePerHour(ratePerHour);
        category.setActive(true);
        return repo.save(category);
    }

    private Vehicle createVehicle(VehicleRepository repo, String licensePlate, String color,
                                  User owner, VehicleCategory category) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setColor(color);
        vehicle.setOwner(owner);
        vehicle.setCategory(category);
        return repo.save(vehicle);
    }

    private void createTicket(TicketRepository repo, Vehicle vehicle, Site site,
                              LocalDateTime entryTime, LocalDateTime exitTime,
                              BigDecimal totalAmount, boolean paid) {
        Ticket ticket = new Ticket();
        ticket.setVehicle(vehicle);
        ticket.setSite(site);
        ticket.setEntryTime(entryTime);
        ticket.setExitTime(exitTime);
        ticket.setTotalAmount(totalAmount);
        ticket.setPaid(paid);
        repo.save(ticket);
    }
}
