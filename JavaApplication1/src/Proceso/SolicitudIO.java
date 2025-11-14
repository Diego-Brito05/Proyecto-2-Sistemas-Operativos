/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proceso;

/**
 *
 * @author Diego
 */
public class SolicitudIO {
    private int idProceso;
    private TipoOperacionIO tipo;
    private String ruta;
    private int tamanoEnBloques; // Solo para CREAR
    private int bloqueObjetivo; // Posición del primer bloque para SSTF, SCAN, etc.
    private final String nuevoNombre; // Usado solo para operaciones de ACTUALIZAR

     public SolicitudIO(int idProceso, TipoOperacionIO tipo, String ruta, int tamanoEnBloques, int bloqueObjetivo, String nuevoNombre) {
        this.idProceso = idProceso;
        this.tipo = tipo;
        this.ruta = ruta;
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloqueObjetivo = bloqueObjetivo;
        this.nuevoNombre = nuevoNombre; // Asignar el nuevo campo
    }
     
       // --- Versión del constructor para operaciones que no necesitan un nuevo nombre ---
    public SolicitudIO(int idProceso, TipoOperacionIO tipo, String ruta, int tamanoEnBloques, int bloqueObjetivo) {
        this(idProceso, tipo, ruta, tamanoEnBloques, bloqueObjetivo, null);
    }

    // Getters
    public int getIdProceso() { return idProceso; }
    public TipoOperacionIO getTipo() { return tipo; }
    public String getRuta() { return ruta; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getBloqueObjetivo() { return bloqueObjetivo; }
    public String getNuevoNombre() {return nuevoNombre;}
}
