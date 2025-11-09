/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proceso;

/**
 *
 * @author Diego
 */
public enum EstadoProceso {
    NUEVO,
    LISTO,
    EJECUTANDO,
    BLOQUEADO, // Esperando por E/S
    TERMINADO
}
