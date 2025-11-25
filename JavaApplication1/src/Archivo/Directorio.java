/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Archivo;
import org.json.JSONArray; // para pasar el archivo a formato Json
import org.json.JSONObject; 
import EstructuraDeDatos.ListaEnlazada;

/**
 *
 * @author Diego
 */
public class Directorio extends EntradaSistemaArchivos {
    private ListaEnlazada<EntradaSistemaArchivos> contenido;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.contenido = new ListaEnlazada<>();
    }
    

    // --- MÉTODO PREFERIDO ---
    /**
     * Elimina una entrada del contenido de este directorio, dado el objeto exacto a eliminar.
     * Este método es más eficiente porque no necesita buscar por nombre.
     * @param entrada El objeto Archivo o Directorio a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public boolean eliminarEntrada(EntradaSistemaArchivos entrada) {
        // Simplemente delega la eliminación a nuestra ListaEnlazada personalizada.
        // Esto asume que tu ListaEnlazada.java tiene el método `eliminar(T dato)`.
        return this.getContenido().eliminar(entrada);
    }

    /**
     * Elimina una entrada buscando por su nombre. Es útil si solo se dispone del nombre.
     * @param nombre El nombre del Archivo o Directorio a eliminar.
     * @return true si se encontró y eliminó, false en caso contrario.
     */
    public boolean eliminarEntrada(String nombre) {
        // Iteramos para encontrar el objeto que corresponde a ese nombre
        for (int i = 0; i < getContenido().getTamano(); i++) {
            EntradaSistemaArchivos entradaActual = getContenido().obtener(i);
            if (entradaActual.getNombre().equals(nombre)) {
                // Una vez encontrado, llamamos al método de nuestra ListaEnlazada
                return getContenido().eliminar(entradaActual);
            }
        }
        return false; // No se encontró ninguna entrada con ese nombre
    }
    
   /**
     * Verifica si este directorio ya contiene una entrada con el nombre especificado.
     * La comparación no distingue entre mayúsculas y minúsculas para simular
     * un comportamiento más realista (ej. como en Windows).
     * @param nombre El nombre a buscar.
     * @return true si ya existe una entrada con ese nombre, false en caso contrario.
     */
    public boolean contieneNombre(String nombre) {
        for (int i = 0; i < this.getContenido().getTamano(); i++) {
            if (this.getContenido().obtener(i).getNombre().equalsIgnoreCase(nombre)) {
                return true; // Se encontró una coincidencia
            }
        }
        return false; // No se encontraron duplicados
    }

    /**
     * Añade una nueva entrada (Archivo o Directorio) al contenido de este directorio,
     * pero solo si no existe ya una entrada con el mismo nombre.
     * @param entrada El archivo o directorio a añadir.
     * @return true si la entrada fue añadida con éxito, false si ya existía un nombre duplicado.
     */
    public boolean agregarEntrada(EntradaSistemaArchivos entrada) {
        // 1. Usamos nuestro nuevo método para verificar si el nombre ya existe.
        if (this.contieneNombre(entrada.getNombre())) {
            // Si ya existe, imprimimos un error y devolvemos 'false' para indicar el fallo.
            System.err.println("Error: Ya existe una entrada con el nombre '" + entrada.getNombre() + "' en el directorio '" + this.getNombre() + "'.");
            return false;
        }
        
        // 2. Si no hay duplicados, procedemos a añadir la entrada.
        this.getContenido().agregarAlFinal(entrada);
        
        // 3. Devolvemos 'true' para indicar que la operación fue un éxito.
        return true;
    }

    /**
     * @return the contenido
     */
    public ListaEnlazada<EntradaSistemaArchivos> getContenido() {
        return contenido;
    }
    
    
    
    
      /**
     * Convierte este directorio y todo su contenido a un objeto JSON de forma recursiva.
     * @return Un JSONObject que representa este directorio.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("nombre", this.getNombre());
        json.put("tipo", "DIRECTORIO");

        // Convertir cada elemento del contenido a JSON
        JSONArray contenidoJson = new JSONArray();
        for (int i = 0; i < this.contenido.getTamano(); i++) {
            EntradaSistemaArchivos entrada = this.contenido.obtener(i);
            if (entrada instanceof Directorio) {
                contenidoJson.put(((Directorio) entrada).toJson());
            } else if (entrada instanceof Archivo) {
                contenidoJson.put(((Archivo) entrada).toJson());
            }
        }
        json.put("contenido", contenidoJson);
        
        return json;
    }

}

