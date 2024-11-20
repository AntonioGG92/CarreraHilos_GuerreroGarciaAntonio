import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

public class Interfaz extends JFrame {
    private JLabel[] raptors; // Representa los raptors en la carrera
    private JButton botonIniciar; // Botón para iniciar la carrera
    private JButton botonReiniciar; // Botón para reiniciar la carrera
    private int numRaptors; // Número de raptors
    private int distancia; // Distancia de la carrera
    private Semaphore semaforo; // Semáforo para sincronizar los raptors

    public Interfaz() {
        configurarParametros();

        setTitle("Carrera de Raptors");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // === SEMÁFORO ===
        semaforo = new Semaphore(1);

        // === CREAR JLayeredPane PARA CONTROLAR CAPAS ===
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 800, 400);
        add(layeredPane);

        // === CARGAR IMAGEN DE FONDO ===
        ImageIcon fondoImagen = new ImageIcon(getClass().getResource("/Resources/fondo.png"));
        JLabel fondo = new JLabel(fondoImagen);
        fondo.setBounds(0, 0, 800, 400);
        layeredPane.add(fondo, Integer.valueOf(0));

        // === CREAR RAPTORS ===
        raptors = new JLabel[numRaptors];
        for (int i = 0; i < numRaptors; i++) {
            ImageIcon raptorImagenOriginal = new ImageIcon(getClass().getResource("/Resources/velo" + (i + 1) + ".png"));
            Image imagenEscalada = raptorImagenOriginal.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon raptorImagenEscalada = new ImageIcon(imagenEscalada);

            raptors[i] = new JLabel(raptorImagenEscalada);
            raptors[i].setBounds(30, 50 + i * 80, 80, 80);
            layeredPane.add(raptors[i], Integer.valueOf(1));
        }

        // === BOTÓN PARA INICIAR LA CARRERA ===
        botonIniciar = new JButton("Iniciar Carrera");
        botonIniciar.setBounds(250, 10, 120, 30);
        botonIniciar.addActionListener(e -> iniciarCarrera());
        layeredPane.add(botonIniciar, Integer.valueOf(2));

        // === BOTÓN PARA REINICIAR LA CARRERA ===
        botonReiniciar = new JButton("Reiniciar Carrera");
        botonReiniciar.setBounds(400, 10, 150, 30);
        botonReiniciar.setEnabled(false); // Inicialmente deshabilitado
        botonReiniciar.addActionListener(e -> reiniciarCarrera(layeredPane));
        layeredPane.add(botonReiniciar, Integer.valueOf(2));
    }

    private void configurarParametros() {
        String inputRaptors = JOptionPane.showInputDialog(null, "Ingrese el número de raptors (mínimo 2, máximo 4):", "Configuración", JOptionPane.QUESTION_MESSAGE);
        numRaptors = Math.max(2, Math.min(4, Integer.parseInt(inputRaptors)));

        String inputDistancia = JOptionPane.showInputDialog(null, "Ingrese la distancia de la carrera (mínimo 50, máximo 500):", "Configuración", JOptionPane.QUESTION_MESSAGE);
        distancia = Math.max(50, Math.min(500, Integer.parseInt(inputDistancia)));
    }

    private void iniciarCarrera() {
        botonIniciar.setEnabled(false);
        botonReiniciar.setEnabled(false);

        for (int i = 0; i < numRaptors; i++) {
            int raptorId = i;
            new Thread(() -> moverRaptor(raptorId)).start();
        }
    }

    private void reiniciarCarrera(JLayeredPane layeredPane) {
        // Reiniciar posiciones de los raptors
        for (int i = 0; i < numRaptors; i++) {
            raptors[i].setLocation(30, 50 + i * 80);
        }

        botonIniciar.setEnabled(true);
        botonReiniciar.setEnabled(false); // Deshabilitar botón de reinicio hasta que se use el de iniciar
    }

    private void moverRaptor(int raptorId) {
        try {
            int progreso = 0;

            while (progreso < distancia) {
                Thread.sleep(100);
                progreso += (int) (Math.random() * 10);

                semaforo.acquire();
                try {
                    int x = 30 + progreso * (700 / distancia);
                    raptors[raptorId].setLocation(x, raptors[raptorId].getY());
                } finally {
                    semaforo.release();
                }
            }

            System.out.println("El raptor " + (raptorId + 1) + " ha terminado la carrera.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Habilitar el botón de reinicio al final de la carrera
        SwingUtilities.invokeLater(() -> botonReiniciar.setEnabled(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Interfaz().setVisible(true));
    }
}
