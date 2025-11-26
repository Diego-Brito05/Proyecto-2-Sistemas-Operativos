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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
    private javax.swing.DefaultListModel<Proceso> modeloEjecutando;
    private javax.swing.DefaultListModel<Proceso> modeloBloqueados;
    private javax.swing.DefaultListModel<Proceso> modeloTerminados;
    private ProcesoListManager listManager;
    
    private ModoUsuario modoActual;
    private DefaultMutableTreeNode nodoClickeado; 
    private PanelDisco panelDisco;
    
    private void inicializarMenuContextual() {
    
    ///  Seccion del MenuPopup, incluyendo funciones para cada caso
        
        
    // Menu Popup para realizar los cambios al directorio desde el mismo Jtree, con click derecho
    popupMenu = new JPopupMenu();
    
    this.modoActual = ModoUsuario.ADMINISTRADOR;
        actualizarUIModo();
    this.eliminarItem = new JMenuItem("Eliminar");
    
   
    this.eliminarItem.addActionListener(e -> {
        
        if (this.nodoClickeado == null) {
            System.out.println("El nodo clickeado es null. Saliendo.");
            return;
        }

        if (this.nodoClickeado.isRoot()) {
            System.out.println("Se intentó eliminar la raíz.");
            JOptionPane.showMessageDialog(this, "No se puede eliminar el directorio raíz.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        EntradaSistemaArchivos entrada = (EntradaSistemaArchivos) this.nodoClickeado.getUserObject();
    
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "¿Estás seguro de que quieres eliminar '" + entrada.getNombre() + "'?", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("Solicitando eliminación para: " + entrada.getNombre());
            sistemaManager.solicitarEliminacion(entrada);
        } else {
            System.out.println("Eliminación cancelada por el usuario.");
        }
        });
    
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
        // Usar la variable 'nodoClickeado' que ya tenemos.
        if (this.nodoClickeado == null) {
            return; // No hay nada seleccionado
        }

        // Prevenir el renombrado de la raíz.
        if (this.nodoClickeado.isRoot()) {
            JOptionPane.showMessageDialog(this, "No se puede renombrar el directorio raíz.", "Acción no permitida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EntradaSistemaArchivos entrada = (EntradaSistemaArchivos) this.nodoClickeado.getUserObject();

        // Pedir el nuevo nombre al usuario, sugiriendo el nombre actual.
        String nuevoNombre = (String) JOptionPane.showInputDialog(
            this,
            "Ingrese el nuevo nombre para '" + entrada.getNombre() + "':",
            "Renombrar",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            entrada.getNombre() // Valor inicial en el campo de texto
        );

        //  Validar la entrada del usuario.
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && !nuevoNombre.equals(entrada.getNombre())) {
            //  Llamar al SistemaManager para que inicie el proceso.
            sistemaManager.solicitarRenombrar(entrada, nuevoNombre.trim());
        }
    });

    // --- Opción: Eliminar ---
    eliminarItem.addActionListener(e -> {
    
        System.out.println("Solicitando eliminación ");
        
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
    this.inicializarPanelDisco();
    this.panelDisco.setPreferredSize(new Dimension(500, 300));
    
    
    jTree1.setCellRenderer(new IconTreeCellRenderer());
    this.TablaArchivos.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    
    this.listManager = new ProcesoListManager(
             
            this.ListaEjecutando, 
            this.ListaBloqueado, 
            this.ListaTerminado
    );
    
    
    //  Crear una única instancia de nuestro renderer.
    ProcesoCellRenderer renderer = new ProcesoCellRenderer();
    
    //  Asignar el mismo renderer a las cinco listas.
   
    this.ListaEjecutando.setCellRenderer(renderer);
    this.ListaBloqueado.setCellRenderer(renderer);
    this.ListaTerminado.setCellRenderer(renderer);
    
    
    
    // Inicializar funcionalidades que dependen de los componentes y del manager.
    inicializarMenuContextual();
    inicializarMotorSimulador();
    
    //  Realizar la primera carga de datos en la UI.
    actualizarArbol();
    actualizarTodasLasVistas();
    
    //  Configurar los detalles finales de la ventana.
    this.setTitle("Simulador de Sistema de Archivos");
    this.setLocationRelativeTo(null); // Centra la ventana
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    
    jTree1.addMouseListener(new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
        // Manejar el evento cuando se presiona el botón, crucial para macOS/Linux
        if (e.isPopupTrigger()) {
            mostrarMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Manejar el evento cuando se suelta el botón, crucial para Windows
        if (e.isPopupTrigger()) {
            mostrarMenu(e);
        }
    }

    private void mostrarMenu(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        TreePath path = jTree1.getPathForLocation(x, y);

        // --- Depuración: Ver qué ruta encontramos ---
        System.out.println("Clic derecho detectado. Ruta encontrada: " + path);

        if (path == null) {
            // Clic en un área vacía
            System.out.println("Clic en área vacía. Estableciendo nodoClickeado a null.");
            nodoClickeado = null;
            // No seleccionamos nada en el árbol
            jTree1.clearSelection();
        } else {
            // Clic sobre un nodo
            // ¡Importante! Asegurarnos de que el nodo se seleccione visualmente.
            jTree1.setSelectionPath(path);
            
            // Asignamos el nodo encontrado a nuestra variable miembro.
            nodoClickeado = (DefaultMutableTreeNode) path.getLastPathComponent();
            System.out.println("Clic en nodo: " + nodoClickeado.getUserObject().toString() + ". Variable nodoClickeado asignada.");
        }

        // configuramos qué opciones del menú están habilitadas
        configurarOpcionesMenu();
        
        // Mostramos el menú
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
     
     
    private void configurarOpcionesMenu() {
        // --- USUARIO ---
        if (this.modoActual == ModoUsuario.USUARIO) {
            // Si estamos en modo USUARIO, deshabilitamos todas las operaciones de escritura.
            crearDirectorioItem.setEnabled(false);
            crearArchivoItem.setEnabled(false);
            renombrarItem.setEnabled(false);
            eliminarItem.setEnabled(false);

            // Salimos del método. No necesitamos hacer más comprobaciones.
            return; 
        }

        // --- ADMINISTRADOR ---

        boolean hayNodoSeleccionado = (nodoClickeado != null);

        // Por defecto, habilitar las opciones de creación
        crearDirectorioItem.setEnabled(true);
        crearArchivoItem.setEnabled(true);

        if (hayNodoSeleccionado) {
            boolean esRaiz = nodoClickeado.isRoot();
            Object objetoUsuario = nodoClickeado.getUserObject();

            eliminarItem.setEnabled(!esRaiz);
            renombrarItem.setEnabled(!esRaiz);

            if (objetoUsuario instanceof Archivo) {
                crearDirectorioItem.setEnabled(false);
                crearArchivoItem.setEnabled(false);
            }
        } else {
            // Clic en área vacía, solo se puede crear.
            eliminarItem.setEnabled(false);
            renombrarItem.setEnabled(false);
        }
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
        
        //Aquí se actualiza el JPanelDisco
        actualizarBloquesDesdeArbol(jTree1);
        
        //Aquí se actualiza la tabla de asignación de archivos
        actualizarTablaArchivos((DefaultMutableTreeNode) jTree1.getModel().getRoot());
    }
     
    /**
     * Las siguientes funciones son para crear y refrescar el JPanel de simulador de disco.
     * 
     * 
     */
    
    //Funciones para actualizar y crear el JPanelDisco
    public void inicializarPanelDisco() {
        // Crear instancia de tu panel personalizado
        panelDisco = new PanelDisco(200, 20);

        // Reemplazar jPanelDisco por discoPanel
        // Supongamos que jPanelDisco tiene un padre que es un contenedor (ej. otro JPanel o JFrame)
        java.awt.Container parent = JPanelDisco.getParent(); 

        // Obtén layout del contenedor  para reañadir correctamente
        java.awt.LayoutManager layout = parent.getLayout();

        // Remover jPanelDisco
        parent.remove(JPanelDisco);

        // Añadir nuevo panel personalizado en la misma posición/layout
        if (layout instanceof java.awt.BorderLayout) {
            parent.add(panelDisco, BorderLayout.CENTER);  // Si antes estaba en CENTER
        } else {
            parent.add(panelDisco);  // Ajusta según layout
        }
        

        // Actualiza el layout y refresca UI
        parent.revalidate(); 
        parent.repaint();
    }
    
    public void actualizarBloquesDesdeArbol(javax.swing.JTree jTree) {
        // Primero limpia la ocupación
        panelDisco.resetBloques();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jTree.getModel().getRoot();
        recorrerNodoYMarcar(root);

        // Finalmente, pide repaint para actualizar visualización
        panelDisco.repaint();
    }
    
    private void recorrerNodoYMarcar(DefaultMutableTreeNode nodo) {
        Object obj = nodo.getUserObject();
        if (obj instanceof Archivo) {
            Archivo archivo = (Archivo) obj;
            for (int i = archivo.getPrimerBloque(); i < archivo.getPrimerBloque() + archivo.getTamanoEnBloques(); i++) {
                panelDisco.setBloqueOcupado(i, true);
            }
        }

        // Si es directorio, recorre hijos
        for (int i = 0; i < nodo.getChildCount(); i++) {
            recorrerNodoYMarcar((DefaultMutableTreeNode) nodo.getChildAt(i));
        }
    }
    
    /**
     * Funciones para actualizar la tabla de asignación de archivos.
     */
    
    private void actualizarTablaArchivos(DefaultMutableTreeNode nodoRaiz) {
        DefaultTableModel modelo = new DefaultTableModel(
            new String[] { "Nombre", "Bloque Inicial", "Tamaño en Bloques" }, 0
        );

        agregarArchivosATabla(nodoRaiz, modelo);

        this.TablaArchivos.setModel(modelo);
    }
    
    //Recorre el JTree para actualizar la tabla
    private void agregarArchivosATabla(DefaultMutableTreeNode nodo, DefaultTableModel modelo) {
        Object obj = nodo.getUserObject();
        if (obj instanceof Archivo) {
            Archivo archivo = (Archivo) obj;
            modelo.addRow(new Object[] {
                archivo.getNombre(),
                archivo.getPrimerBloque(),
                archivo.getTamanoEnBloques()
            });
        }

        for (int i = 0; i < nodo.getChildCount(); i++) {
            agregarArchivosATabla((DefaultMutableTreeNode) nodo.getChildAt(i), modelo);
        }
    }
    
    
    /**
    * Configura y arranca el Timer que actúa como el motor principal de la simulación.
    * Con el modelo de 3 estados, el motor es muy simple.
    */
    private void inicializarMotorSimulador() {
        
        int velocidadSimulacionMS = 500; // 0,5 segundos por "tick"

        // El ActionListener que se ejecutará en cada "tick" del timer.
        ActionListener tickListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // 1. Fase de Ejecución de E/S: Mueve un proceso de BLOQUEADO a TERMINADO,
                //    pasando por el estado EJECUTANDO.
                sistemaManager.procesarSiguienteSolicitudIO();

                // 2. Actualiza toda la interfaz gráfica para reflejar cualquier cambio.
                actualizarTodasLasVistas();
            }
       };

    // Creamos el timer con la velocidad y el listener.
    this.motorSimulador = new Timer(velocidadSimulacionMS, tickListener);
    
    // Lo iniciamos para que comience a hacer "ticks".
    this.motorSimulador.start();
}
    
    
    private void actualizarListasDeProcesos() {
    
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

        // Esta es la llamada correcta que debería funcionar
         if (listManager != null) {
             listManager.actualizarListas(sistemaManager);
        }

        //  Solo actualizamos el JTree si el manager nos dice que algo cambió.
         if (sistemaManager.verificarYResetearCambioEnEstructura()) {
                System.out.println("¡Cambio detectado en el árbol! Actualizando JTree...");
                actualizarArbol();
            }
        // Para depurar
        // System.out.println("Actualizando vistas. Procesos en cola Bloqueado: " + sistemaManager.getColaBloqueados().getTamano());
    }
    
    
    
    /**
    * Método auxiliar para actualizar los textos de la UI que indican el modo actual.
    */
    private void actualizarUIModo() {
        if (this.modoActual == ModoUsuario.ADMINISTRADOR) {
            // Actualiza el texto del botón
            modoToggleButton.setText("Modo: Administrador");
            // Actualiza el JTextArea si lo estás usando para esto
            modoAct.setText("ADMINISTRADOR");
            System.out.println("Cambiado a Modo Administrador.");
        } else {
            modoToggleButton.setText("Modo: Usuario");
            modoAct.setText("USUARIO");
            System.out.println("Cambiado a Modo Usuario.");
        }
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
        jScrollPane2 = new javax.swing.JScrollPane();
        TablaArchivos = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel85 = new javax.swing.JLabel();
        CargarConfig = new javax.swing.JButton();
        GuardarConfig = new javax.swing.JButton();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        Simulador = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        ListaEjecutando = new javax.swing.JList<>();
        jLabel27 = new javax.swing.JLabel();
        jScrollPane18 = new javax.swing.JScrollPane();
        ListaTerminado = new javax.swing.JList<>();
        jLabel31 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jScrollPane24 = new javax.swing.JScrollPane();
        ListaBloqueado = new javax.swing.JList<>();
        jLabel43 = new javax.swing.JLabel();
        modoAct = new javax.swing.JTextArea();
        modoToggleButton = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        PoliticaPlanificacion = new javax.swing.JComboBox<>();
        CambiarPolitica = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        JPanelDisco = new PanelDisco(200, 20);

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

        TablaArchivos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre", "Bloque Inicial", "Tamaño en Bloques"
            }
        ));
        jScrollPane2.setViewportView(TablaArchivos);

        javax.swing.GroupLayout VisualizadorDiscoLayout = new javax.swing.GroupLayout(VisualizadorDisco);
        VisualizadorDisco.setLayout(VisualizadorDiscoLayout);
        VisualizadorDiscoLayout.setHorizontalGroup(
            VisualizadorDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VisualizadorDiscoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1035, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(552, Short.MAX_VALUE))
        );
        VisualizadorDiscoLayout.setVerticalGroup(
            VisualizadorDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VisualizadorDiscoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 735, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        Configuracion.addTab("Tabla de asignacion de archivos", VisualizadorDisco);

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        jPanel13.setBackground(new java.awt.Color(153, 153, 255));
        jPanel13.setForeground(new java.awt.Color(255, 255, 255));

        CargarConfig.setText("Cargar Configuración");
        CargarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CargarConfigActionPerformed(evt);
            }
        });

        GuardarConfig.setText("Guardar Configuración");
        GuardarConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GuardarConfigActionPerformed(evt);
            }
        });

        jLabel86.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel86.setText("Configuracion Con archivo JSON");

        jLabel87.setText("Guarda o carga la Configuracion del sitema de archivos");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(162, 162, 162)
                        .addComponent(jLabel85))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel86)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(CargarConfig)
                                .addGap(66, 66, 66)
                                .addComponent(GuardarConfig)))))
                .addContainerGap(37, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel87)
                .addGap(78, 78, 78))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(jLabel86)
                .addGap(26, 26, 26)
                .addComponent(jLabel87)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel85)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CargarConfig)
                    .addComponent(GuardarConfig))
                .addGap(49, 49, 49))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(1109, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(492, 492, 492))
        );

        Configuracion.addTab("Configuracion", jPanel3);

        Simulador.setBackground(new java.awt.Color(102, 204, 255));

        jScrollPane17.setViewportView(ListaEjecutando);

        jLabel27.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("Terminado");

        jScrollPane18.setViewportView(ListaTerminado);

        jLabel31.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Ejecutando");

        jLabel33.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel33.setText("Modo:");

        jLabel34.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jScrollPane24.setViewportView(ListaBloqueado);

        jLabel43.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("Bloqueado");

        modoAct.setColumns(20);
        modoAct.setRows(5);

        modoToggleButton.setText("Modo: Administrador");
        modoToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modoToggleButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Visualizador de Bloques");

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jLabel44.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel44.setText("Cambiar Política de Planificación");

        PoliticaPlanificacion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "C-SCAN" }));
        PoliticaPlanificacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PoliticaPlanificacionActionPerformed(evt);
            }
        });

        CambiarPolitica.setText("Guardar Cambios");
        CambiarPolitica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CambiarPoliticaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PoliticaPlanificacion, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CambiarPolitica, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel44)
                .addGap(18, 18, 18)
                .addComponent(PoliticaPlanificacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CambiarPolitica)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(51, 204, 255));
        jPanel2.setLayout(new java.awt.BorderLayout());

        JPanelDisco.setMaximumSize(new java.awt.Dimension(400, 200));
        JPanelDisco.setMinimumSize(new java.awt.Dimension(100, 100));
        JPanelDisco.setLayout(new java.awt.BorderLayout());
        jPanel2.add(JPanelDisco, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout SimuladorLayout = new javax.swing.GroupLayout(Simulador);
        Simulador.setLayout(SimuladorLayout);
        SimuladorLayout.setHorizontalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(67, 67, 67)
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(440, 440, 440))
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modoAct, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(modoToggleButton)))
                        .addGap(76, 76, 76)
                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGap(633, 633, 633)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 840, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(120, Short.MAX_VALUE))
        );
        SimuladorLayout.setVerticalGroup(
            SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SimuladorLayout.createSequentialGroup()
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(jLabel1)
                        .addGap(56, 56, 56)
                        .addComponent(jLabel34)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SimuladorLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)))
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(jLabel43)
                            .addComponent(jLabel27))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                            .addGroup(SimuladorLayout.createSequentialGroup()
                                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jScrollPane17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE)
                                    .addComponent(jScrollPane24, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(SimuladorLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 163, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SimuladorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(modoAct, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(modoToggleButton))
                    .addComponent(jLabel33))
                .addGap(16, 16, 16))
        );

        Configuracion.addTab("Visualizador Discos", Simulador);

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

    private void modoToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modoToggleButtonActionPerformed
        // El JToggleButton tiene un estado "seleccionado"
    if (modoToggleButton.isSelected()) {
        // Si está seleccionado, estamos en modo Administrador
        this.modoActual = ModoUsuario.ADMINISTRADOR;
    } else {
        // Si no está seleccionado, estamos en modo Usuario
        this.modoActual = ModoUsuario.USUARIO;
    }
    
    // Actualizamos la UI para que refleje el cambio
    actualizarUIModo();
    }//GEN-LAST:event_modoToggleButtonActionPerformed

    private void CambiarPoliticaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CambiarPoliticaActionPerformed
        String seleccion = (String) PoliticaPlanificacion.getSelectedItem();
        this.sistemaManager.cambiarPolitica(seleccion);
    }//GEN-LAST:event_CambiarPoliticaActionPerformed

    private void PoliticaPlanificacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PoliticaPlanificacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PoliticaPlanificacionActionPerformed

    private void CargarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CargarConfigActionPerformed
                                         
        JFileChooser fileChooser = new JFileChooser(".");

        // FILTRAR ARCHIVOS: Mostrar solo archivos .json ---
        // Creamos un filtro que solo acepta directorios y archivos con extensión .json.
        FileNameExtensionFilter filtroJson = new FileNameExtensionFilter("Archivos JSON (*.json)", "json");
        fileChooser.setFileFilter(filtroJson);

        // Opcional: Para evitar que el usuario pueda seleccionar "Todos los archivos"
        // fileChooser.setAcceptAllFileFilterUsed(false);

        // --- 2. Mostrar el diálogo de apertura ---
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            // Obtenemos el archivo seleccionado
            java.io.File archivoSeleccionado = fileChooser.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();

            //  Asegurarnos de que el archivo termina en .json ---
            if (!ruta.toLowerCase().endsWith(".json")) {
                JOptionPane.showMessageDialog(
                    this, 
                    "Por favor, seleccione un archivo con la extensión .json.", 
                    "Archivo Inválido", 
                    JOptionPane.ERROR_MESSAGE
                );
                return; // Detenemos la ejecución si el archivo no es válido.
            }

            // Si pasamos el filtro y la verificación, procedemos a cargar.
            sistemaManager.cargarConfiguracion(ruta);

            // Forzamos una actualización inmediata de la UI.
            actualizarArbol();
        }
    }//GEN-LAST:event_CargarConfigActionPerformed

    private void GuardarConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GuardarConfigActionPerformed
        JFileChooser fileChooser = new JFileChooser(".");
        FileNameExtensionFilter filtroJson = new FileNameExtensionFilter("Archivos JSON (*.json)", "json");
        fileChooser.setFileFilter(filtroJson);

        int resultado = fileChooser.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            java.io.File archivoSeleccionado = fileChooser.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();

            // Asegurarnos de que la extensión .json esté presente
            if (!ruta.toLowerCase().endsWith(".json")) {
                ruta += ".json";
            }

            // VERIFICACIÓN MANUAL DE SOBRESCRITURA
            java.io.File archivoFinal = new java.io.File(ruta);

            //  Comprobar si el archivo ya existe.
            if (archivoFinal.exists()) {
                //  Si existe, mostrar nuestro propio diálogo de confirmación.
                int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo '" + archivoFinal.getName() + "' ya existe.\n¿Desea reemplazarlo?",
                    "Confirmar Guardado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                //  Si el usuario no presiona "Sí", abortamos la operación.
                if (confirmacion != JOptionPane.YES_OPTION) {
                    System.out.println("Guardado cancelado por el usuario para evitar sobrescritura.");
                    return; // Salimos del método sin guardar.
                }
            }

            //  Si el archivo no existía o si el usuario confirmó la sobrescritura, procedemos a guardar.
            sistemaManager.guardarConfiguracion(ruta);
        }
    }//GEN-LAST:event_GuardarConfigActionPerformed

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
    private javax.swing.JButton CambiarPolitica;
    private javax.swing.JButton CargarConfig;
    private javax.swing.JTabbedPane Configuracion;
    private javax.swing.JButton GuardarConfig;
    private javax.swing.JPanel JPanelDisco;
    private javax.swing.JList<Proceso> ListaBloqueado;
    private javax.swing.JList<Proceso> ListaEjecutando;
    private javax.swing.JList<Proceso> ListaTerminado;
    private javax.swing.JComboBox<String> PoliticaPlanificacion;
    private javax.swing.JPanel Simulador;
    private javax.swing.JTable TablaArchivos;
    private javax.swing.JPanel VisualizadorDisco;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JTree jTree1;
    private javax.swing.JTextArea modoAct;
    private javax.swing.JToggleButton modoToggleButton;
    // End of variables declaration//GEN-END:variables
}
