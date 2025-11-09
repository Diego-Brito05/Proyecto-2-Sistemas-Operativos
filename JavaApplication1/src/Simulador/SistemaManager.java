/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulador;

import Archivo.Archivo;
import Archivo.Bloque;
import Archivo.Directorio;
import Archivo.EntradaSistemaArchivos;
import EstructuraDeDatos.Cola;
import Politica.PoliticaCSCAN;
import Politica.PoliticaFIFO;
import Politica.PoliticaSCAN;
import Politica.PoliticaSSTF;
import Proceso.EstadoProceso;
import Proceso.Proceso;
import Proceso.SolicitudIO;
import Proceso.TipoOperacionIO;
import static Proceso.TipoOperacionIO.ACTUALIZAR;
import static Proceso.TipoOperacionIO.CREAR;
import static Proceso.TipoOperacionIO.ELIMINAR;

/**
 *
 * @author Diego
 */
public class SistemaManager {

    private Directorio directorioRaiz;
    private Bloque[] disco;
    private static final int TAMANO_DISCO = 200;
    
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
    
    /**
     * Crea el array que simula el disco y lo llena con objetos Bloque,
     * cada uno con su ID y en estado 'libre'.
     */
    private void inicializarDisco() {
        // 1. Crear el array del tamaño definido.
        this.disco = new Bloque[TAMANO_DISCO];
        
        // 2. Llenar cada posición del array con un nuevo objeto Bloque.
        for (int i = 0; i < TAMANO_DISCO; i++) {
            // Asumiendo que tu constructor de Bloque acepta un ID.
            this.disco[i] = new Bloque(i); 
        }
        System.out.println("Disco de " + TAMANO_DISCO + " bloques inicializado.");
    }

