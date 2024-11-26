import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.*;
import java.net.URL;
import java.io.IOException;

public class Interfaz extends JFrame {
    private JLabel[] raptors; 
    private JLabel[] etiquetasProgreso; 
    private JButton botonIniciar;
    private JButton botonReiniciar;
    private JTextField campoDistancia;
    private final int numRaptors = 4;
    private int distancia; 
    private Semaphore semaforo;
    private List<Integer> posiciones; 
    private Clip musicaFondo;

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

        // === LOGO EN EL CENTRO DEL FONDO ===
        ImageIcon logoImagen = new ImageIcon(getClass().getResource("/Resources/logoDinoRace.png"));
        JLabel logo = new JLabel(logoImagen);
        logo.setBounds(300, 0, 250, 150); // Aumentar la altura del logo para asegurarse de que se vea completo
        panelCapas.add(logo, Integer.valueOf(1)); // Añadir el logo en la capa superior del fondo

        // === CREAR RAPTORS ===
        raptors = new JLabel[numRaptors];
        etiquetasProgreso = new JLabel[numRaptors];
        int espacioEntreLineas = 90; // Espacio entre las líneas de los raptors
        int ajusteVertical = 60; // Ajuste vertical para que los raptors empiecen más abajo y el logo tenga espacio

        for (int i = 0; i < numRaptors; i++) {
            ImageIcon raptorImagenOriginal = new ImageIcon(getClass().getResource("/Resources/velo" + (i + 1) + ".png"));
            Image imagenEscalada = raptorImagenOriginal.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon raptorImagenEscalada = new ImageIcon(imagenEscalada);

            // Ajustar posición vertical de cada raptor
            raptors[i] = new JLabel(raptorImagenEscalada);
            raptors[i].setBounds(50, (espacioEntreLineas * i) + ajusteVertical, 80, 80);
            panelCapas.add(raptors[i], Integer.valueOf(2));

            // Inicializar etiquetas de progreso (invisibles al inicio)
            etiquetasProgreso[i] = new JLabel("0%");
            etiquetasProgreso[i].setBounds(140, (espacioEntreLineas * i) + ajusteVertical + 30, 50, 20);
            etiquetasProgreso[i].setForeground(Color.YELLOW);
            etiquetasProgreso[i].setFont(new Font("Arial", Font.BOLD, 16)); // Aumentar el tamaño de la fuente para mejor visibilidad
            etiquetasProgreso[i].setVisible(false); // Invisibles hasta que comience la carrera
            panelCapas.add(etiquetasProgreso[i], Integer.valueOf(3));
        }

        // === PANEL INFERIOR PARA FONDO DE BOTONES Y CAMPO DE DISTANCIA ===
        JPanel panelInferior = new JPanel();
        panelInferior.setBounds(0, 400, 870, 120);
        panelInferior.setBackground(new Color(34, 49, 63)); // Color oscuro que pega mejor con la temática de la jungla
        panelInferior.setLayout(null);
        add(panelInferior);

        // === CAMPO DE TEXTO PARA DISTANCIA ===
        JLabel etiquetaDistancia = new JLabel("Distancia (50 - 500):");
        etiquetaDistancia.setBounds(10, 10, 150, 30);
        etiquetaDistancia.setForeground(Color.YELLOW); // Cambiar color del texto a amarillo
        panelInferior.add(etiquetaDistancia);

        campoDistancia = new JTextField("100"); // Valor predeterminado de distancia
        campoDistancia.setBounds(160, 10, 60, 30);
        panelInferior.add(campoDistancia);

        // === BOTÓN PARA INICIAR LA CARRERA ===
        botonIniciar = new JButton("Iniciar Carrera");
        botonIniciar.setBounds(300, 10, 120, 30);
        botonIniciar.setBackground(new Color(85, 107, 47)); // Un color verde que pega mejor con la temática de jungla
        botonIniciar.setForeground(Color.WHITE);
        botonIniciar.setFocusPainted(false);
        botonIniciar.addActionListener(e -> iniciarCarrera());
        panelInferior.add(botonIniciar);

        // === BOTÓN PARA REINICIAR LA CARRERA ===
        botonReiniciar = new JButton("Reiniciar Carrera");
        botonReiniciar.setBounds(450, 10, 150, 30);
        botonReiniciar.setBackground(new Color(139, 0, 0)); // Un color rojo oscuro que pega mejor con la temática de jungla
        botonReiniciar.setForeground(Color.WHITE);
        botonReiniciar.setFocusPainted(false);
        botonReiniciar.setEnabled(false); // Inicialmente deshabilitado
        botonReiniciar.addActionListener(e -> reiniciarCarrera(panelCapas));
        panelInferior.add(botonReiniciar);

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
        int ajusteVertical = 60;

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

            // Bajar el volumen de la música de fondo
            FloatControl controlVolumen = (FloatControl) musicaFondo.getControl(FloatControl.Type.MASTER_GAIN);
            controlVolumen.setValue(-20.0f); // Reducir el volumen

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
