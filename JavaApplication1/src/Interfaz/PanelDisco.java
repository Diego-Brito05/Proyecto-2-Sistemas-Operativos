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
    private int columnas;

    public PanelDisco(int totalBloques, int columnas) {
        this.bloquesOcupados = new boolean[totalBloques];
        this.columnas = columnas;
        setPreferredSize(new Dimension(400, 120));;
        //setBackground(Color.WHITE); // Ayuda para visualizar.
        //setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }
    
    // Para actualizar el estado de los bloques
    public void setBloqueOcupado(int indice, boolean ocupado) {
        bloquesOcupados[indice] = ocupado;
        repaint();
    }
    
    // Para reiniciar los bloques (todos libres)
    public void resetBloques() {
        for (int i = 0; i < bloquesOcupados.length; i++) {
            bloquesOcupados[i] = false;
        }
        repaint();
    }

    // Aquí pintas el grid
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int size = 42;
        for (int i = 0; i < bloquesOcupados.length; i++) {
            int x = (i % columnas) * size;
            int y = (i / columnas) * size;
            g.setColor(bloquesOcupados[i] ? Color.GREEN : Color.LIGHT_GRAY);
            g.fillRect(x, y, size-2, size-2);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size-2, size-2);
        }
    }
}
