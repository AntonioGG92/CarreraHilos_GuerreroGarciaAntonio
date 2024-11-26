import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.*;
import java.net.URL;
import java.io.IOException;

public class Interfaz extends JFrame {
    private JLabel[] raptors; // Representa a los raptors en la carrera
    private JLabel[] etiquetasProgreso; // Etiquetas para mostrar el progreso de cada raptor
    private JButton botonIniciar; // Botón para iniciar la carrera
    private JButton botonReiniciar; // Botón para reiniciar la carrera
    private JTextField campoDistancia; // Campo de texto para ingresar la distancia de la carrera
    private final int numRaptors = 4; // Número fijo de raptors
    private int distancia; // Distancia de la carrera
    private Semaphore semaforo; // Semáforo para sincronizar los raptors
    private List<Integer> posiciones; // Lista para almacenar el orden de llegada de los raptors
    private Clip musicaFondo; // Clip para reproducir la música de fondo

    public Interfaz() {
        setTitle("Carrera de Raptors");
        setSize(870, 520); // Ajuste del tamaño para visibilidad de FINISH
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // === SEMÁFORO ===
        semaforo = new Semaphore(1);

        // === CREAR JLayeredPane PARA CONTROLAR CAPAS ===
        JLayeredPane panelCapas = new JLayeredPane(); // Panel con capas para organizar los elementos
        panelCapas.setBounds(0, 0, 850, 400);
        add(panelCapas);

        // === CARGAR IMAGEN DE FONDO ===
        ImageIcon fondoImagen = new ImageIcon(getClass().getResource("/Resources/fondo.png"));
        JLabel fondo = new JLabel(fondoImagen);
        fondo.setBounds(0, 0, 850, 400);
        panelCapas.add(fondo, Integer.valueOf(0));

        // === CREAR RAPTORS ===
        raptors = new JLabel[numRaptors];
        etiquetasProgreso = new JLabel[numRaptors];
        int espacioEntreLineas = 90; // Espacio entre las líneas de los raptors
        int ajusteVertical = 10; // Ajuste vertical para centrar en la línea adecuadamente

        for (int i = 0; i < numRaptors; i++) {
            ImageIcon raptorImagenOriginal = new ImageIcon(getClass().getResource("/Resources/velo" + (i + 1) + ".png"));
            Image imagenEscalada = raptorImagenOriginal.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon raptorImagenEscalada = new ImageIcon(imagenEscalada);

            // Ajustar posición vertical de cada raptor
            raptors[i] = new JLabel(raptorImagenEscalada);
            raptors[i].setBounds(50, (espacioEntreLineas * i) + ajusteVertical, 80, 80);
            panelCapas.add(raptors[i], Integer.valueOf(1));

            // Inicializar etiquetas de progreso (invisibles al inicio)
            etiquetasProgreso[i] = new JLabel("0%");
            etiquetasProgreso[i].setBounds(140, (espacioEntreLineas * i) + ajusteVertical + 30, 50, 20);
            etiquetasProgreso[i].setForeground(Color.YELLOW);
            etiquetasProgreso[i].setFont(new Font("Arial", Font.BOLD, 16)); // Aumentar el tamaño de la fuente para mejor visibilidad
            etiquetasProgreso[i].setVisible(false); // Invisibles hasta que comience la carrera
            panelCapas.add(etiquetasProgreso[i], Integer.valueOf(2));
        }

        // === CAMPO DE TEXTO PARA DISTANCIA ===
        JLabel etiquetaDistancia = new JLabel("Distancia:");
        etiquetaDistancia.setBounds(10, 420, 70, 30);
        add(etiquetaDistancia);

        campoDistancia = new JTextField("100"); // Valor predeterminado de distancia
        campoDistancia.setBounds(80, 420, 60, 30);
        add(campoDistancia);

        // === BOTÓN PARA INICIAR LA CARRERA ===
        botonIniciar = new JButton("Iniciar Carrera");
        botonIniciar.setBounds(300, 420, 120, 30);
        botonIniciar.setBackground(new Color(34, 139, 34)); // Color verde temático
        botonIniciar.setForeground(Color.WHITE);
        botonIniciar.setFocusPainted(false);
        botonIniciar.addActionListener(e -> iniciarCarrera());
        add(botonIniciar);

        // === BOTÓN PARA REINICIAR LA CARRERA ===
        botonReiniciar = new JButton("Reiniciar Carrera");
        botonReiniciar.setBounds(450, 420, 150, 30);
        botonReiniciar.setBackground(new Color(205, 92, 92)); // Color rojo temático
        botonReiniciar.setForeground(Color.WHITE);
        botonReiniciar.setFocusPainted(false);
        botonReiniciar.setEnabled(false); // Inicialmente deshabilitado
        botonReiniciar.addActionListener(e -> reiniciarCarrera(panelCapas));
        add(botonReiniciar);

        // === INICIAR MÚSICA DE FONDO ===
        reproducirMusicaFondo();
    }

