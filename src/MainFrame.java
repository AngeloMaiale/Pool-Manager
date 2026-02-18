import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;

    public MainFrame() {
        setTitle("Simulador de Conexiones - ADELANTO");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Panel Superior (Controles) ---
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout());

        btnTestConn = new JButton("Verificar Conexión DB");
        btnTestConn.setBackground(new Color(200, 255, 200)); // Color verde suave

        btnRaw = new JButton("Simulación RAW");
        btnRaw.setEnabled(false); // Deshabilitado para el adelanto

        btnPooled = new JButton("Simulación POOLED");
        btnPooled.setEnabled(false); // Deshabilitado para el adelanto

        btnStop = new JButton("Detener");
        btnStop.setBackground(Color.RED);
        btnStop.setForeground(Color.WHITE);
        btnStop.setEnabled(false);

        panelButtons.add(btnTestConn);
        panelButtons.add(btnRaw);
        panelButtons.add(btnPooled);
        panelButtons.add(btnStop);

        // --- Área Central (Logs) ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Log de Ejecución en Tiempo Real"));

        add(panelButtons, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // --- Eventos ---
        btnTestConn.addActionListener(e -> testSingleConnection());

        LogManager.log("Interfaz iniciada. Esperando comandos...");
        appendToGui("Sistema listo. Cargado desde: " + DatabaseConfig.getUrl());
    }

    // Método simple para demostrar que JDBC funciona
    private void testSingleConnection() {
        new Thread(() -> {
            appendToGui("Intentando conectar a PostgreSQL...");
            try (Connection conn = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPass())) {

                if (conn != null) {
                    appendToGui("¡ÉXITO! Conexión establecida.");
                    LogManager.log("Prueba de conexión exitosa.");
                    JOptionPane.showMessageDialog(this, "Conexión Exitosa a la DB");
                }
            } catch (SQLException ex) {
                appendToGui("ERROR: " + ex.getMessage());
                LogManager.log("Fallo en prueba de conexión: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Error de conexión", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void appendToGui(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
    }

    public static void main(String[] args) {
        // Asegurarse de cargar la UI en el hilo de eventos
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}