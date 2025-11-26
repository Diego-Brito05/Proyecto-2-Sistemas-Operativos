/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaz;

import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Esteacosta
 */

public class PanelDisco extends JPanel {
    private boolean[] bloquesOcupados;
    
    private boolean[] esPrimerBloque;
    
    private int columnas;

    public PanelDisco(int totalBloques, int columnas) {
        this.bloquesOcupados = new boolean[totalBloques];
        this.esPrimerBloque = new boolean[totalBloques]; 
        this.columnas = columnas;
        setPreferredSize(new Dimension(400, 120));
    }
    
    // Se queda igual
    public void setBloqueOcupado(int indice, boolean ocupado) {
        if (indice >= 0 && indice < bloquesOcupados.length) {
            bloquesOcupados[indice] = ocupado;
        }
        // Quitamos repaint() de aquí para que sea más eficiente. La actualización se hará al final.
    }
    
    // --- NUEVO MÉTODO ---
    /**
     * Marca un bloque como el inicio de un archivo.
     * @param indice El índice del bloque.
     * @param esPrimero true si es el primer bloque, false en caso contrario.
     */
    public void setEsPrimerBloque(int indice, boolean esPrimero) {
        if (indice >= 0 && indice < esPrimerBloque.length) {
            esPrimerBloque[indice] = esPrimero;
        }
    }
    
    // Modificado para limpiar ambos arrays
    public void resetBloques() {
        for (int i = 0; i < bloquesOcupados.length; i++) {
            bloquesOcupados[i] = false;
            esPrimerBloque[i] = false; // <-- Limpiar también el nuevo array
        }
        // Quitamos repaint() de aquí. Se llamará desde VentanaPrincipal.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = 42; // Ajusta este tamaño según necesites
        for (int i = 0; i < bloquesOcupados.length; i++) {
            int x = (i % columnas) * size;
            int y = (i / columnas) * size;

            // --- LÓGICA DE COLORES MEJORADA ---
            if (esPrimerBloque[i]) {
                g.setColor(Color.YELLOW); // Si es el primer bloque, es amarillo.
            } else if (bloquesOcupados[i]) {
                g.setColor(Color.GREEN); // Si está ocupado pero no es el primero, es verde.
            } else {
                g.setColor(Color.LIGHT_GRAY); // Si está libre, es gris.
            }
            
            g.fillRect(x, y, size - 2, size - 2);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size - 2, size - 2);
        }
    }
}
