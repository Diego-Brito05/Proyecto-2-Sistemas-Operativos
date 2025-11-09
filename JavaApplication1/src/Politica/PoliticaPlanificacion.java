/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Politica;

import EstructuraDeDatos.Cola;
import Proceso.SolicitudIO;

/**
 *
 * @author Diego
 */
public interface PoliticaPlanificacion {
    
    /**
     * Selecciona la siguiente SolicitudIO de la cola según la lógica de la política.
     * IMPORTANTE: La política es responsable de remover la solicitud seleccionada de la cola.
     * 
     * @param colaIO La cola de solicitudes de E/S pendientes.
     * @param cabezalActual La posición actual del cabezal del disco (número de bloque).
     * @param direccionActual La dirección actual del movimiento del cabezal (para SCAN/C-SCAN).
     * @return La solicitud seleccionada para ser procesada, o null si la cola está vacía.
     */
    SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual);
}