    // --- MÉTODOS DE SOLICITUD (LLAMADOS DESDE LA UI) ---
    public void solicitarCreacionArchivo(Directorio padre, String nombre, int tamano) {
        int proximoId = Proceso.peekNextId(); 
        SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.CREAR, padre.getNombre() + "/" + nombre, tamano, encontrarPrimerBloqueLibre());
        Proceso p = new Proceso("Crear " + nombre, solicitud);
        p.setEstado(EstadoProceso.NUEVO);
        colaNuevos.encolar(p);
    }
    
    /**
    * Crea un proceso para solicitar la eliminación de un archivo o directorio.
    * Este método es llamado por la UI y no ejecuta la eliminación directamente.
    * @param entrada La entrada del sistema de archivos (Archivo o Directorio) a eliminar.
    */
        public void solicitarEliminacion(EntradaSistemaArchivos entrada) {
            if (entrada == null || entrada.getPadre() == null) {
                System.err.println("Error: No se puede solicitar la eliminación de la raíz o de un objeto nulo.");
                return;
            }

            // 1. Obtener el próximo ID para la solicitud y el proceso.
            int proximoId = Proceso.peekNextId();

            // 2. Construir la ruta completa del elemento a eliminar.
            String ruta = construirRutaCompleta(entrada);

            // 3. Determinar el bloque objetivo para los algoritmos de planificación.
            //    Si es un archivo, es su primer bloque. Si es un directorio, podemos usar 0 o el bloque del padre.
            int bloqueObjetivo = 0;
            if (entrada instanceof Archivo) {
                bloqueObjetivo = ((Archivo) entrada).getPrimerBloque();
            }

            // 4. Crear la solicitud de E/S. El tamaño no es relevante para ELIMINAR.
            SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.ELIMINAR, ruta, 0, bloqueObjetivo);

            // 5. Crear el proceso con su solicitud asociada.
            Proceso p = new Proceso("Eliminar " + entrada.getNombre(), solicitud);
            p.setEstado(EstadoProceso.NUEVO);

            // 6. Encolar el proceso en la cola de NUEVOS para que el simulador lo recoja.
            colaNuevos.encolar(p);

            System.out.println("Proceso " + p.getId() + " encolado para eliminar '" + entrada.getNombre() + "'.");
        }
        
        /**
        * Crea un proceso para solicitar la creación de un nuevo directorio.
        * @param padre El directorio donde se creará el nuevo directorio.
        * @param nombre El nombre del nuevo directorio.
        */
        public void solicitarCreacionDirectorio(Directorio padre, String nombre) {
            int proximoId = Proceso.peekNextId();

            // Construimos la ruta completa para la solicitud.
            String rutaCompleta = construirRutaCompleta(padre) + "/" + nombre;

            // Un directorio no tiene tamaño en bloques ni un bloque objetivo inicial específico. Usamos 0.
            SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.CREAR_DIRECTORIO, rutaCompleta, 0, 0);

            Proceso p = new Proceso("Crear Dir " + nombre, solicitud);
            p.setEstado(EstadoProceso.NUEVO);
            colaNuevos.encolar(p);

            System.out.println("Proceso " + p.getId() + " encolado para crear directorio '" + nombre + "'.");
        }

   /**
    * Método de utilidad para construir la ruta completa de una entrada del sistema de archivos,
    * navegando hacia arriba desde la entrada hasta la raíz.
    * @param entrada La entrada de la cual se quiere obtener la ruta.
    * @return Un String con la ruta completa (ej: "C:/Users/Admin/archivo.txt").
    */
        private String construirRutaCompleta(EntradaSistemaArchivos entrada) {
            if (entrada.getPadre() == null) { // Caso base: es la raíz
                return entrada.getNombre();
            }

            // Llamada recursiva para construir la ruta del padre y luego añadir el nombre actual
            return construirRutaCompleta(entrada.getPadre()) + "/" + entrada.getNombre();
        }
    
    
    
    // --- MÉTODOS DEL CICLO DEL SIMULADOR ---
    
            /**
          * Mueve los procesos de la cola NUEVO a la cola LISTO.
          * Representa la admisión de un proceso en el sistema.
          */
        public void admitirNuevosProcesos() {
             if (!colaNuevos.estaVacia()) {
                 Proceso p = colaNuevos.desencolar();
                 p.setEstado(EstadoProceso.LISTO);
                 colaListos.encolar(p);
                 System.out.println("Proceso " + p.getId() + " admitido y movido a LISTO.");
            }
        }
         
         /**
        * Mueve los procesos de la cola LISTO a la cola BLOQUEADO.
        * En este paso, se genera la SolicitudIO y se encola para el disco.
        */
       public void prepararIO() {
           if (!colaListos.estaVacia()) {
               Proceso p = colaListos.desencolar();

               // Generar y encolar la solicitud de E/S
               colaIO.encolar(p.getSolicitudAsociada());

               // Mover el proceso a la cola de bloqueados
               p.setEstado(EstadoProceso.BLOQUEADO);
               colaBloqueados.encolar(p);
               System.out.println("Proceso " + p.getId() + " preparado para E/S y movido a BLOQUEADO.");
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
                    // --- INICIO DE LA EJECUCIÓN ---
                    proceso.setEstado(EstadoProceso.EJECUTANDO);
                    procesoEnEjecucionIO = proceso;

                    // ¡AQUÍ OCURRE LA MAGIA!
                    // Se llama al método que realmente modifica el sistema de archivos.
                    boolean exito = ejecutarOperacionReal(solicitud);

                    if (!exito) {
                        // Manejar el fallo (ej. no hay espacio)
                        System.out.println("ERROR: La operación para el proceso " + proceso.getId() + " ha fallado.");
                    }

                    // --- FIN DE LA EJECUCIÓN ---
                    proceso.setEstado(EstadoProceso.TERMINADO);
                    colaTerminados.encolar(proceso);
                    procesoEnEjecucionIO = null; // Liberar el "procesador" de E/S
                }
            }

        /**
         * Método "despachador" que llama a la función CRUD correcta basado en la solicitud.
         * ESTE ES EL ÚNICO LUGAR DONDE SE MODIFICA EL SISTEMA DE ARCHIVOS.
         * @param solicitud La solicitud de E/S a ejecutar.
         * @return true si la operación fue exitosa, false en caso contrario.
         */
        private boolean ejecutarOperacionReal(SolicitudIO solicitud) {
                switch (solicitud.getTipo()) {
                case CREAR:
                    return _ejecutarCreacionArchivo(solicitud);
                case CREAR_DIRECTORIO: // <-- AÑADIR ESTE CASO
                    return _ejecutarCreacionDirectorio(solicitud);
                case ELIMINAR:
                    return _ejecutarEliminacion(solicitud);
                case ACTUALIZAR:
                    // return _ejecutarRenombrado(solicitud);
                default:
                    return false;
            }
        }

        private boolean _ejecutarCreacionArchivo(SolicitudIO solicitud) {
            System.out.println("EJECUTANDO: Creación de archivo para Proceso " + solicitud.getIdProceso());

            // 1. Extraer la ruta del padre y el nombre del nuevo archivo
            String rutaCompleta = solicitud.getRuta();
            int ultimoSlash = rutaCompleta.lastIndexOf('/');
            if (ultimoSlash == -1) return false; // Ruta inválida

            String rutaPadre = rutaCompleta.substring(0, ultimoSlash);
            String nombreArchivo = rutaCompleta.substring(ultimoSlash + 1);

            // Si la ruta del padre es solo "C:", la dejamos así. Si no, y está vacía, es la raíz.
            if (rutaPadre.isEmpty()) rutaPadre = directorioRaiz.getNombre();

            // 2. Encontrar el directorio padre usando nuestra nueva función
            EntradaSistemaArchivos entradaPadre = buscarEntradaPorRuta(rutaPadre);

            // Validar que el padre exista y sea un directorio
            if (entradaPadre == null || !(entradaPadre instanceof Directorio)) {
                System.err.println("Error: Directorio padre no encontrado o no es un directorio: " + rutaPadre);
                return false;
            }
            Directorio padre = (Directorio) entradaPadre;

            // 3. Lógica de asignación de bloques (esto se queda igual)
            int primerBloque = solicitud.getBloqueObjetivo();
            // Aquí iría la lógica para marcar los bloques como ocupados

            // 4. Crear el objeto Archivo y añadirlo al modelo de datos (esto ahora funciona)
            Archivo nuevoArchivo = new Archivo(nombreArchivo, padre, solicitud.getTamanoEnBloques(), primerBloque, solicitud.getIdProceso(), "green");
            return padre.agregarEntrada(nuevoArchivo); 
        }
        
        /**
        * Realiza la creación real de un directorio en el sistema de archivos.
        * @param solicitud La solicitud de E/S de tipo CREAR_DIRECTORIO.
        * @return true si la operación fue exitosa.
        */
        private boolean _ejecutarCreacionDirectorio(SolicitudIO solicitud) {
            System.out.println("EJECUTANDO: Creación de directorio para Proceso " + solicitud.getIdProceso());

            // 1. Extraer la ruta del padre y el nombre del nuevo directorio.
            String rutaCompleta = solicitud.getRuta();
            int ultimoSlash = rutaCompleta.lastIndexOf('/');
            if (ultimoSlash == -1) return false;

            String rutaPadre = rutaCompleta.substring(0, ultimoSlash);
            String nombreDirectorio = rutaCompleta.substring(ultimoSlash + 1);

            // Corrección para el caso de la raíz
            if (rutaPadre.isEmpty()) rutaPadre = directorioRaiz.getNombre();

            // 2. Encontrar el directorio padre.
            EntradaSistemaArchivos entradaPadre = buscarEntradaPorRuta(rutaPadre);

            if (entradaPadre == null || !(entradaPadre instanceof Directorio)) {
                System.err.println("Error: Directorio padre no encontrado en la ruta: " + rutaPadre);
                return false;
            }
            Directorio padre = (Directorio) entradaPadre;

            // 3. Crear el nuevo objeto Directorio y añadirlo a su padre.
            Directorio nuevoDirectorio = new Directorio(nombreDirectorio, padre);
            return padre.agregarEntrada(nuevoDirectorio);
        }
        
    /**
     * Método principal que inicia el proceso de eliminación a partir de una solicitud.
     * Este es el método que se llama desde procesarSiguienteSolicitudIO.
     * @param solicitud La solicitud de E/S de eliminación.
     * @return true si la operación fue exitosa.
     */
    private boolean _ejecutarEliminacion(SolicitudIO solicitud) {
        System.out.println("EJECUTANDO: Eliminación para Proceso " + solicitud.getIdProceso());

        // 1. Encontrar la entrada (archivo/directorio) a partir de su ruta
        EntradaSistemaArchivos entradaAEliminar = buscarEntradaPorRuta(solicitud.getRuta());

        if (entradaAEliminar == null) {
            System.err.println("Advertencia: No se encontró la entrada a eliminar (quizás ya fue borrada): " + solicitud.getRuta());
            return true; // Consideramos éxito para que el proceso no se quede atascado
        }

        if (entradaAEliminar.getPadre() == null) {
            System.err.println("Error: No se puede eliminar el directorio raíz.");
            return false; // No se puede eliminar la raíz
        }

        // 2. Iniciar el proceso de borrado recursivo y desconexión
        return eliminarRecursivamente(entradaAEliminar);
    }


    /**
     * Función recursiva que elimina una entrada y todo su contenido.
     * Libera los bloques de los archivos y elimina las entradas de sus directorios padres.
     * @param entrada La entrada a eliminar.
     * @return true si la eliminación fue exitosa.
     */
    private boolean eliminarRecursivamente(EntradaSistemaArchivos entrada) {
            // Caso Recursivo: La entrada es un Directorio
        if (entrada instanceof Directorio) {
            Directorio dir = (Directorio) entrada;

            // Copiamos el contenido a un array para evitar problemas al modificar la lista mientras la recorremos.
            // Esto es crucial.
            Object[] contenidoCopia = new Object[dir.getContenido().getTamano()];
            for(int i = 0; i < dir.getContenido().getTamano(); i++) {
                contenidoCopia[i] = dir.getContenido().obtener(i);
            }

            // Llamada recursiva para cada hijo del directorio.
            for (Object item : contenidoCopia) {
                eliminarRecursivamente((EntradaSistemaArchivos) item);
            }
        }
        // Caso Base: La entrada es un Archivo (o después de vaciar un Directorio)
        else if (entrada instanceof Archivo) {
            // Lógica para liberar los bloques de un archivo
            Archivo archivo = (Archivo) entrada;
            int bloqueActual = archivo.getPrimerBloque();

            while (bloqueActual != Bloque.FIN_DE_ARCHIVO) {
                if (bloqueActual >= 0 && bloqueActual < TAMANO_DISCO) {
                    int siguienteBloque = disco[bloqueActual].getSiguienteBloque();
                    disco[bloqueActual].liberar();
                    System.out.println("Bloque " + bloqueActual + " liberado para archivo '" + archivo.getNombre() + "'.");
                    bloqueActual = siguienteBloque;
                } else {
                    System.err.println("Error: Puntero de bloque inválido encontrado durante la eliminación.");
                    break; 
                }
            }
        }

        // Acción Final (para ambos, Archivos y Directorios ya vacíos):
        // Quitar la referencia de esta entrada de su directorio padre.
        Directorio padre = entrada.getPadre();
        if (padre != null) {
            System.out.println("Eliminando '" + entrada.getNombre() + "' de '" + padre.getNombre() + "'.");
            return padre.eliminarEntrada(entrada);
        }

        return false; // No debería llegar aquí a menos que sea la raíz.
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
    public Cola<Proceso> getColaListos() { return colaListos;}
    public Cola<Proceso> getColaBloqueados() { return colaBloqueados; }
    public Cola<Proceso> getColaTerminados() { return colaTerminados; }
    public Cola<SolicitudIO> getColaIO() { return colaIO; }
    public PlanificadorDisco getPlanificador() { return planificador; }

    // --- Métodos de utilidad ---
    /**
    * Busca el primer bloque libre en el disco.
    * @return El índice del primer bloque libre, o -1 si el disco está lleno.
    */
    private int encontrarPrimerBloqueLibre() {
        for (int i = 0; i < TAMANO_DISCO; i++) {
            // Asumiendo que tu clase Bloque tiene un método isOcupado()
            if (disco[i] != null && !disco[i].isOcupado()) {
                return i;
            }
        }
        return -1; // No hay espacio
    }

    /**
     * Busca un proceso por su ID en una cola, lo saca de ella y lo devuelve.
     * Es crucial para mover procesos entre colas.
     * @param cola La cola en la que se va a buscar.
     * @param idProceso El ID del proceso a buscar.
     * @return El objeto Proceso si se encuentra, o null si no está en la cola.
     */
    private Proceso buscarYRemoverDeCola(Cola<Proceso> cola, int idProceso) {
        Cola<Proceso> colaAuxiliar = new Cola<>();
        Proceso encontrado = null;

        // Vaciamos la cola original, buscando el proceso
        while (!cola.estaVacia()) {
            Proceso p = cola.desencolar();
            if (p.getId() == idProceso) {
                encontrado = p; // ¡Lo encontramos! No lo volvemos a encolar.
            } else {
                colaAuxiliar.encolar(p); // No es, lo guardamos en la cola auxiliar.
            }
        }

        // Restauramos la cola original con los elementos que no fueron removidos
        while (!colaAuxiliar.estaVacia()) {
            cola.encolar(colaAuxiliar.desencolar());
        }

        return encontrado;
    }
    
                

        
        /**
         * Busca una EntradaSistemaArchivos (archivo o directorio) a partir de su ruta completa.
         * @param ruta La ruta completa, ej: "C:/Users/Admin/archivo.txt"
         * @return El objeto EntradaSistemaArchivos si se encuentra, o null si la ruta es inválida.
         */
        private EntradaSistemaArchivos buscarEntradaPorRuta(String ruta) {
            String[] partes = ruta.split("/");

            // Validación básica: la ruta debe empezar con el nombre de la raíz
            if (partes.length == 0 || !partes[0].equals(directorioRaiz.getNombre())) {
                return null;
            }

            EntradaSistemaArchivos actual = directorioRaiz;

            // Iteramos por cada parte de la ruta, empezando después de la raíz (i=1)
            for (int i = 1; i < partes.length; i++) {
                String nombreBuscado = partes[i];

                // Si en medio de la ruta encontramos un archivo, la ruta es inválida
                if (!(actual instanceof Directorio)) {
                    return null; 
                }

                Directorio dirActual = (Directorio) actual;
                EntradaSistemaArchivos siguiente = null;

                // Buscamos el siguiente componente en el contenido del directorio actual
                for (int j = 0; j < dirActual.getContenido().getTamano(); j++) {
                    EntradaSistemaArchivos hijo = dirActual.getContenido().obtener(j);
                    if (hijo.getNombre().equals(nombreBuscado)) {
                        siguiente = hijo;
                        break; // Lo encontramos, salimos del bucle interior
                    }
                }

                // Si no encontramos el siguiente componente, la ruta no existe
                if (siguiente == null) {
                    return null;
                }

                // Avanzamos al siguiente nivel de la ruta
                actual = siguiente;
            }

            return actual; // Devolvemos la entrada final encontrada
        }
        
        
        


    }
