import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

public class Interfaz extends JFrame {
    private JLabel[] coches; // Representa los coches en la carretera
    private JButton botonIniciar; // Botón para iniciar la carrera
    private int numCoches; // Número de coches
    private int distancia; // Distancia de la carrera
    private Semaphore semaforo; // Semáforo para sincronizar los coches

    public Interfaz() {
        // Configurar parámetros iniciales usando JOptionPane
        configurarParametros();

        setTitle("Carrera de Coches");
        setSize(800, 400); // Tamaño fijo para la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null); // Usar diseño absoluto para colocar elementos manualmente

        // === SEMÁFORO ===
        semaforo = new Semaphore(1); // Permitir acceso sincronizado a recursos compartidos

        // === CREAR JLayeredPane PARA CONTROLAR CAPAS ===
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 800, 400);
        add(layeredPane);

        // === CARGAR IMAGEN DE FONDO ===
        ImageIcon fondoImagen = new ImageIcon(getClass().getResource("/Resources/fondo.png"));
        JLabel fondo = new JLabel(fondoImagen);
        fondo.setBounds(0, 0, 800, 400);
        layeredPane.add(fondo, Integer.valueOf(0));

        // === CREAR COCHES ===
        coches = new JLabel[numCoches];
        for (int i = 0; i < numCoches; i++) {
        	ImageIcon cocheImagenOriginal = new ImageIcon(getClass().getResource("/Resources/velo" + (i + 1) + ".png"));
            Image imagenEscalada = cocheImagenOriginal.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon cocheImagenEscalada = new ImageIcon(imagenEscalada);

            coches[i] = new JLabel(cocheImagenEscalada);
            coches[i].setBounds(30, 50 + i * 80, 80, 80);
            layeredPane.add(coches[i], Integer.valueOf(1));
        }

        // === BOTÓN PARA INICIAR LA CARRERA ===
        botonIniciar = new JButton("Iniciar Carrera");
        botonIniciar.setBounds(350, 10, 120, 30);
        botonIniciar.addActionListener(e -> iniciarCarrera());
        layeredPane.add(botonIniciar, Integer.valueOf(2));
    }

    private void configurarParametros() {
        // Solicitar número de coches
        String inputCoches = JOptionPane.showInputDialog(null, "Ingrese el número de coches (mínimo 2, máximo 4):", "Configuración", JOptionPane.QUESTION_MESSAGE);
        numCoches = Math.max(2, Math.min(4, Integer.parseInt(inputCoches))); // Validar el rango entre 2 y 4

        // Solicitar distancia de la carrera
        String inputDistancia = JOptionPane.showInputDialog(null, "Ingrese la distancia de la carrera (mínimo 50, máximo 500):", "Configuración", JOptionPane.QUESTION_MESSAGE);
        distancia = Math.max(50, Math.min(500, Integer.parseInt(inputDistancia))); // Validar el rango entre 50 y 500
    }

    private void iniciarCarrera() {
        botonIniciar.setEnabled(false); // Deshabilitar botón mientras la carrera está en progreso

        for (int i = 0; i < numCoches; i++) {
            int cocheId = i;
            new Thread(() -> moverCoche(cocheId)).start(); // Crear un hilo para cada coche
        }
    }

    private void moverCoche(int cocheId) {
        try {
            int progreso = 0;

            while (progreso < distancia) {
                Thread.sleep(100); // Simular tiempo entre movimientos
                progreso += (int) (Math.random() * 10); // Incrementar la posición aleatoriamente

                // Usar semáforo para sincronizar el acceso a la GUI
                semaforo.acquire();
                try {
                    int x = 30 + progreso * (700 / distancia); // Calcular nueva posición
                    coches[cocheId].setLocation(x, coches[cocheId].getY());
                } finally {
                    semaforo.release();
                }
            }

            System.out.println("El coche " + (cocheId + 1) + " ha terminado la carrera.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Interfaz().setVisible(true));
    }
}
