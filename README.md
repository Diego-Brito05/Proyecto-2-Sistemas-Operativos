Proyecto 2: Simulador Virtual de Sistema de Archivos con Gestión de
Permisos y Asignación de Bloques
Planteamiento del Problema
El objetivo de este proyecto es que los estudiantes desarrollen un simulador de sistema de
archivos avanzado, en el que puedan comprender y aplicar conceptos fundamentales como la
gestión de archivos y directorios, la asignación de bloques de almacenamiento, la administración
de permisos, la fragmentación del espacio en disco, y la administración de operaciones de
entrada/salida mediante procesos de usuario.
Para ello, los estudiantes deberán implementar un sistema de archivos simulado en Java
utilizando NetBeans, con una interfaz gráfica que represente visualmente la estructura jerárquica
de directorios y archivos mediante un JTree, así como la distribución de bloques en una
Simulación de un Disco (SD), una tabla de asignación de archivos, y un sistema de gestión de
procesos que realicen las operaciones de E/S.
El sistema debe operar en dos modos de usuario: modo administrador y modo usuario. En
el modo administrador se permite realizar todas las operaciones, incluyendo crear, modificar y
eliminar archivos y directorios, gestionar todos los procesos del sistema, cambiar las políticas de
planificación del disco, y visualizar información completa del SD y las estadísticas del sistema.
Por otro lado, el modo usuario restringe las acciones a solo lectura de archivos propios o públicos

y la creación de procesos para realizar operaciones de E/S sobre sus propios archivos, impidiendo
modificar archivos del sistema o acceder a información de otros usuarios.
Los archivos creados deberán tener un tamaño en bloques, los cuales serán asignados
utilizando el método de asignación encadenada, en donde cada archivo se representa como una
lista enlazada de bloques en el SD. Sin embargo, la asignación de estos bloques no se realizará de
manera directa, sino que será gestionada por procesos de usuario que soliciten operaciones de E/S
al sistema. Cada vez que un proceso necesite crear, leer, actualizar o eliminar un archivo, generará
una solicitud de E/S que será procesada por el sistema de archivos según la política de
planificación de disco activa.
El sistema deberá mantener una cola de procesos, donde cada proceso tendrá un estado
(nuevo, listo, ejecutando, bloqueado o terminado) y estará asociado a una operación específica
sobre el sistema de archivos. Cuando un proceso realiza una solicitud CRUD, esta solicitud entra
en la cola de E/S del disco, donde el planificador determinará el orden en que serán atendidas las
solicitudes según la política configurada (FIFO, SSTF, SCAN, C-SCAN, entre otras).
Opcionalmente, el sistema puede implementar un mecanismo de almacenamiento
intermedio (buffering) para gestionar eficientemente las operaciones de E/S. El buffer actuará
como una memoria caché que almacena temporalmente datos en tránsito entre los procesos y el
disco, permitiendo reducir el número de accesos directos al disco y mejorar el rendimiento general
del sistema. El buffer deberá emplear una política de reemplazo como FIFO, LRU o LFU para
gestionar su contenido cuando esté lleno.
La interfaz gráfica debe mostrar en tiempo real el estado del sistema: la estructura de
directorios y archivos en el JTree, la visualización del disco con los bloques ocupados y libres
(indicando qué proceso o archivo ocupa cada bloque), la tabla de asignación de archivos
mostrando para cada archivo su nombre, cantidad de bloques, dirección del primer bloque y el
proceso que lo creó, y una vista de la cola de procesos con sus estados actuales y las operaciones
que están solicitando.
Además, el sistema deberá actualizarse en tiempo real cada vez que se realice una
operación CRUD (Crear, Leer, Actualizar, Eliminar), reflejando los cambios en la estructura de
directorios, en el estado del disco SD, en la tabla de asignación de archivos, en el estado de los
procesos y en las métricas del sistema.

Requerimientos Funcionales
1. Visualización de la estructura del sistema de archivos:
○ Implementar un JTree para representar la estructura jerárquica de directorios y
archivos.
○ Referencia de aprendizaje: Pueden echarle un ojo a este video que describe
cómo utilizar este componente
https://www.youtube.com/watch?v=SfljfVnLbc4
○ Mostrar información del archivo o directorio seleccionado (nombre, tamaño).
2. Simulación del SD y asignación de bloques:
○ Representar visualmente el SD como un conjunto de bloques, indicando cuáles
están ocupados y cuáles están libres.
○ Simular la asignación encadenada, donde cada archivo se almacena como una
lista enlazada de bloques.
○ Manejar la liberación de bloques cuando se eliminan archivos.
○ Definir un tamaño limitado de almacenamiento, evitando la creación de
archivos si no hay espacio disponible. (Una cantidad máxima de bloques
razonable)

