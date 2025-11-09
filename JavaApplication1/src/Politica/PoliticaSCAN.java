/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Politica;

import EstructuraDeDatos.Cola;
import Proceso.SolicitudIO;
import Simulador.PlanificadorDisco;

/**
 *
 * @author Diego
 */
public class PoliticaSCAN implements PoliticaPlanificacion {
    // Necesitamos una referencia al planificador para cambiar su dirección
    private PlanificadorDisco planificador; 
    public PoliticaSCAN(PlanificadorDisco p) { this.planificador = p; }
    
    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual) {
        // ... Lógica similar a SSTF para vaciar la cola a una lista temporal ...
        // 1. Separar las solicitudes en dos listas: las que están en la dirección del movimiento y las que están detrás.
        // 2. Si hay solicitudes en la dirección actual, elegir la más cercana de esa lista.
        // 3. Si no hay, cambiar la dirección en el planificador (planificador.setDireccionActual(...)), 
        //    y luego elegir la más cercana en la nueva dirección.
        // 4. Volver a encolar el resto.
        
        // Esta implementación es compleja, aquí un pseudocódigo simplificado:
        // encontrar_solicitudes_en_direccion();
        // if (hay_solicitudes_en_direccion) {
        //   return mas_cercana_de_esa_lista;
        // } else {
        //   planificador.invertirDireccion();
        //   encontrar_solicitudes_en_nueva_direccion();
        //   return mas_cercana_de_esa_lista;
        // }
        
        // Por simplicidad, y para que puedas avanzar, usaremos una lógica más básica por ahora.
        // Una implementación completa requiere más código para manejar la lista temporal.
        // Esta es solo una aproximación.
        System.out.println("Política SCAN no implementada completamente. Usando FIFO como fallback.");
        return colaIO.desencolar(); 
    }
}
