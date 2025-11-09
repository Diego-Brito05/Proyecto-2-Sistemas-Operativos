/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 *
 * @author Diego
 * Implementación de una lista enlazada
 * 
 * @param <T> El tipo de dato a almacenar en la lista.
 */

public class ListaEnlazada<T> {
    private Nodo<T> cabeza;
    private int tamano;

    public ListaEnlazada() {
        this.cabeza = null;
        this.tamano = 0;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void agregarAlFinal(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        if (estaVacia()) {
            cabeza = nuevoNodo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevoNodo);
        }
        tamano++;
    }

    public T eliminarDelFrente() {
        if (estaVacia()) {
            return null;
        }
        Nodo<T> nodoEliminado = cabeza;
        cabeza = cabeza.getSiguiente();
        tamano--;
        return nodoEliminado.getDato();
    }
    
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            return null; // O lanzar una excepción
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }
    
    public boolean eliminar(T dato) {
     if (estaVacia()) return false;

     // Caso 1: El elemento a eliminar es la cabeza de la lista
     if (cabeza.getDato().equals(dato)) {
         cabeza = cabeza.getSiguiente();
         tamano--;
         return true;
    }

     // Caso 2: El elemento está en otra parte de la lista
     Nodo<T> actual = cabeza;
     while (actual.getSiguiente() != null && !actual.getSiguiente().getDato().equals(dato)) {
         actual = actual.getSiguiente();
    }

     // Si encontramos el nodo (actual.getSiguiente() es el que queremos borrar)
     if (actual.getSiguiente() != null) {
         actual.setSiguiente(actual.getSiguiente().getSiguiente());
         tamano--;
         return true;
    }

     return false; // No se encontró el dato en la lista
    }
}
