/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

/**
 *
 * @author Diego
 */
public abstract class EntradaSistemaArchivos {
    protected String nombre;
    protected Directorio padre;

    public EntradaSistemaArchivos(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Directorio getPadre() {
        return padre;
    }

    // Para que el JTree muestre el nombre
    @Override
    public String toString() {
        return nombre;
    }
}
