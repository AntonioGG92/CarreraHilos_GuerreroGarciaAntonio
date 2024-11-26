import java.util.concurrent.Semaphore;

public class Carrera {
    private final int numCoches; // Número de raptors que participan en la carrera
    private final int distancia; // Distancia de la carrera
    private CarreraListener listener; // Interfaz que recibe eventos durante la carrera

    // Constructor para inicializar el número de raptors y la distancia
    public Carrera(int numCoches, int distancia) {
        this.numCoches = numCoches;
        this.distancia = distancia;
    }

    // Setter para registrar un listener que reciba actualizaciones del progreso
    public void setListener(CarreraListener listener) {
        this.listener = listener;
    }

    // Método para iniciar la carrera
    public void iniciarCarrera() {
        // Creamos un arreglo de hilos para cada raptor
        Thread[] hilosCoches = new Thread[numCoches];

        // Inicializamos e iniciamos cada hilo de la carrera
        for (int i = 0; i < numCoches; i++) {
            final int id = i;
            hilosCoches[i] = new Thread(() -> {
                int progreso = 0;
                int velocidad = (int) (Math.random() * 10 + 1); // Velocidad aleatoria entre 1 y 10

                // Bucle para hacer avanzar al raptor hasta llegar a la meta
                while (progreso < distancia) {
                    try {
                        Thread.sleep(200); // Simula el tiempo que pasa mientras el raptor avanza
                        progreso += velocidad; // Aumenta la posición según la velocidad

                        // Notificamos a la interfaz gráfica sobre el progreso del raptor
                        if (listener != null) {
                            listener.onProgresoActualizado(id, Math.min(progreso, distancia));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace(); // En caso de error, imprimimos la traza
                    }
                }

                // Notificamos que el raptor ha llegado al final de la carrera
                if (listener != null) {
                    listener.onCocheTerminado(id);
                }
            });

            // Iniciamos el hilo del raptor
            hilosCoches[i].start();
        }

        // Creamos un hilo extra que espere a que todos los raptors terminen la carrera
        new Thread(() -> {
            for (Thread hilo : hilosCoches) {
                try {
                    hilo.join(); // Espera a que el hilo del raptor termine
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Una vez que todos han terminado, notificamos que la carrera ha concluido
            if (listener != null) {
                listener.onCarreraTerminada();
            }
        }).start();
    }

    // Interfaz para poder manejar los eventos de la carrera desde otra clase
    public interface CarreraListener {
        void onProgresoActualizado(int cocheId, int progreso); // Evento para actualizar el progreso de un raptor
        void onCocheTerminado(int cocheId); // Evento para indicar que un raptor ha terminado la carrera
        void onCarreraTerminada(); // Evento para indicar que la carrera ha finalizado
    }
}
