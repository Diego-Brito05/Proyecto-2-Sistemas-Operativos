/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Politica;

import EstructuraDeDatos.Cola;
import Proceso.SolicitudIO;

/**
 *
 * @author Diego
 */
public class PoliticaFIFO implements PoliticaPlanificacion {
    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual) {
        return colaIO.desencolar();
    }
}