    // Método para iniciar la carrera
    private void iniciarCarrera() {
        try {
            // Leer distancia desde el campo de texto
            distancia = Integer.parseInt(campoDistancia.getText());
            if (distancia < 50 || distancia > 500) {
                throw new IllegalArgumentException("La distancia debe estar entre 50 y 500.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un valor numérico válido para la distancia.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        botonIniciar.setEnabled(false);
        botonReiniciar.setEnabled(false);

        // Inicializar la lista de posiciones
        posiciones = new ArrayList<>();

        for (int i = 0; i < numRaptors; i++) {
            etiquetasProgreso[i].setVisible(true); // Hacer visible la etiqueta de progreso cuando el raptor comienza
            int raptorId = i;
            new Thread(() -> moverRaptor(raptorId)).start();
        }
    }

    // Método para reiniciar la carrera
    private void reiniciarCarrera(JLayeredPane panelCapas) {
        // Reiniciar posiciones de los raptors y etiquetas de progreso
        int espacioEntreLineas = 90;
        int ajusteVertical = 10;

        for (int i = 0; i < numRaptors; i++) {
            raptors[i].setLocation(50, (espacioEntreLineas * i) + ajusteVertical);
            etiquetasProgreso[i].setText("0%");
            etiquetasProgreso[i].setVisible(false); // Ocultar las etiquetas de progreso al reiniciar
        }

        botonIniciar.setEnabled(true);
        botonReiniciar.setEnabled(false); // Deshabilitar botón de reinicio hasta que se use el de iniciar
    }

    // Método para mover cada raptor en la carrera
    private void moverRaptor(int raptorId) {
        try {
            int progreso = 0;
            int anchoTotalPista = 750; // Ajustar el ancho para que los raptors lleguen al final correctamente

            while (progreso < distancia) {
                Thread.sleep(100);
                progreso += (int) (Math.random() * 10);

                semaforo.acquire();
                try {
                    int x = 50 + (progreso * (anchoTotalPista - 50) / distancia);
                    x = Math.min(x, anchoTotalPista); // Asegurarse de que no pase del final de la pista
                    raptors[raptorId].setLocation(x, raptors[raptorId].getY());

                    // Actualizar la etiqueta de progreso y moverla junto al raptor
                    int progresoPorcentaje = (progreso * 100) / distancia;
                    etiquetasProgreso[raptorId].setText(progresoPorcentaje + "%");
                    etiquetasProgreso[raptorId].setLocation(x + 90, raptors[raptorId].getY() + 30); // Mover la etiqueta junto al raptor
                } finally {
                    semaforo.release();
                }
            }

            System.out.println("El raptor " + (raptorId + 1) + " ha terminado la carrera.");
            synchronized (posiciones) {
                posiciones.add(raptorId + 1);
                if (posiciones.size() == numRaptors) {
                    mostrarResultados();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Habilitar el botón de reinicio al final de la carrera
        SwingUtilities.invokeLater(() -> botonReiniciar.setEnabled(true));
    }

    // Método para mostrar los resultados de la carrera
    private void mostrarResultados() {
        StringBuilder resultados = new StringBuilder("¡Ganador! --> Raptor " + posiciones.get(0) + "\n\nPosiciones finales:\n");
        for (int i = 0; i < posiciones.size(); i++) {
            resultados.append((i + 1)).append(". Raptor ").append(posiciones.get(i)).append("\n");
        }
        JOptionPane.showMessageDialog(this, resultados.toString(), "Resultados", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para reproducir música de fondo
    private void reproducirMusicaFondo() {
        try {
            URL musicaURL = getClass().getResource("/Resources/musicaFondo.wav"); // Obtener la URL del archivo desde el classpath
            if (musicaURL == null) {
                throw new IOException("El archivo de música no se encuentra.");
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaURL);
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioStream);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Reproducir en bucle
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("No se pudo reproducir la música de fondo: " + e.getMessage());
        }
    }

    // Método principal para lanzar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Interfaz().setVisible(true));
    }
}
