package com.g3.parking.datatransfer;

public class PlacaResponse {

    private boolean exito;
    private String placa;
    private String error;

    public static PlacaResponse error(String mensaje) {
        PlacaResponse r = new PlacaResponse();
        r.exito = false;
        r.error = mensaje;
        return r;
    }

    // Getters & Setters

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }


    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