3. Gestión de archivos y directorios (CRUD):
○ Crear:
■ Los administradores podrán crear archivos y directorios.
■ Se especificará el tamaño del archivo en bloques, los cuales serán
asignados en el SD.

○ Leer:
■ Todos los usuarios podrán visualizar la estructura del sistema y sus
propiedades.
○ Actualizar:
■ Solo los administradores podrán modificar el nombre
○ Eliminar:
■ Al borrar un archivo, se liberarán los bloques asignados en el SD.
■ Al eliminar un directorio, también se deben eliminar todos sus archivos
y subdirectorios.
4. Planificación de disco:
○ El planificador debe determinar el orden en que serán atendidas las solicitudes en
la cola de E/S según la política que se seleccione en la interfaz, por ejemplo: FIFO,
SSTF, SCAN, C-SCAN, entre otras.
○ Se deben configurar al menos cuatro (4) políticas.
5. Modo Administrador vs. Modo Usuario:
○ Implementar dos modos de uso:
■ Administrador: Permite realizar todas las operaciones.
■ Usuario: Restringido a solo lectura.
○ El modo se seleccionará mediante la interfaz.

Java
6. Tabla de asignación de archivos:
○ Implementar un JTable que muestre:
■ El nombre del archivo.
■ La cantidad de bloques asignados.
■ La dirección del primer bloque.
■ Si van a usar colores para representar a los archivos, entonces por
favor incluyan el color correspondiente al archivo

○ La tabla debe actualizarse en tiempo real con cada operación CRUD.
7. Almacenar el estado de los archivos en el sistema:
○ Los estudiantes podrán elegir almacenar la información del sistema de
archivos en un archivo de texto, JSON o cualquier otro formato que les resulte
conveniente para que los datos puedan ser cargados en futuras ejecuciones.

8. (Opcional) Almacenamiento intermedio:
○ Mantener un área de memoria (buffer) para almacenar temporalmente las
solicitudes o bloques de datos más recientes.
○ Implementar una política de reemplazo cuando el buffer esté lleno, por
ejemplo: FIFO, LRU o LFU.
○ Mostrar en la interfaz gráfica el estado del buffer (bloques cargados y espacio
disponible).
○ Permitir que las operaciones de lectura y escritura consulten primero el buffer
antes de acceder al disco.

Consideraciones
● El proyecto puede ser elaborado máximo por 2 personas (3 si alguno queda solo)
● Se permiten proyectos de compañeros de diferentes secciones.
● Solo se permite el uso de librerías para leer el CSV, JSON, SQL etc.
● No se permite uso de librerías para estructuras de datos, como ArrayList, Queue, etc.
● Tener un repositorio en GitHub es obligatorio.
● Para la entrega, junto al código, se debe entregar un informe donde se detalle la
funcionalidad de las clases y métodos más importantes del proyecto, así como las
conclusiones obtenidas a partir del desarrollo y las pruebas de la simulación, no hace
falta documentar todo el código.
● La entrega del trabajo consta de el informe en .PDF y el link de github. Se deberán enviar
ambos a sleon@correo.unimet.edu.ve y mginez@correo.unimet.edu.ve antes de las 7:00
am del viernes de semana 11.
● Se requiere que hagan el proyecto en versiones posteriores a java 21, para poder asegurar
un manejo adecuado del uso de los repositorios tanto entre los miembros del equipo como
en la corrección.
● El viernes semana 11, se realizará la defensa por turnos en el salón habitual. Todos los
integrantes del equipo deben estar presentes (con alguna de sus computadoras).
● Los alumnos que no asistan al interrogatorio, serán calificados en base a 0 (cero).
● Los proyectos sin interfaz gráfica, serán calificados en base a 0 (cero). No se corregirá
código para validar funcionamiento, más si para verificar que los miembros del equipo lo
comprendan.
● Los proyectos sin repositorio en GitHub, serán calificados en base a 0 (cero).
● Los programas que no se ejecuten adecuadamente, serán calificados en base a 0 (cero).
● Los proyectos que no sean realizados en Java (específicamente en el IDE de Netbeans)
serán calificados en base a 0 (cero).
● Es importante que cada uno de los miembros de cada equipo posea un buen conocimiento
general del funcionamiento de cada módulo de la solución.
