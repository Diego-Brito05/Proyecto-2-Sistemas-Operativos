/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interfaz;

import Archivo.Archivo;
import Archivo.Directorio;
import Archivo.EntradaSistemaArchivos;
import EstructuraDeDatos.Cola;
import Proceso.Proceso;
import Simulador.SistemaManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Diego
 */
public class VentanaPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JMenuItem crearDirectorioItem;
    private javax.swing.JMenuItem crearArchivoItem;
    private javax.swing.JMenuItem renombrarItem;
    private javax.swing.JMenuItem eliminarItem;
    private SistemaManager sistemaManager;
    private Timer motorSimulador;
    private javax.swing.DefaultListModel<Proceso> modeloNuevos;
    private javax.swing.DefaultListModel<Proceso> modeloListos;
    private javax.swing.DefaultListModel<Proceso> modeloEjecutando;
    private javax.swing.DefaultListModel<Proceso> modeloBloqueados;
    private javax.swing.DefaultListModel<Proceso> modeloTerminados;
    private ProcesoListManager listManager;
    
    private void inicializarMenuContextual() {
    
    ///  Seccion del MenuPopup, incluyendo funciones para cada caso
        
        
    // Menu Popup para realizar los cambios al directorio desde el mismo Jtree, con cliick derecho
    popupMenu = new JPopupMenu();
    
    // --- Opción: Crear Directorio ---
    crearDirectorioItem = new JMenuItem("Crear Directorio");
    crearDirectorioItem.addActionListener(e -> {
            Directorio dirPadre = null;

            // 1. Obtener el nodo seleccionado del JTree.
            DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

            if (nodoSeleccionado == null) {
                // CASO 1: No hay nada seleccionado. Usamos la raíz como padre por defecto.
                dirPadre = sistemaManager.getDirectorioRaiz();
            } else {
                Object objetoUsuario = nodoSeleccionado.getUserObject();
                if (objetoUsuario instanceof Directorio) {
                    // CASO 2: Se seleccionó un directorio. Lo usamos como padre.
                    dirPadre = (Directorio) objetoUsuario;
                } else if (objetoUsuario instanceof Archivo) {
                    // CASO 3 (Mejora): Se seleccionó un archivo. Usamos el directorio que lo contiene.
                    dirPadre = ((Archivo) objetoUsuario).getPadre();
                }
            }

            // 2. Comprobación final. Si por alguna razón no pudimos determinar un padre, no continuamos.
            if (dirPadre == null) {
                JOptionPane.showMessageDialog(this, "No se pudo determinar un directorio padre válido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Pedir el nombre del nuevo directorio al usuario.
            String nombre = JOptionPane.showInputDialog(this, "Crear directorio dentro de '" + dirPadre.getNombre() + "':", "Crear Directorio", JOptionPane.PLAIN_MESSAGE);

            if (nombre != null && !nombre.trim().isEmpty()) {
                sistemaManager.solicitarCreacionDirectorio(dirPadre, nombre.trim());
        }
    });


    // --- Opción: Crear Archivo ---
    crearArchivoItem = new JMenuItem("Crear Archivo");
    crearArchivoItem.addActionListener(e -> {
        Directorio dirPadre = null;

        // 1. La misma lógica inteligente para determinar el directorio padre.
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

        if (nodoSeleccionado == null) {
            dirPadre = sistemaManager.getDirectorioRaiz();
        } else {
            Object objetoUsuario = nodoSeleccionado.getUserObject();
            if (objetoUsuario instanceof Directorio) {
                dirPadre = (Directorio) objetoUsuario;
            } else if (objetoUsuario instanceof Archivo) {
                dirPadre = ((Archivo) objetoUsuario).getPadre();
            }
        }

        if (dirPadre == null) {
            JOptionPane.showMessageDialog(this, "No se pudo determinar un directorio padre válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Pedir datos al usuario (nombre y tamaño).
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo archivo en '" + dirPadre.getNombre() + "':");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques para '" + nombre + "':");
        if (tamanoStr == null) return;

        try {
            int tamano = Integer.parseInt(tamanoStr);
            if (tamano <= 0) {
                JOptionPane.showMessageDialog(this, "El tamaño debe ser un número positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Llamar al manager para que inicie el proceso.
            sistemaManager.solicitarCreacionArchivo(dirPadre, nombre.trim(), tamano);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para el tamaño.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
    DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
    if (nodo == null || nodo.isRoot()) return;
    
    EntradaSistemaArchivos entrada = (EntradaSistemaArchivos) nodo.getUserObject();
    
    // Simplemente solicita la eliminación. No la ejecuta.
    sistemaManager.solicitarEliminacion(entrada);
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
    //  Siempre primero: NetBeans crea los componentes visuales (JTree, JList, etc.).
    initComponents();
    
    //  Crear la instancia del gestor del sistema.
    this.sistemaManager = new SistemaManager();
    
    //  ¡CRUCIAL! Crear el gestor de listas. Este constructor CREA los modelos
    //    y los ASIGNA a las JList que ya fueron creadas por initComponents().
    //    Asegúrate de que los nombres (ListaNuevo, etc.) coincidan con tu diseño.
    this.listManager = new ProcesoListManager(
            this.ListaNuevo, 
            this.ListaListo, 
            this.ListaEjecutando, 
            this.ListaBloqueado, 
            this.ListaTerminado
    );
    
    
    //  Crear una única instancia de nuestro renderer.
    ProcesoCellRenderer renderer = new ProcesoCellRenderer();
    
    //  Asignar el mismo renderer a las cinco listas.
    this.ListaNuevo.setCellRenderer(renderer);
    this.ListaListo.setCellRenderer(renderer);
    this.ListaEjecutando.setCellRenderer(renderer);
    this.ListaBloqueado.setCellRenderer(renderer);
    this.ListaTerminado.setCellRenderer(renderer);
    
    // Inicializar funcionalidades que dependen de los componentes y del manager.
    inicializarMenuContextual();
    inicializarMotorSimulador();
    
    //  Realizar la primera carga de datos en la UI.
    //    Ahora que el listManager existe, esta llamada funcionará correctamente.
    actualizarTodasLasVistas();
    
    //  Configurar los detalles finales de la ventana.
    this.setTitle("Simulador de Sistema de Archivos");
    this.setLocationRelativeTo(null); // Centra la ventana
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    
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
     
    
    public void actualizarArbol() {
            // 1. Obtiene la raíz desde la fuente de verdad: el SistemaManager.
            Directorio raiz = this.sistemaManager.getDirectorioRaiz();

            if (raiz == null) {
                jTree1.setModel(null);
                return;
            }

            // El resto del código es idéntico
        DefaultMutableTreeNode rootNode = crearNodosDelArbol(raiz);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        jTree1.setModel(treeModel);
    }
     
     /**
     * Configura y arranca el Timer que actúa como el motor principal de la simulación.
     */
    private void inicializarMotorSimulador() {
        int velocidadSimulacionMS = 1500; // 1.5 segundos por "tick"

        // El ActionListener es el código que se ejecutará en cada "tick" del timer.
        ActionListener tickListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // --- CICLO COMPLETO DEL SIMULADOR ---

            // 1. Fase de Admisión: Mueve de NUEVO a LISTO.
            sistemaManager.admitirNuevosProcesos();

            // 2. Fase de Preparación de E/S: Mueve de LISTO a BLOQUEADO.
            sistemaManager.prepararIO();

            // 3. Fase de Ejecución de E/S: Mueve de BLOQUEADO a TERMINADO (pasando por EJECUTANDO).
            sistemaManager.procesarSiguienteSolicitudIO();

            // 4. Actualiza toda la interfaz gráfica para reflejar los cambios de este "tick".
            actualizarTodasLasVistas();
        }
    };

        // Creamos el timer con la velocidad y el listener
        this.motorSimulador = new Timer(velocidadSimulacionMS, tickListener);
        
        // Lo iniciamos
        this.motorSimulador.start();
    }
    
    
    private void actualizarListasDeProcesos() {
    // Actualizar la lista de NUEVOS
    actualizarUnaLista(modeloNuevos, sistemaManager.getColaNuevos());
    
    // Actualizar la lista de LISTOS
    actualizarUnaLista(modeloListos, sistemaManager.getColaListos());
    
    // Actualizar la lista de BLOQUEADOS
    actualizarUnaLista(modeloBloqueados, sistemaManager.getColaBloqueados());
    
    // Actualizar la lista de TERMINADOS
    actualizarUnaLista(modeloTerminados, sistemaManager.getColaTerminados());

    // Actualizar el proceso en EJECUCIÓN (que no es una cola)
    modeloEjecutando.clear();
    Proceso enEjecucion = sistemaManager.getProcesoEnEjecucionIO();
    if (enEjecucion != null) {
        modeloEjecutando.addElement(enEjecucion);
    }
}

/**
 * Método de utilidad para actualizar un DefaultListModel a partir de una de tus Colas.
 * @param modelo El modelo de la JList a actualizar.
 * @param cola La cola de procesos del SistemaManager.
 */
    private void actualizarUnaLista(DefaultListModel<Proceso> modelo, Cola<Proceso> cola) {
    modelo.clear();
    
    // Truco para iterar tu cola sin destruirla:
    Cola<Proceso> aux = new Cola<>();
    while (!cola.estaVacia()) {
        Proceso p = cola.desencolar();
        modelo.addElement(p); // Añadir al modelo para la JList
        aux.encolar(p);      // Guardar en la cola auxiliar
    }
    // Devolver los elementos a la cola original
    while(!aux.estaVacia()) {
        cola.encolar(aux.desencolar());
    }
}
    
    public void actualizarTodasLasVistas() {
    actualizarArbol();

    // Esta es la llamada correcta que debería funcionar
     if (listManager != null) {
         listManager.actualizarListas(sistemaManager);
    }

    // Para depurar, vamos a añadir logs
    System.out.println("Actualizando vistas. Procesos en cola NUEVO: " + sistemaManager.getColaNuevos().getTamano());
 }
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Configuracion = new javax.swing.JTabbedPane();
        Arbol = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        VisualizadorDisco = new javax.swing.JPanel();
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

        javax.swing.GroupLayout ArbolLayout = new javax.swing.GroupLayout(Arbol);
        Arbol.setLayout(ArbolLayout);
        ArbolLayout.setHorizontalGroup(
            ArbolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1593, Short.MAX_VALUE)
        );
        ArbolLayout.setVerticalGroup(
            ArbolLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
        );

        Configuracion.addTab("Arbol", Arbol);

        javax.swing.GroupLayout VisualizadorDiscoLayout = new javax.swing.GroupLayout(VisualizadorDisco);
        VisualizadorDisco.setLayout(VisualizadorDiscoLayout);
        VisualizadorDiscoLayout.setHorizontalGroup(
            VisualizadorDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1593, Short.MAX_VALUE)
        );
        VisualizadorDiscoLayout.setVerticalGroup(
            VisualizadorDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 786, Short.MAX_VALUE)
        );

        Configuracion.addTab("Visualizador Disco", VisualizadorDisco);

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

        Configuracion.addTab("Configuracion", jPanel3);

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

        Configuracion.addTab("Procesos", Simulador);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Configuracion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1593, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(Configuracion, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JPanel Arbol;
    private javax.swing.JTabbedPane Configuracion;
    private javax.swing.JToggleButton IndicadorActivo;
    private javax.swing.JToggleButton IniciarPausarButton;
    private javax.swing.JList<Proceso> ListaBloqueado;
    private javax.swing.JList<Proceso> ListaEjecutando;
    private javax.swing.JList<Proceso> ListaListo;
    private javax.swing.JList<Proceso> ListaNuevo;
    private javax.swing.JList<Proceso> ListaTerminado;
    private javax.swing.JPanel Simulador;
    private javax.swing.JPanel VisualizadorDisco;
    private javax.swing.JTextArea enEjec;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JTree jTree1;
    private javax.swing.JTextArea modoAct;
    // End of variables declaration//GEN-END:variables
}
