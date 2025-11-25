package com.g3.parking.datatransfer;

import com.g3.parking.model.Ticket;
import java.time.LocalDateTime;

public class ActiveTicketResponse {
    private Long ticketId;
    private LocalDateTime entryTime;
    private VehicleInfo vehicle;

    public static class VehicleInfo {
        private String licensePlate;
        private String color;
        private String categoryName;
        private String ownerUsername;

        public VehicleInfo(String licensePlate, String color, String categoryName, String ownerUsername) {
            this.licensePlate = licensePlate;
            this.color = color;
            this.categoryName = categoryName;
            this.ownerUsername = ownerUsername;
        }

        // Getters y Setters
        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getOwnerUsername() {
            return ownerUsername;
        }

        public void setOwnerUsername(String ownerUsername) {
            this.ownerUsername = ownerUsername;
        }
    }

    public static ActiveTicketResponse fromTicket(Ticket ticket) {
        ActiveTicketResponse response = new ActiveTicketResponse();
        response.ticketId = ticket.getId();
        response.entryTime = ticket.getEntryTime();
        
        if (ticket.getVehicle() != null) {
            String categoryName = ticket.getVehicle().getCategory() != null 
                ? ticket.getVehicle().getCategory().getName() 
                : "N/A";
            String ownerUsername = ticket.getVehicle().getOwner() != null 
                ? ticket.getVehicle().getOwner().getUsername() 
                : "N/A";
            
            response.vehicle = new VehicleInfo(
                ticket.getVehicle().getLicensePlate(),
                ticket.getVehicle().getColor(),
                categoryName,
                ownerUsername
            );
        }
        
        return response;
    }

    // Getters y Setters
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleInfo vehicle) {
        this.vehicle = vehicle;
    }
}
