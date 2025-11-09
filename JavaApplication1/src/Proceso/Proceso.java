/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proceso;

/**
 *
 * @author Diego
 */
public class Proceso {
    private static int contadorId = 0;
    private int id;
    private String nombre;
    private EstadoProceso estado;
    private SolicitudIO solicitudAsociada;

     public Proceso(String nombre, SolicitudIO solicitud) {
        this.id = contadorId++;
        this.nombre = nombre;
        this.estado = EstadoProceso.NUEVO;
        this.solicitudAsociada = solicitud;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    
    public SolicitudIO getSolicitudAsociada() {
        return solicitudAsociada;
        }
}
