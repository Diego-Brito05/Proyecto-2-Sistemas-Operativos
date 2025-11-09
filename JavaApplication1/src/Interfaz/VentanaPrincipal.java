/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaz;

import Archivo.Archivo;
import Archivo.Directorio;
import Archivo.EntradaSistemaArchivos;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Diego
 */
public class VentanaPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());
    private Directorio directorioRaiz;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem crearDirectorioItem;
    private javax.swing.JMenuItem crearArchivoItem;
    private javax.swing.JMenuItem renombrarItem;
    private javax.swing.JMenuItem eliminarItem;
    
    
    
    private void inicializarMenuContextual() {
        
    popupMenu = new JPopupMenu();
    
    // --- Opción: Crear Directorio ---
    crearDirectorioItem = new JMenuItem("Crear Directorio");
    crearDirectorioItem.addActionListener(e -> {
        // Lógica para crear un directorio
        JOptionPane.showMessageDialog(this, "Acción: Crear Directorio");
    });
    
    // --- Opción: Crear Archivo ---
    crearArchivoItem = new JMenuItem("Crear Archivo");
    crearArchivoItem.addActionListener(e -> {
        // Lógica para crear un archivo
        JOptionPane.showMessageDialog(this, "Acción: Crear Archivo");
    });

    // --- Opción: Renombrar ---
    renombrarItem = new JMenuItem("Renombrar");
    renombrarItem.addActionListener(e -> {
        // Lógica para renombrar
        JOptionPane.showMessageDialog(this, "Acción: Renombrar");
    });

    // --- Opción: Eliminar ---
    eliminarItem = new JMenuItem("Eliminar");
    eliminarItem.addActionListener(e -> {
        // Lógica para eliminar
        JOptionPane.showMessageDialog(this, "Acción: Eliminar");
        // Aquí llamarías a un método que se encargue de la eliminación,
        // libere los bloques y finalmente llame a actualizarArbol(directorioRaiz);
    });

    // Añadir las opciones al menú emergente
    popupMenu.add(crearDirectorioItem);
    popupMenu.add(crearArchivoItem);
    popupMenu.add(renombrarItem);
    popupMenu.add(new JSeparator()); // Una línea separadora
    popupMenu.add(eliminarItem);
    }
    /**
     * Creates new form VentanaPrincipal
     */
    public VentanaPrincipal() {
    initComponents();
    inicializarMenuContextual();
    
    // --- AÑADE ESTE BLOQUE DE CÓDIGO ---
    jTree1.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                mostrarMenu(e);
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
             if (e.isPopupTrigger()) {
                mostrarMenu(e);
            }
        }
        
        private void mostrarMenu(MouseEvent e) {
            // Obtener la ruta del árbol para la ubicación del clic
            int x = e.getX();
            int y = e.getY();
            TreePath path = jTree1.getPathForLocation(x, y);

            if (path == null) {
                // El usuario hizo clic en un área vacía, no hacemos nada
                return;
            }
            
            // Seleccionar el nodo en el que se hizo clic derecho
            jTree1.setSelectionPath(path);
            
            // Obtener el objeto de usuario (nuestro Archivo o Directorio)
            DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object objetoUsuario = nodoSeleccionado.getUserObject();
            
            // --- LÓGICA CONTEXTUAL ---
            if (objetoUsuario instanceof Directorio) {
                // Si es un directorio, habilitamos todo
                crearDirectorioItem.setEnabled(true);
                crearArchivoItem.setEnabled(true);
                renombrarItem.setEnabled(true);
                eliminarItem.setEnabled(true);
            } else if (objetoUsuario instanceof Archivo) {
                // Si es un archivo, deshabilitamos las opciones de crear
                crearDirectorioItem.setEnabled(false);
                crearArchivoItem.setEnabled(false);
                renombrarItem.setEnabled(true);
                eliminarItem.setEnabled(true);
            }
            
            // Mostrar el menú en la posición del cursor
            popupMenu.show(jTree1, x, y);
        }
    });
    
    
    
    // Pega aquí la lógica que tenías en tu constructor original
    inicializarSistemaDeArchivos();
    actualizarArbol(directorioRaiz);

    // Y añade la configuración de la ventana
    this.setTitle("Simulador de Sistema de Archivos");
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
    
    
     private DefaultMutableTreeNode crearNodosDelArbol(Directorio directorio) {
        // 1. Crea un nodo para el directorio actual.
        //    Guardamos el objeto 'Directorio' completo. ¡Esto es muy útil!
        //    JTree llamará a su método toString() para saber qué texto mostrar.
        DefaultMutableTreeNode nodoDirectorio = new DefaultMutableTreeNode(directorio);

        // 2. Recorre el contenido del directorio
        for (int i = 0; i < directorio.getContenido().getTamano(); i++) {
            EntradaSistemaArchivos entrada = directorio.getContenido().obtener(i);

            // 3. Verifica si la entrada es un sub-directorio o un archivo
            if (entrada instanceof Directorio) {
                // Si es un directorio, llama a este mismo método de forma recursiva
                Directorio subDir = (Directorio) entrada;
                DefaultMutableTreeNode subNodo = crearNodosDelArbol(subDir);
                // Y añade el resultado como hijo del nodo actual
                nodoDirectorio.add(subNodo);
            } else if (entrada instanceof Archivo) {
                // Si es un archivo, es una "hoja" del árbol. Simplemente crea un nodo para él.
                Archivo archivo = (Archivo) entrada;
                DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(archivo);
                // Y lo añade como hijo del nodo actual
                nodoDirectorio.add(nodoArchivo);
            }
        }

        return nodoDirectorio;
    }
     
      private void inicializarSistemaDeArchivos() {
        // Creamos la estructura de datos en memoria
        this.directorioRaiz = new Directorio("C:", null); // La raíz no tiene padre
        
        Directorio users = new Directorio("Users", this.directorioRaiz);
        Directorio system = new Directorio("System", this.directorioRaiz);
        
        Directorio admin = new Directorio("Admin", users);
        Directorio docs = new Directorio("Documentos", admin);
        
        Archivo archivo1 = new Archivo("notas.txt", docs, 5, 10, 1, "blue");
        Archivo archivo2 = new Archivo("plan.docx", docs, 12, 15, 1, "green");
        Archivo boot = new Archivo("boot.ini", system, 1, 0, 0, "gray");

        // Enlazamos la estructura
        this.directorioRaiz.agregarEntrada(users);
        this.directorioRaiz.agregarEntrada(system);
        
        users.agregarEntrada(admin);
        
        admin.agregarEntrada(docs);
        
        docs.agregarEntrada(archivo1);
        docs.agregarEntrada(archivo2);
        
        system.agregarEntrada(boot);
    }
    

    // ----------- MÉTODOS PARA ACTUALIZAR EL JTREE -----------
    
    /**
     * Actualiza todo el JTree para que refleje el estado actual
     * del sistema de archivos simulado.
     * 
     * @param raiz El directorio raíz de tu sistema de archivos.
     */
    public void actualizarArbol(Directorio raiz) {
        if (raiz == null) {
            jTree1.setModel(null);
            return;
        }
        DefaultMutableTreeNode rootNode = crearNodosDelArbol(raiz);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        jTree1.setModel(treeModel);
    }
     
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        Simulador = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        ListaEjecutando = new javax.swing.JList<>();
        jLabel27 = new javax.swing.JLabel();
        IndicadorActivo = new javax.swing.JToggleButton();
        jScrollPane18 = new javax.swing.JScrollPane();
        ListaTerminado = new javax.swing.JList<>();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jScrollPane21 = new javax.swing.JScrollPane();
        ListaNuevo = new javax.swing.JList<>();
        jScrollPane22 = new javax.swing.JScrollPane();
        ListaListo = new javax.swing.JList<>();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane24 = new javax.swing.JScrollPane();
        ListaBloqueado = new javax.swing.JList<>();
        jLabel43 = new javax.swing.JLabel();
        IniciarPausarButton = new javax.swing.JToggleButton();
        modoAct = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        enEjec = new javax.swing.JTextArea();
        jLabel72 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setViewportView(jTree1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1593, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Arbol", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1593, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 786, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab2", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1593, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 786, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab3", jPanel3);

        Simulador.setBackground(new java.awt.Color(102, 204, 255));

        jScrollPane17.setViewportView(ListaEjecutando);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("Terminado");

        IndicadorActivo.setBackground(new java.awt.Color(51, 255, 0));
        IndicadorActivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IndicadorActivoActionPerformed(evt);
            }
        });

        jScrollPane18.setViewportView(ListaTerminado);

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Listo");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Ejecutando");

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("Nuevo");

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel33.setText("Modo:");

        jScrollPane21.setViewportView(ListaNuevo);

        jScrollPane22.setViewportView(ListaListo);

        jLabel34.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jScrollPane24.setViewportView(ListaBloqueado);

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("Bloqueado");

        IniciarPausarButton.setText("Iniciar");
        IniciarPausarButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                IniciarPausarButtonItemStateChanged(evt);
            }
        });
        IniciarPausarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IniciarPausarButtonActionPerformed(evt);
            }
        });

        modoAct.setColumns(20);
        modoAct.setRows(5);

        enEjec.setColumns(20);
        enEjec.setRows(5);
        jScrollPane2.setViewportView(enEjec);

        jLabel72.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel72.setText("Log de ejecución");

        javax.swing.GroupLayout SimuladorLayout = new javax.swing.GroupLayout(Simulador);
        Simulador.setLayout(SimuladorLayout);
        SimuladorLayout.setHorizontalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(IniciarPausarButton, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(SimuladorLayout.createSequentialGroup()
                                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(modoAct, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(SimuladorLayout.createSequentialGroup()
                                        .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(73, 73, 73)
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(63, 63, 63)
                                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(38, 38, 38)
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addComponent(IndicadorActivo)
                        .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addGap(236, 236, 236)
                                .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGap(548, 548, 548)
                        .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 906, Short.MAX_VALUE))))
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGap(645, 645, 645)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SimuladorLayout.setVerticalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addComponent(IndicadorActivo, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(88, 88, 88))
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addComponent(jLabel72)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32)
                    .addComponent(jLabel43)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                    .addComponent(jScrollPane22, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane24, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modoAct, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel33)
                        .addComponent(IniciarPausarButton)))
                .addGap(16, 16, 16))
        );

        jTabbedPane1.addTab("Procesos", Simulador);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1593, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void IndicadorActivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IndicadorActivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IndicadorActivoActionPerformed

    private void IniciarPausarButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_IniciarPausarButtonItemStateChanged

    }//GEN-LAST:event_IniciarPausarButtonItemStateChanged

    private void IniciarPausarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IniciarPausarButtonActionPerformed

    }//GEN-LAST:event_IniciarPausarButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton IndicadorActivo;
    private javax.swing.JToggleButton IniciarPausarButton;
    private javax.swing.JList<Proceso.Proceso> ListaBloqueado;
    private javax.swing.JList<Proceso.Proceso> ListaEjecutando;
    private javax.swing.JList<Proceso.Proceso> ListaListo;
    private javax.swing.JList<Proceso.Proceso> ListaNuevo;
    private javax.swing.JList<Proceso.Proceso> ListaTerminado;
    private javax.swing.JPanel Simulador;
    private javax.swing.JTextArea enEjec;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTextArea modoAct;
    // End of variables declaration//GEN-END:variables
}
