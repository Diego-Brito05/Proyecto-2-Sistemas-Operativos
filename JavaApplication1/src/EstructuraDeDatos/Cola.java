/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 *
 * @author Diego
 */
public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;
    private final Object bloqueoCola = new Object(); // Objeto para sincronizar accesos a esta cola

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }

    public void encolar(T elemento) {
        synchronized (bloqueoCola) { // Protege el acceso a la cola
            Nodo<T> nuevoNodo = new Nodo<>(elemento);
            if (estaVacia()) { // estaVacia() también debe ser sincronizado si se llama desde aquí
                frente = nuevoNodo;
                finalCola = nuevoNodo;
            } else {
                finalCola.setSiguiente(nuevoNodo);
                finalCola = nuevoNodo;
            }
            tamano++;
            // Podrías notificar a hilos que esperan por elementos si esta cola fuera de tipo productor/consumidor
            // bloqueoCola.notifyAll();
        }
    }

    public T desencolar() {
        synchronized (bloqueoCola) { // Protege el acceso a la cola
            if (estaVacia()) {
                return null;
            }
            T dato = frente.getDato();
            frente = frente.getSiguiente();
            if (frente == null) {
                finalCola = null;
            }
            tamano--;
            return dato;
        }
    }
    
    
     /**
     * Busca y elimina la primera ocurrencia de un elemento específico de la cola.
     * Esta es una funcionalidad extendida, no típica de una cola FIFO estricta,
     * pero necesaria para que el planificador de mediano plazo pueda suspender
     * un proceso bloqueado específico.
     *
     * @param dato El dato a eliminar.
     * @return true si el elemento fue encontrado y eliminado, false en caso contrario.
     */
    public boolean eliminar(T dato) {
        synchronized (bloqueoCola) {
            if (estaVacia()) {
                return false;
            }

            // Caso 1: El elemento a eliminar es el primero (el frente).
            // Podemos simplemente usar el método desencolar.
            if (frente.getDato().equals(dato)) {
                desencolar(); // Este método ya maneja la actualización de 'frente' y 'tamano'.
                return true;
            }

            // Caso 2: El elemento está en medio o al final de la cola.
            Nodo<T> previo = frente;
            Nodo<T> actual = frente.getSiguiente();

            while (actual != null) {
                if (actual.getDato().equals(dato)) {
                    // Encontramos el nodo. Enlazamos el nodo previo con el siguiente.
                    previo.setSiguiente(actual.getSiguiente());

                    // Si el nodo que eliminamos era el último (el final de la cola),
                    // debemos actualizar la referencia 'finalCola' para que apunte al nodo previo.
                    if (actual == finalCola) {
                        finalCola = previo;
                    }

                    tamano--; // Decrementamos el tamaño.
                    return true; // Éxito.
                }
                
                // Avanzamos en la búsqueda.
                previo = actual;
                actual = actual.getSiguiente();
            }

            return false; // El elemento no se encontró en la cola.
        }
    }


    public T verFrente() {
        synchronized (bloqueoCola) {
            if (estaVacia()) {
                return null;
            }
            return frente.getDato();
        }
    }

    public boolean estaVacia() {
        synchronized (bloqueoCola) { // Es importante sincronizar este método también
            return frente == null;
        }
    }

    public int getTamano() {
        synchronized (bloqueoCola) { // También sincronizar
            return tamano;
        }
    }

    // toArray también debe ser sincronizado para obtener una "snapshot" consistente
    public T[] toArray(T[] a) {
        synchronized (bloqueoCola) {
            // ... (implementación de toArray) ...
            if (tamano == 0) {
                return (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 0);
            }

            if (a.length < tamano) {
                a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), tamano);
            }

            int i = 0;
            Nodo<T> actual = frente;
            while (actual != null) {
                a[i++] = actual.getDato();
                actual = actual.getSiguiente();
            }

            if (a.length > tamano) {
                for (int j = tamano; j < a.length; j++) {
                    a[j] = null;
                }
            }
            return a;
        }
    }
}
