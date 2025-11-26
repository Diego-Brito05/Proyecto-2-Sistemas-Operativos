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
import EstructuraDeDatos.ListaEnlazada;
import EstructuraDeDatos.Nodo;
import Politica.DireccionScan;
import Politica.PoliticaCSCAN;
import Politica.PoliticaFIFO;
import Politica.PoliticaPlanificacion;
import Politica.PoliticaSCAN;
import Politica.PoliticaSSTF;
import Proceso.EstadoProceso;
import Proceso.Proceso;
import Proceso.SolicitudIO;
import Proceso.TipoOperacionIO;
import static Proceso.TipoOperacionIO.ACTUALIZAR;
import static Proceso.TipoOperacionIO.CREAR;
import static Proceso.TipoOperacionIO.ELIMINAR;


//--Imports para guardar y cargar archivos y directorios al Jtree--
import java.io.FileWriter; 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Diego
 */
public class SistemaManager {

    private Directorio directorioRaiz;
    private Bloque[] disco;
    private static final int TAMANO_DISCO = 200;
    private int tiempoRestanteEjecucionIO;
    private static final int TIEMPO_POR_TICK = 500; // Debe coincidir con el Timer de motorSimulador de VentanaPrincipal
    
      // --- BANDERA DE ESTADO ---
    private boolean huboCambioEnEstructura = false;
    
    // Colas de estados de proceso
    private Proceso procesoEnEjecucionIO; // Proceso cuya E/S se está ejecutando
    private Cola<Proceso> colaBloqueados;
    private Cola<Proceso> colaTerminados;
    
    // Cola de E/S y Planificador
    private Cola<SolicitudIO> colaIO;
    private PlanificadorDisco planificador;
    
    // Variable de pausa
    private boolean pausa;

    public SistemaManager() {
        inicializarDisco();
        directorioRaiz = new Directorio("C:", null);
        
        this.tiempoRestanteEjecucionIO = 0;
        this.procesoEnEjecucionIO = null;
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        
        this.colaIO = new Cola<>();
        this.planificador = new PlanificadorDisco();
        this.pausa = false;
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
    
    public void cambiarPolitica(String Spolitica) {
        PoliticaPlanificacion politica = null;
        if (null == Spolitica) {
            politica = new PoliticaFIFO();
        } else switch (Spolitica) {
            case "C-SCAN":
                politica = new PoliticaCSCAN(this.planificador);
                break;
            case "SCAN":
                politica = new PoliticaSCAN(this.planificador);
                break;
            case "SSTF":
                politica = new PoliticaSSTF();
                break;
            default:
                politica = new PoliticaFIFO();
                break;
        }
        this.planificador.setPolitica(politica);
    }

    // --- MÉTODOS DE SOLICITUD (LLAMADOS DESDE LA UI) ---
    public void solicitarCreacionArchivo(Directorio padre, String nombre, int tamano) {
        int proximoId = Proceso.peekNextId();
        String rutaCompleta = construirRutaCompleta(padre) + "/" + nombre;

        // El 'bloqueObjetivo' para una creación no es tan relevante para la planificación
        // como lo es para una lectura o escritura en un archivo existente.
        // Usamos 0 como un objetivo simbólico.
        SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.CREAR, rutaCompleta, tamano, 0);

        Proceso p = new Proceso("Crear " + nombre, solicitud);
        p.setEstado(EstadoProceso.BLOQUEADO); 

        colaBloqueados.encolar(p);             
        colaIO.encolar(solicitud);             

        System.out.println("Proceso " + p.getId() + " creado y encolado directamente en BLOQUEADO.");
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

            // 1. Crear la SolicitudIO
            int proximoId = Proceso.peekNextId();
            String ruta = construirRutaCompleta(entrada);
            int bloqueObjetivo = 0;
            if (entrada instanceof Archivo) {
                bloqueObjetivo = ((Archivo) entrada).getPrimerBloque();
            }
            SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.ELIMINAR, ruta, 0, bloqueObjetivo);

            // 2. Crear el Proceso
            Proceso p = new Proceso("Eliminar " + entrada.getNombre(), solicitud);

            // 3. Establecer el estado directamente a BLOQUEADO
            p.setEstado(EstadoProceso.BLOQUEADO);

