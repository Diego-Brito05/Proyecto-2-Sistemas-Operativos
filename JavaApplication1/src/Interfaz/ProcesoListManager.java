/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaz;

import EstructuraDeDatos.Cola;
import Proceso.Proceso;
import Simulador.SistemaManager;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 * @author Diego
 */
/**
 * Clase auxiliar que crea y gestiona los modelos de datos (DefaultListModel)
 * para las cinco JList que muestran el estado de los procesos.
 */
public class ProcesoListManager {

    // Los modelos que contendrán los datos de los procesos
    private final DefaultListModel<Proceso> modeloEjecutando;
    private final DefaultListModel<Proceso> modeloBloqueados;
    private final DefaultListModel<Proceso> modeloTerminados;

    /**
     * El constructor recibe las JList de la ventana principal para
     * crear y asignarles sus respectivos modelos.
     * @param listaEjecutando La JList para el proceso en Ejecución.
     * @param listaBloqueado La JList para la cola de Bloqueados.
     * @param listaTerminado La JList para la cola de Terminados.
     */
    public ProcesoListManager(JList<Proceso> listaEjecutando, JList<Proceso> listaBloqueado, JList<Proceso> listaTerminado) {
        // 1. Crear una instancia de cada modelo
        this.modeloEjecutando = new DefaultListModel<>();
        this.modeloBloqueados = new DefaultListModel<>();
        this.modeloTerminados = new DefaultListModel<>();
        
        // 2. Asignar cada modelo a su JList correspondiente
        listaEjecutando.setModel(this.modeloEjecutando);
        listaBloqueado.setModel(this.modeloBloqueados);
        listaTerminado.setModel(this.modeloTerminados);
    }

    /**
     * Método central que lee el estado actual de las colas en el SistemaManager
     * y actualiza los cinco modelos de lista para reflejar los cambios.
     * @param manager La instancia del SistemaManager con los datos actuales.
     */
    public void actualizarListas(SistemaManager manager) {
        // Actualizar cada una de las listas
        copiarColaAModelo(manager.getColaBloqueados(), modeloBloqueados);
        copiarColaAModelo(manager.getColaTerminados(), modeloTerminados);
        
        // El proceso en ejecución no es una cola, se trata de forma especial
        modeloEjecutando.clear();
        Proceso enEjecucion = manager.getProcesoEnEjecucionIO();
        if (enEjecucion != null) {
            modeloEjecutando.addElement(enEjecucion);
        }
    }

    /**
     * Método de utilidad que limpia un modelo y lo rellena con los datos de una
     * de tus colas personalizadas, sin destruir la cola original.
     * @param cola La cola de procesos de origen.
     * @param modelo El DefaultListModel de destino.
     */
    private void copiarColaAModelo(Cola<Proceso> cola, DefaultListModel<Proceso> modelo) {
        modelo.clear();
        
        Cola<Proceso> aux = new Cola<>();
        while (!cola.estaVacia()) {
            Proceso p = cola.desencolar();
            modelo.addElement(p); // Añadir al modelo para la JList
            aux.encolar(p);       // Guardar en la cola auxiliar
        }
        // Devolver los elementos a la cola original
        while (!aux.estaVacia()) {
            cola.encolar(aux.desencolar());
        }
    }
}
