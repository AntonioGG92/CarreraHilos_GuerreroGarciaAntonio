import java.util.concurrent.Semaphore;

public class Carrera {
    private final int numCoches;
    private final int distancia;
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
                int velocidad = (int) (Math.random() * 10 + 1); // Velocidad aleatoria entre 1 y 10

                while (progreso < distancia) {
                    try {
                        Thread.sleep(200); // Simula el tiempo de movimiento
                        progreso += velocidad;

                        // Notificar a la interfaz gráfica sobre el progreso
                        if (listener != null) {
                            listener.onProgresoActualizado(id, Math.min(progreso, distancia));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Notificar al final de la carrera
                if (listener != null) {
                    listener.onCocheTerminado(id);
                }
            });

            hilosCoches[i].start();
        }

        // Esperar a que todos terminen
        new Thread(() -> {
            for (Thread hilo : hilosCoches) {
                try {
                    hilo.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Anunciar el final de la carrera
            if (listener != null) {
                listener.onCarreraTerminada();
            }
        }).start();
    }

    public interface CarreraListener {
        void onProgresoActualizado(int cocheId, int progreso);
        void onCocheTerminado(int cocheId);
        void onCarreraTerminada();
    }
}