            // 4. Encolar el proceso en la cola de BLOQUEADOS
            colaBloqueados.encolar(p);

            // 5. Encolar la solicitud de E/S inmediatamente en la cola del disco
            colaIO.encolar(solicitud);
            // ---------------------

            System.out.println("Proceso " + p.getId() + " encolado para eliminar '" + entrada.getNombre() + "' y puesto en BLOQUEADO.");
        }
        
        /**
        * Crea un proceso para solicitar la creación de un nuevo directorio.
        * @param padre El directorio donde se creará el nuevo directorio.
        * @param nombre El nombre del nuevo directorio.
        */
        public void solicitarCreacionDirectorio(Directorio padre, String nombre) {
            int proximoId = Proceso.peekNextId();
            String rutaCompleta = construirRutaCompleta(padre) + "/" + nombre;
            SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.CREAR_DIRECTORIO, rutaCompleta, 0, 0);

            Proceso p = new Proceso("Crear Dir " + nombre, solicitud);
            p.setEstado(EstadoProceso.BLOQUEADO); 

            colaBloqueados.encolar(p);            
            colaIO.encolar(solicitud);             

            System.out.println("Proceso " + p.getId() + " creado y encolado directamente en BLOQUEADO.");
        }
        
        /**
        * Crea un proceso para solicitar el renombrado de un archivo o directorio.
        * @param entrada La entrada del sistema de archivos a renombrar.
        * @param nuevoNombre El nuevo nombre para la entrada.
        */
        public void solicitarRenombrar(EntradaSistemaArchivos entrada, String nuevoNombre) {
            int proximoId = Proceso.peekNextId();
            String ruta = construirRutaCompleta(entrada);

            // Para renombrar, el bloque objetivo puede ser el del archivo o 0 para un directorio.
            int bloqueObjetivo = (entrada instanceof Archivo) ? ((Archivo) entrada).getPrimerBloque() : 0;

            SolicitudIO solicitud = new SolicitudIO(proximoId, TipoOperacionIO.ACTUALIZAR, ruta, 0, bloqueObjetivo, nuevoNombre);

            Proceso p = new Proceso("Renombrar a " + nuevoNombre, solicitud);
            p.setEstado(EstadoProceso.BLOQUEADO);

            colaBloqueados.encolar(p);
            colaIO.encolar(solicitud);

            System.out.println("Proceso " + p.getId() + " encolado para renombrar '" + entrada.getNombre() + "' a '" + nuevoNombre + "'.");
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
    * Gestiona el ciclo de vida de la ejecución de E/S.
    * Si una operación está en curso, descuenta su tiempo.
    * Si no hay operación en curso, inicia una nueva desde la cola de bloqueados.
    */
    public void procesarSiguienteSolicitudIO() {
       // Caso 0: Está pausado.
       if (this.pausa) {
           return; //Salimos ya que el sistema está pausado.
       }
       
        // --- CASO 1: Hay una operación en curso ---
        if (procesoEnEjecucionIO != null) {
            // Descontamos el tiempo de un "tick"
            this.tiempoRestanteEjecucionIO -= TIEMPO_POR_TICK;
            System.out.println("Proceso " + procesoEnEjecucionIO.getId() + " en ejecución. Tiempo restante: " + tiempoRestanteEjecucionIO + "ms");

            // Verificamos si la operación ha terminado
            if (this.tiempoRestanteEjecucionIO <= 0) {
                System.out.println("Proceso " + procesoEnEjecucionIO.getId() + " ha terminado su E/S.");

                // Movemos el proceso a la cola de TERMINADOS
                procesoEnEjecucionIO.setEstado(EstadoProceso.TERMINADO);
                colaTerminados.encolar(procesoEnEjecucionIO);

                // Liberamos el "procesador" de E/S
                procesoEnEjecucionIO = null;
            }
            return; // Salimos, ya que en este tick solo procesamos la operación activa
        }

        // --- CASO 2: No hay operación en curso, intentamos iniciar una nueva ---
        if (colaIO.estaVacia()) {
            return; // No hay nada que hacer
        }

        // Seleccionamos la siguiente solicitud según la política
        SolicitudIO solicitud = planificador.seleccionarSiguiente(colaIO);
        if (solicitud == null) return;

        // Movemos el proceso de BLOQUEADO a EJECUTANDO
        Proceso proceso = buscarYRemoverDeCola(colaBloqueados, solicitud.getIdProceso());

        if (proceso != null) {
            System.out.println("Iniciando E/S para Proceso " + proceso.getId() + " (" + solicitud.getTipo() + ")");
            proceso.setEstado(EstadoProceso.EJECUTANDO);
            procesoEnEjecucionIO = proceso;

            // ¡ACCIÓN CLAVE! Establecemos el tiempo que durará esta operación.
            this.tiempoRestanteEjecucionIO = getDuracionOperacion(solicitud.getTipo());

            // Ejecutamos la lógica de modificación del sistema de archivos AHORA, al inicio.
            // La simulación de tiempo representa la espera a que el disco "termine".
            boolean exito = ejecutarOperacionReal(solicitud);

            if (!exito) {
                // Si la operación falla instantáneamente (ej. nombre duplicado), la terminamos de inmediato.
                System.out.println("ERROR: La operación para el proceso " + proceso.getId() + " ha fallado.");
                proceso.setEstado(EstadoProceso.TERMINADO); // O un nuevo estado "FALLIDO"
                colaTerminados.encolar(proceso);
                procesoEnEjecucionIO = null;
                this.tiempoRestanteEjecucionIO = 0;
            }
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
            case CREAR_DIRECTORIO:
                return _ejecutarCreacionDirectorio(solicitud);
            case ELIMINAR:
                return _ejecutarEliminacion(solicitud);
            case ACTUALIZAR: 
                return _ejecutarRenombrado(solicitud);
            default:
                return false;
            }
        }
        
        /**
        * Devuelve la duración en milisegundos para un tipo de operación de E/S.
        * @param tipo El tipo de operación.
        * @return La duración en ms.
        */
       private int getDuracionOperacion(TipoOperacionIO tipo) {
            switch (tipo) {
                case CREAR_DIRECTORIO:
                    return 1000;
                case CREAR:
                    return 1500;
                case ACTUALIZAR:
                    return 2000;
                case ELIMINAR:
                    return 2500;
                default:
                    return 500; // Un valor por defecto para operaciones no especificadas
            }
        }

       /**
        * Realiza la creación real de un archivo, incluyendo la verificación de espacio
        * y la asignación encadenada de bloques en el disco.
        * @param solicitud La solicitud de E/S de tipo CREAR.
        * @return true si la operación fue exitosa.
        */
        private boolean _ejecutarCreacionArchivo(SolicitudIO solicitud) {
           System.out.println("EJECUTANDO: Creación de archivo para Proceso " + solicitud.getIdProceso());
    
            int tamanoRequerido = solicitud.getTamanoEnBloques();

            // --- 1. VERIFICACIÓN DE ESPACIO ---
            if (contarBloquesLibres() < tamanoRequerido) {
                System.err.println("Error: Espacio insuficiente en disco. Se requieren " + tamanoRequerido + 
                                   " bloques, pero solo hay " + contarBloquesLibres() + " disponibles.");
                // Opcional: Podrías notificar al usuario con un JOptionPane aquí,
                // aunque es más complejo porque esto se ejecuta en el hilo del Timer.
                return false;
            }

            // ... (la lógica para encontrar el directorio padre se queda igual) ...
            String rutaCompleta = solicitud.getRuta();
            int ultimoSlash = rutaCompleta.lastIndexOf('/');
            String rutaPadre = (ultimoSlash > 0) ? rutaCompleta.substring(0, ultimoSlash) : directorioRaiz.getNombre();
            String nombreArchivo = rutaCompleta.substring(ultimoSlash + 1);

            EntradaSistemaArchivos entradaPadre = buscarEntradaPorRuta(rutaPadre);
            if (entradaPadre == null || !(entradaPadre instanceof Directorio)) {
                System.err.println("Error: Directorio padre no encontrado en la ruta: " + rutaPadre);
                return false;
            }
            Directorio padre = (Directorio) entradaPadre;


            // --- 2. ASIGNACIÓN ENCADENADA DE BLOQUES ---
            int primerBloqueAsignado = -1;
            int bloqueAnterior = -1;
            int bloquesAsignados = 0;

            // Empezamos la búsqueda desde el principio del disco
            int indiceBusqueda = 0; 

            while (bloquesAsignados < tamanoRequerido) {
                // Encontrar el siguiente bloque libre disponible
                int bloqueLibre = encontrarSiguienteBloqueLibre(indiceBusqueda);

                if (bloqueLibre == -1) {
                     // Esto no debería pasar si la comprobación de espacio inicial fue correcta,
                     // pero es una salvaguarda.
                     System.err.println("Error catastrófico: No se encontraron suficientes bloques libres a pesar de la verificación inicial.");
                     // Aquí deberíamos liberar los bloques que ya asignamos para este archivo (rollback)
                     // (Por simplicidad, omitimos el rollback por ahora)
                     return false;
                }

                // Marcar el bloque como ocupado
                getDisco()[bloqueLibre].ocupar(solicitud.getIdProceso());
                System.out.println("Asignando bloque " + bloqueLibre + " al archivo '" + nombreArchivo + "'.");

                if (primerBloqueAsignado == -1) {
                    // Este es el primer bloque que encontramos para el archivo
                    primerBloqueAsignado = bloqueLibre;
                }

                if (bloqueAnterior != -1) {
                    // Si ya teníamos un bloque asignado, lo encadenamos a este nuevo
                    getDisco()[bloqueAnterior].setSiguienteBloque(bloqueLibre);
                }

                // Actualizamos nuestras variables para la siguiente iteración
                bloqueAnterior = bloqueLibre;
                bloquesAsignados++;
                indiceBusqueda = bloqueLibre + 1; // Continuamos la búsqueda desde el siguiente bloque
            }

            // El último bloque de la cadena debe apuntar a FIN_DE_ARCHIVO
            if (bloqueAnterior != -1) {
                getDisco()[bloqueAnterior].setSiguienteBloque(Bloque.FIN_DE_ARCHIVO);
            }


            // --- 3. CREACIÓN DEL OBJETO ARCHIVO ---
            Archivo nuevoArchivo = new Archivo(nombreArchivo, padre, tamanoRequerido, primerBloqueAsignado, solicitud.getIdProceso(), "green");

            // Lo añadimos al directorio.
            boolean exito = padre.agregarEntrada(nuevoArchivo);
            
            if (exito) {
                // Si el archivo se agregó correctamente al directorio, activamos la bandera.
                this.huboCambioEnEstructura = true;
            }

            return exito;

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
            boolean exito = padre.agregarEntrada(nuevoDirectorio);

        if (exito) {
        this.huboCambioEnEstructura = true; // <-- ACTIVAR LA BANDERA
        }
        return exito;
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
        boolean exito = eliminarRecursivamente(entradaAEliminar);
    
        if (exito) {
        this.huboCambioEnEstructura = true; // <-- ACTIVAR LA BANDERA
        }
        return exito;
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
                    int siguienteBloque = getDisco()[bloqueActual].getSiguienteBloque();
                    getDisco()[bloqueActual].liberar();
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
    
    
    
    /**
    * Realiza el renombrado real de un archivo o directorio.
    * @param solicitud La solicitud de E/S de tipo ACTUALIZAR.
    * @return true si la operación fue exitosa.
    */
    private boolean _ejecutarRenombrado(SolicitudIO solicitud) {
        System.out.println("EJECUTANDO: Renombrado para Proceso " + solicitud.getIdProceso());

        // 1. Encontrar la entrada a renombrar por su ruta original.
        EntradaSistemaArchivos entrada = buscarEntradaPorRuta(solicitud.getRuta());
        if (entrada == null) {
            System.err.println("Error: No se encontró la entrada a renombrar: " + solicitud.getRuta());
            return false;
        }

        // 2. Obtener el nuevo nombre desde la solicitud.
        String nuevoNombre = solicitud.getNuevoNombre();
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            System.err.println("Error: El nuevo nombre es inválido.");
            return false;
        }

        // 3. ¡MUY IMPORTANTE! Verificar que no haya un nombre duplicado en el mismo directorio.
        Directorio padre = entrada.getPadre();
        if (padre != null && padre.contieneNombre(nuevoNombre)) {
            System.err.println("Error: Ya existe una entrada con el nombre '" + nuevoNombre + "' en este directorio.");
            // Podríamos notificar al usuario aquí si quisiéramos.
            return false;
        }

        // 4. Realizar el renombrado.
        entrada.setNombre(nuevoNombre);

        // 5. Activar la bandera para que el JTree se actualice.
        this.huboCambioEnEstructura = true;

        return true;
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
    public Proceso getProcesoEnEjecucionIO() { return procesoEnEjecucionIO; }
    public Cola<Proceso> getColaBloqueados() { return colaBloqueados; }
    public Cola<Proceso> getColaTerminados() { return colaTerminados; }
    public Cola<SolicitudIO> getColaIO() { return colaIO; }
    public PlanificadorDisco getPlanificador() { return planificador; }

    // --- Métodos de utilidad ---
    /**
    * Cuenta el número total de bloques libres en el disco.
    * @return El número de bloques disponibles.
    */
    private int contarBloquesLibres() {
        int contador = 0;
        for (int i = 0; i < TAMANO_DISCO; i++) {
            if (getDisco()[i] != null && !disco[i].isOcupado()) {
                contador++;
            }
        }
        return contador;
    }

   /**
    * Encuentra el índice del siguiente bloque libre, comenzando la búsqueda desde
    * una posición específica.
    * @param inicio El índice desde el cual empezar a buscar.
    * @return El índice del siguiente bloque libre, o -1 si no se encuentra ninguno.
    */
    private int encontrarSiguienteBloqueLibre(int inicio) {
        for (int i = inicio; i < TAMANO_DISCO; i++) {
            if (getDisco()[i] != null && !disco[i].isOcupado()) {
                return i;
            }
        }
        return -1; // No se encontraron más bloques libres
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
                encontrado = p; // Lo encontramos, No lo volvemos a encolar.
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
        
         /**
     * Comprueba si ha ocurrido un cambio en la estructura de archivos/directorios
     * desde la última vez que se comprobó.
     * Si hubo un cambio, devuelve true y resetea la bandera a false.
     * @return true si la estructura del árbol necesita ser actualizada, false en caso contrario.
     */
    public boolean verificarYResetearCambioEnEstructura() {
        if (this.huboCambioEnEstructura) {
            this.huboCambioEnEstructura = false; // Reseteamos la bandera
            
            //Se crea una lista de los archivos
            ListaEnlazada<Archivo> files = new ListaEnlazada<Archivo>();
            duplicarArchivos(this.directorioRaiz, files);
            ordenarArchivosAscendente(files);
            reubicarArchivos(files);
            
            return true; // Informamos que sí hubo un cambio
        }
        return false; // No hubo cambios
    }

    // --- Funciones de cargado y guardado del Jtree ---
     
    /**
     * Guarda el estado actual del sistema de archivos (la estructura del JTree)
     * en un archivo JSON.
     * @param rutaArchivo La ruta donde se guardará el archivo, ej. "configuracion.json".
     */
    public void guardarConfiguracion(String rutaArchivo) {
        if (directorioRaiz == null) {
            System.err.println("No hay sistema de archivos para guardar.");
            return;
        }

        // Convertimos el directorio raíz (y todo su contenido) a JSON.
        JSONObject jsonRaiz = directorioRaiz.toJson();

        // Escribimos el objeto JSON a un archivo de texto.
        try (FileWriter file = new FileWriter(rutaArchivo)) {
            file.write(jsonRaiz.toString(4)); // toString(4) para que el JSON sea legible (indentado)
            System.out.println("Configuración guardada exitosamente en " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar la configuración: " + e.getMessage());
        }        
    }
        
        
    /**
     * Carga un sistema de archivos desde un archivo de configuración JSON,
     * reemplazando la estructura actual.
     * @param rutaArchivo La ruta del archivo JSON a cargar.
     */
    public void cargarConfiguracion(String rutaArchivo) {
        try {
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            JSONObject jsonRaiz = new JSONObject(contenido);

            // Reconstruimos la estructura a partir del JSON.
            EntradaSistemaArchivos nuevaRaiz = reconstruirDesdeJson(jsonRaiz, null);

            
            // Verificamos que es realmente un Directorio
            // antes de asignarlo a directorioRaiz.
            if (nuevaRaiz instanceof Directorio) {
                // Hacemos el casting explícito para que los tipos coincidan.
                this.directorioRaiz = (Directorio) nuevaRaiz;

                this.huboCambioEnEstructura = true; // Notificamos que la UI debe actualizarse
                System.out.println("Configuración cargada exitosamente desde " + rutaArchivo);
            } else {
                // Esto solo pasaría si el archivo JSON está mal formado (la raíz es un archivo).
                System.err.println("Error: El archivo de configuración es inválido. La raíz debe ser un directorio.");
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo de configuración: " + e.getMessage());
        } catch (org.json.JSONException e) {
            System.err.println("Error al parsear el JSON: " + e.getMessage());
        }
    }
    
     /**
     * Método auxiliar recursivo que reconstruye la estructura de directorios
     * a partir de un objeto JSON.
     * @param jsonObject El JSONObject a procesar (puede ser un archivo o directorio).
     * @param padre El directorio padre de la entrada que se está creando.
     * @return La EntradaSistemaArchivos creada (Archivo o Directorio).
     */
    private EntradaSistemaArchivos reconstruirDesdeJson(JSONObject jsonObject, Directorio padre) {
        String tipo = jsonObject.getString("tipo");
        String nombre = jsonObject.getString("nombre");

        if ("DIRECTORIO".equals(tipo)) {
            Directorio nuevoDir = new Directorio(nombre, padre);
            JSONArray contenidoJson = jsonObject.getJSONArray("contenido");
            
            for (int i = 0; i < contenidoJson.length(); i++) {
                JSONObject hijoJson = contenidoJson.getJSONObject(i);
                // Llamada recursiva para cada hijo, pasando el directorio actual como padre
                nuevoDir.agregarEntrada(reconstruirDesdeJson(hijoJson, nuevoDir));
            }
            return nuevoDir;
            
        } else if ("ARCHIVO".equals(tipo)) {
            int tamano = jsonObject.getInt("tamanoEnBloques");
            int primerBloque = jsonObject.getInt("primerBloque");
            int idProceso = jsonObject.getInt("idProcesoCreador");
            String color = jsonObject.getString("color");
            
            // TODO: Al cargar, deberíamos marcar los bloques del disco como ocupados.
            // Esta lógica es más compleja y se puede añadir después.
            
            return new Archivo(nombre, padre, tamano, primerBloque, idProceso, color);
        }
        
        return null; // Tipo desconocido
    }
    
    /**
     * Funciones para reorganizar los archivos.
     * 
     */
    
    public void duplicarArchivos(Directorio dir, ListaEnlazada<Archivo> lista) {
        for (int n = 0; n < dir.getContenido().getTamano(); n++) {
            Object obj = dir.getContenido().obtener(n);
            if (obj instanceof Archivo) {
                lista.agregarAlFinal((Archivo) obj);
            } else if (obj instanceof Directorio) {
                duplicarArchivos((Directorio) obj, lista);
            }
        }
    }
    
    public void ordenarArchivosAscendente(ListaEnlazada <Archivo> lista) {
        if (!lista.estaVacia()) {
            int n = lista.getTamano();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n-i-1; j++) {
                    if (lista.obtener(j).getPrimerBloque() > lista.obtener(j+1).getPrimerBloque()) {
                        Archivo temp = lista.obtener(j);
                        lista.agregar(j, new Nodo(lista.obtener(j+1)));
                        lista.agregar(j+1, new Nodo(temp));
                    }
                }
            }
        }
    }
    
    public void reubicarArchivos(ListaEnlazada<Archivo> lista) {
        int siguienteBloque = 0;
        for (int i = 0; i < lista.getTamano(); i++) {
            Archivo archivo = lista.obtener(i);
            int tamano = archivo.getTamanoEnBloques();
            
            //Asigna el nuevo bloque inicial
            archivo.setPrimerBloque(siguienteBloque);
            
            //Actualiza el siguiente bloque libre
            siguienteBloque += tamano;
        }
    }
    
    /**
     * Otras funciones.
     *  
     */
    
    public boolean pausado() {
        return pausa;
    }
    
    public void setPausa(boolean newPausa) {
        this.pausa = newPausa;
    }

    
    public Bloque[] getDisco() {return disco;}

    }
    
    
    
    
    
    
    
    
    
