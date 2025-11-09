/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Politica;

import EstructuraDeDatos.Cola;
import EstructuraDeDatos.ListaEnlazada;
import Proceso.SolicitudIO;

/**
 *
 * @author Diego
 */
public class PoliticaSSTF implements PoliticaPlanificacion {
    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaIO, int cabezalActual, DireccionScan direccionActual) {
        if (colaIO.estaVacia()) return null;

        // Vaciamos la cola a una lista temporal para poder iterarla
        ListaEnlazada<SolicitudIO> temp = new ListaEnlazada<>();
        while (!colaIO.estaVacia()) {
            temp.agregarAlFinal(colaIO.desencolar());
        }

        SolicitudIO mejorOpcion = null;
        int minimaDistancia = Integer.MAX_VALUE;

        // Buscamos la solicitud con la menor distancia al cabezal
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.obtener(i);
            int distancia = Math.abs(actual.getBloqueObjetivo() - cabezalActual);
            if (distancia < minimaDistancia) {
                minimaDistancia = distancia;
                mejorOpcion = actual;
            }
        }

        // Volvemos a encolar todas las solicitudes excepto la elegida
        for (int i = 0; i < temp.getTamano(); i++) {
            SolicitudIO actual = temp.obtener(i);
            if (actual != mejorOpcion) {
                colaIO.encolar(actual);
            }
        }

        return mejorOpcion;
    }
}
