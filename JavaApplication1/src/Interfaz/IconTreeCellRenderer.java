/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaz;

import Archivo.Archivo;
import Archivo.Directorio;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Un renderer personalizado para el JTree que asigna iconos diferentes a
 * directorios y archivos.
 */
public class IconTreeCellRenderer extends DefaultTreeCellRenderer {

    private final ImageIcon folderIcon;
    private final ImageIcon fileIcon;

    public IconTreeCellRenderer() {
        // Cargar los iconos desde el paquete de recursos.
        // La ruta debe ser relativa a la raíz del classpath (Source Packages).
        // Asegúrate de que la ruta sea correcta.
        folderIcon = new ImageIcon(getClass().getResource("/Imagen/CarpetaIMG.png"));
        fileIcon = new ImageIcon(getClass().getResource("/Imagen/ArchivoIMG.png"));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        
        // 1. Dejar que el renderer por defecto haga su trabajo inicial (texto, selección, etc.)
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // 2. Extraer el objeto de usuario del nodo del árbol
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            // 3. Asignar el icono correspondiente según el tipo de objeto
            if (userObject instanceof Directorio) {
                setIcon(folderIcon);
            } else if (userObject instanceof Archivo) {
                setIcon(fileIcon);
            } else {
                // Para cualquier otro caso (como la raíz si no es un Directorio)
                // podemos dejar el icono por defecto o poner uno genérico.
                // setIcon(null); // O el icono por defecto.
            }
        }

        return this;
    }
}
