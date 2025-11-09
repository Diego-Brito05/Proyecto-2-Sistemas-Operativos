/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 *
 * @author Diego
 */
public class Bloque {
    public static final int FIN_DE_ARCHIVO = -1;

    private int id;
    private boolean ocupado;
    private int siguienteBloque; // Clave para la asignación encadenada
    private int idProceso; // Para saber qué proceso lo ocupa

    public Bloque(int id) {
        this.id = id;
        this.ocupado = false;
        this.siguienteBloque = FIN_DE_ARCHIVO;
        this.idProceso = -1; // -1 indica que no hay proceso asignado
    }
    
    public void ocupar(int idProceso) {
        this.ocupado = true;
        this.idProceso = idProceso;
    }

    public void liberar() {
        this.ocupado = false;
        this.siguienteBloque = FIN_DE_ARCHIVO;
        this.idProceso = -1;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public boolean isOcupado() { return ocupado; }
    public int getSiguienteBloque() { return siguienteBloque; }
    public void setSiguienteBloque(int siguienteBloque) { this.siguienteBloque = siguienteBloque; }
    public int getIdProceso() { return idProceso; }
}
