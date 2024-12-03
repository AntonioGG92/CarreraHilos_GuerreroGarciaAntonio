import java.util.concurrent.Semaphore;

public class Carrera {
    private final int numCoches; // Número de raptors 
    private final int distancia; // Distancia de la carrera
    private CarreraListener listener; 
    
    public Carrera(int numCoches, int distancia) {
        this.numCoches = numCoches;
        this.distancia = distancia;
    }

    public void setListener(CarreraListener listener) {
        this.listener = listener;
    }

    public void iniciarCarrera() {
        
        Thread[] hilosCoches = new Thread[numCoches];

        for (int i = 0; i < numCoches; i++) {
            final int id = i;
            hilosCoches[i] = new Thread(() -> {
                int progreso = 0;
                int velocidad = (int) (Math.random() * 10 + 1); 

                // Bucle para hacer avanzar al raptor hasta llegar a la meta
                while (progreso < distancia) {
                    try {
                        Thread.sleep(200); // Simula el tiempo que pasa mientras el raptor avanza
                        progreso += velocidad; 

                        if (listener != null) {
                            listener.onProgresoActualizado(id, Math.min(progreso, distancia));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

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
                    hilo.join(); 
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
        void onProgresoActualizado(int cocheId, int progreso); 
        void onCocheTerminado(int cocheId); 
        void onCarreraTerminada();
    }
}
