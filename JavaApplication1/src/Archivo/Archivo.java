/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

import org.json.JSONObject; // para pasar el archivo a formato Json


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
    /**
     * Convierte este archivo a un objeto JSON.
     * @return Un JSONObject que representa este archivo.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("nombre", this.getNombre());
        json.put("tipo", "ARCHIVO");
        json.put("tamanoEnBloques", this.getTamanoEnBloques());
        json.put("primerBloque", this.getPrimerBloque());
        json.put("idProcesoCreador", this.getIdProcesoCreador());
        json.put("color", this.getColor());
        
        return json;
    }
    
    // Getters y Setters
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public int getPrimerBloque() { return primerBloque; }
    public int getIdProcesoCreador() { return idProcesoCreador; }
    public String getColor() { return color; }
}
