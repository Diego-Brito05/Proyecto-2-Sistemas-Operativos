/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulador;

import Archivo.Bloque;
import Archivo.Directorio;
import EstructuraDeDatos.Cola;
import Politica.PoliticaCSCAN;
import Politica.PoliticaFIFO;
import Politica.PoliticaSCAN;
import Politica.PoliticaSSTF;
import Proceso.EstadoProceso;
import Proceso.Proceso;
import Proceso.SolicitudIO;
import Proceso.TipoOperacionIO;

/**
 *
 * @author Diego
 */
public class SistemaManager {

    private Directorio directorioRaiz;
    private Bloque[] disco;
    private static final int TAMANO_DISCO = PlanificadorDisco.MAX_BLOQUES;
    
    // Colas de estados de proceso
    private Cola<Proceso> colaNuevos;
    private Cola<Proceso> colaListos;
    private Proceso procesoEnEjecucionIO; // Proceso cuya E/S se está ejecutando
    private Cola<Proceso> colaBloqueados;
    private Cola<Proceso> colaTerminados;
    
    // Cola de E/S y Planificador
    private Cola<SolicitudIO> colaIO;
    private PlanificadorDisco planificador;

    public SistemaManager() {
        inicializarDisco();
        directorioRaiz = new Directorio("C:", null);
        
        this.colaNuevos = new Cola<>();
        this.colaListos = new Cola<>();
        this.procesoEnEjecucionIO = null;
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        
        this.colaIO = new Cola<>();
        this.planificador = new PlanificadorDisco();
    }
    
    private void inicializarDisco() { /* ... */ }

    // --- MÉTODOS DE SOLICITUD (LLAMADOS DESDE LA UI) ---
    public void solicitarCreacionArchivo(Directorio padre, String nombre, int tamano) {
        int proximoId = Proceso.peekNextId(); // Asumiendo que Proceso tiene este método estático
        SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.CREAR, padre.getNombre() + "/" + nombre, tamano, encontrarPrimerBloqueLibre());
        Proceso p = new Proceso("Crear " + nombre, solicitud);
        p.setEstado(EstadoProceso.NUEVO);
        colaNuevos.encolar(p);
    }
    
    // --- MÉTODOS DEL CICLO DEL SIMULADOR ---
    
    public void admitirNuevosProcesos() {
        if (!colaNuevos.estaVacia()) {
            Proceso p = colaNuevos.desencolar();
            colaIO.encolar(p.getSolicitudAsociada());
            p.setEstado(EstadoProceso.BLOQUEADO);
            colaBloqueados.encolar(p);
        }
    }

    public void procesarSiguienteSolicitudIO() {
        if (procesoEnEjecucionIO != null || colaIO.estaVacia()) {
            return; 
        }

        SolicitudIO solicitud = planificador.seleccionarSiguiente(colaIO);
        if (solicitud == null) return;
        
        Proceso proceso = buscarYRemoverDeCola(colaBloqueados, solicitud.getIdProceso());
        if (proceso != null) {
            proceso.setEstado(EstadoProceso.EJECUTANDO);
            procesoEnEjecucionIO = proceso;
            
            _ejecutarOperacionIO(solicitud);
            
            proceso.setEstado(EstadoProceso.TERMINADO);
            colaTerminados.encolar(proceso);
            procesoEnEjecucionIO = null;
        }
    }
    
    private void _ejecutarOperacionIO(SolicitudIO solicitud) {
        System.out.println("Ejecutando E/S para Proceso " + solicitud.getIdProceso() + 
                           " en bloque " + solicitud.getBloqueObjetivo() + 
                           " (Cabezal en " + planificador.getCabezalActual() + ")");
        // Aquí iría la lógica real de manipulación del disco...
    }
    
    // --- GESTIÓN DE POLÍTICAS ---
    public void cambiarPoliticaPlanificacion(String nombrePolitica) {
        switch (nombrePolitica.toUpperCase()) {
            case "FIFO": planificador.setPolitica(new PoliticaFIFO()); break;
            case "SSTF": planificador.setPolitica(new PoliticaSSTF()); break;
            //case "SCAN": planificador.setPolitica(new PoliticaSCAN()); break;
            //case "C-SCAN": planificador.setPolitica(new PoliticaCSCAN()); break;
        }
    }

    // --- GETTERS PARA LA UI ---
    public Directorio getDirectorioRaiz() { return directorioRaiz; }
    public Cola<Proceso> getColaNuevos() { return colaNuevos; }
    public Proceso getProcesoEnEjecucionIO() { return procesoEnEjecucionIO; }
    public Cola<Proceso> getColaBloqueados() { return colaBloqueados; }
    public Cola<Proceso> getColaTerminados() { return colaTerminados; }
    public Cola<SolicitudIO> getColaIO() { return colaIO; }
    public PlanificadorDisco getPlanificador() { return planificador; }

    // --- Métodos de utilidad ---
    private int encontrarPrimerBloqueLibre() { return 0; /* Implementar */ }
    private Proceso buscarYRemoverDeCola(Cola<Proceso> cola, int idProceso) { /* Implementar */ return null; }
}
