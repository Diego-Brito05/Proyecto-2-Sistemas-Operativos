/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Politica;

import Simulador.PlanificadorDisco;
import EstructuraDeDatos.Cola;
import EstructuraDeDatos.ListaEnlazada;
import EstructuraDeDatos.Nodo;
import Proceso.SolicitudIO;

/**
 *
 * @author Diego
 */
public class PoliticaCSCAN implements PoliticaPlanificacion {
    // Necesitamos una referencia al planificador para cambiar su dirección
    private PlanificadorDisco planificador; 
    public PoliticaCSCAN(PlanificadorDisco p) {
        this.planificador = p; 
    }
    
    
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual) {
        // ... POLITICA C-SCAN ...
        // l igual que en SCAN se separa la cola en dos. Una de Solicitudes en bloques mayores y otras en bloques menores
        // La diferencia es que siempre se utiliza la lista de bloques mayores al del cabezal
        // Si la lista de mayores es null entonces el cabezal vuelve a 0 y la lista de menores ahora es la de mayores.
        
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
        
        if (planificador.getDireccion() == DireccionScan.DESCENDENTE) {
            planificador.setDireccion(DireccionScan.ASCENDENTE);
        }
        
        if (mayor.estaVacia()) {
            planificador.setCabezal(0);
            mayor = menor;
            menor = null;
        }
        
        SolicitudIO seleccionado = null;
        
        if (mayor.estaVacia()) return null;
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
            
            seleccionado = mayor.eliminarDelFrente();
        
        
        while (!mayor.estaVacia()) {
            colaIO.encolar(mayor.eliminarDelFrente());
        }
      
        return seleccionado; 
    }
}
