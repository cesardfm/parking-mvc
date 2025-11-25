package com.g3.parking.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceRequest {
    private String invoiceId;
    private String payerName;
    private String parkingName;
    private String description;
    private BigDecimal amount;
    private LocalDateTime date;
    
    // Información del ticket
    private String vehicleLicensePlate;
    private String vehicleCategory;
    private String vehicleColor;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal discount;
    private String parkingAddress;
    private String parkingOrganization;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getParkingAddress() {
        return parkingAddress;
    }

    public void setParkingAddress(String parkingAddress) {
        this.parkingAddress = parkingAddress;
    }

    public String getParkingOrganization() {
        return parkingOrganization;
    }

    public void setParkingOrganization(String parkingOrganization) {
        this.parkingOrganization = parkingOrganization;
    }
}
