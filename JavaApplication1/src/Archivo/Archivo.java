/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 *
 * @author Diego
 */
public class Archivo extends EntradaSistemaArchivos {
    private int tamanoEnBloques;
    private int primerBloque;
    private int idProcesoCreador;
    private String color; // Para la UI

    public Archivo(String nombre, Directorio padre, int tamanoEnBloques, int primerBloque, int idProcesoCreador, String color) {
        super(nombre, padre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
        this.idProcesoCreador = idProcesoCreador;
        this.color = color;
    }

    // Getters y Setters
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloque() { return primerBloque; }
    public int getIdProcesoCreador() { return idProcesoCreador; }
    public String getColor() { return color; }
}
