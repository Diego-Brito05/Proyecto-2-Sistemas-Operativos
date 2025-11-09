/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;

import EstructuraDeDatos.ListaEnlazada;

/**
 *
 * @author Diego
 */
public class Directorio extends EntradaSistemaArchivos {
    private ListaEnlazada<EntradaSistemaArchivos> contenido;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.contenido = new ListaEnlazada<>();
    }

    public void agregarEntrada(EntradaSistemaArchivos entrada) {
        this.contenido.agregarAlFinal(entrada);
    }
    
    public boolean eliminarEntrada(String nombre) {
        for (int i = 0; i < contenido.getTamano(); i++) {
            if(contenido.obtener(i).getNombre().equals(nombre)) {
                // Para eliminar, necesitaríamos modificar la lista enlazada para
                // soportar eliminación por índice o valor.
                // Por ahora, asumimos que se puede hacer. (Ya se agregó en ListaEnlazada)
                return contenido.eliminar(contenido.obtener(i));
            }
        }
        return false;
    }

    public ListaEnlazada<EntradaSistemaArchivos> getContenido() {
        return contenido;
    }
}
