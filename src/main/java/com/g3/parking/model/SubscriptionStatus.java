package com.g3.parking.model;

public enum SubscriptionStatus {
    ACTIVE,      // Activa y pagada
    EXPIRED,     // Venció por fecha
    CANCELLED,   // Usuario la canceló
    SUSPENDED,   // Suspendida por falta de pago
    PENDING      // Pendiente de activación
}
