/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaz;

/**
 *
 * @author Diego
 */
import Proceso.Proceso;
import Proceso.TipoOperacionIO;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * Un renderer personalizado para mostrar objetos Proceso en una JList con formato.
 * - Centra el texto.
 * - Pone en negrita el nombre del archivo/directorio.
 * - Cambia el color del texto según el tipo de operación de E/S.
 */
public class ProcesoCellRenderer extends JLabel implements ListCellRenderer<Proceso> {

    public ProcesoCellRenderer() {
        setOpaque(true);
        // Para que el HTML multilínea se alinee correctamente, alineamos el JLabel verticalmente
        setVerticalAlignment(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Proceso> list,
                                                  Proceso proceso,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        
        // --- 1. Extraer información del Proceso ---
        int idProceso = proceso.getId();
        TipoOperacionIO tipoIO = proceso.getSolicitudAsociada().getTipo();
        String nombreProcesoCompleto = proceso.getNombre();
        
        // Extraemos el tipo de operación y el nombre del archivo/directorio
        String[] partes = nombreProcesoCompleto.split(" ", 2);
        String tipoOperacionStr = partes.length > 0 ? partes[0] : "";
        String nombreEntrada = partes.length > 1 ? partes[1] : "";

        // --- 2. Asignar color según el tipo de operación ---
        String colorHex;
        switch (tipoIO) {
            case CREAR:
            case CREAR_DIRECTORIO:
                colorHex = "#006400"; // Verde oscuro
                break;
            case ELIMINAR:
                colorHex = "#FF0000"; // Rojo
                break;
            case ACTUALIZAR:
                colorHex = "#000096"; // Azul oscuro
                break;
            case LEER:
                colorHex = "#000000"; // Negro
                break;
            default:
                colorHex = "#808080"; // Gris
                break;
        }

        // --- 3. Construir la cadena HTML ---
        String textoHtml = String.format(
            "<html><div style='text-align: center;'>" +
            "<b>Proceso %d</b><br>" +
            "<font color='%s'>%s</font><br>" +
            "%s" +
            "</div></html>",
            idProceso,
            colorHex,
            tipoOperacionStr,
            nombreEntrada
        );
        
        setText(textoHtml);

        // --- 4. Manejar la selección del usuario ---
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            // El color del texto ya está definido por el HTML, así que no lo sobrescribimos
            // a menos que queramos un efecto especial para la selección.
        } else {
            setBackground(list.getBackground());
        }

        return this;
    }
}