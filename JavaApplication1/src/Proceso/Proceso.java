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
    // Contador estático para generar IDs únicos para cada proceso.
    private static int contadorId = 0;

    private final int id;
    private String nombre;
    private EstadoProceso estado;
    private final SolicitudIO solicitudAsociada;

    public Proceso(String nombre, SolicitudIO solicitud) {
        // Asigna el ID actual y LUEGO incrementa el contador para el siguiente.
        this.id = contadorId++;
        this.nombre = nombre;
        this.solicitudAsociada = solicitud;
        this.estado = EstadoProceso.NUEVO; // Un proceso siempre nace en estado NUEVO.
    }

    /**
     * Método estático que permite "espiar" cuál será el próximo ID a ser asignado,
     * sin consumirlo. Es crucial para crear la SolicitudIO antes que el Proceso.
     * @return El valor del siguiente ID de proceso.
     */
    public static int peekNextId() {
        return contadorId;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public SolicitudIO getSolicitudAsociada() {
        return solicitudAsociada;
    }
    
    @Override
    public String toString() {
        // Útil para debugging o para mostrar en listas simples.
        return "P" + id + ": " + nombre + " (" + estado + ")";
    }
}
