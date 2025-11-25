/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Politica;

import EstructuraDeDatos.Cola;
import EstructuraDeDatos.ListaEnlazada;
import EstructuraDeDatos.Nodo;
import Proceso.SolicitudIO;
import Simulador.PlanificadorDisco;

/**
 *
 * @author Diego
 */
public class PoliticaSCAN implements PoliticaPlanificacion {
    // Necesitamos una referencia al planificador para cambiar su dirección
    private PlanificadorDisco planificador; 
    public PoliticaSCAN(PlanificadorDisco p) {
        this.planificador = p; 
    }

    
    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual) {
        // ... POLITICA SCAN ...
        // 1. Separar las solicitudes en dos listas: las que están en la dirección del movimiento y las que están detrás.
        // 2. Si hay solicitudes en la dirección actual, elegir la más cercana de esa lista.
        // 3. Si no hay, cambiar la dirección en el planificador (planificador.setDireccionActual(...)), 
        //    y luego elegir la más cercana en la nueva dirección.
        // 4. Volver a encolar el resto.
        
        if (colaIO.estaVacia()) return null;
        
        ListaEnlazada<SolicitudIO> mayor = new ListaEnlazada<SolicitudIO>();
        ListaEnlazada<SolicitudIO> menor = new ListaEnlazada<SolicitudIO>();
        
        while (!colaIO.estaVacia()) {
            SolicitudIO actual = colaIO.desencolar();
            if (actual.getBloqueObjetivo() < cabezalActual) {
                menor.agregarAlFinal(actual);
            } else {
                mayor.agregarAlFinal(actual);
            }
        }
        
        if (planificador.getDireccion() == DireccionScan.ASCENDENTE && mayor.estaVacia()) {
            planificador.setDireccion(DireccionScan.DESCENDENTE);
        }
        if (planificador.getDireccion() == DireccionScan.DESCENDENTE && menor.estaVacia()) {
            planificador.setDireccion(DireccionScan.ASCENDENTE);
        }
        
        SolicitudIO seleccionado = null;
        
        if (!mayor.estaVacia()) {
            int n = mayor.getTamano();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n-i-1; j++) {
                    if (mayor.obtener(j).getBloqueObjetivo() > mayor.obtener(j+1).getBloqueObjetivo()) {
                        SolicitudIO temp = mayor.obtener(j);
                        mayor.agregar(j, new Nodo(mayor.obtener(j+1)));
                        mayor.agregar(j+1, new Nodo(temp));
                    }
                }
            }
        }
        
        if (!menor.estaVacia()) {
            int n = menor.getTamano();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n-i-1; j++) {
                    if (menor.obtener(j).getBloqueObjetivo() < menor.obtener(j+1).getBloqueObjetivo()) {
                        SolicitudIO temp = menor.obtener(j);
                        menor.agregar(j, new Nodo(menor.obtener(j+1)));
                        menor.agregar(j+1, new Nodo(temp));
                    }
                }
            }
        }
        
        if (direccionActual == DireccionScan.ASCENDENTE ) {
            // Se utiliza la lista de procesos en bloques mayores al del cabezal
            if (mayor.estaVacia()) return null;
            seleccionado = mayor.eliminarDelFrente();
            
        } else {
            // Se utiliza la lista de procesos en bloques menores al cabezal
            if (menor.estaVacia()) return null;
            seleccionado = menor.eliminarDelFrente();
        }
        
        if (planificador.getDireccion() == DireccionScan.ASCENDENTE) {
            while (!mayor.estaVacia()) {
            colaIO.encolar(mayor.eliminarDelFrente());
            while (!menor.estaVacia()) {
            colaIO.encolar(menor.eliminarDelFrente());
        }
        }
        } else {
            while (!menor.estaVacia()) {
            colaIO.encolar(menor.eliminarDelFrente());
        }
            while (!mayor.estaVacia()) {
            colaIO.encolar(mayor.eliminarDelFrente());
        }
        }
        
        
      
        return seleccionado; 
    }
}
