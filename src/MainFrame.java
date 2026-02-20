import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;
    private SimulationEngine engine;

    public MainFrame() {
        setTitle("Simulador de Conexiones a BD - Adelanto");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        engine = new SimulationEngine(this);

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout());

        btnTestConn = new JButton("Verificar DB");
        btnTestConn.setBackground(new Color(200, 255, 200));

        btnRaw = new JButton("Simulación RAW");

        btnPooled = new JButton("Simulación POOLED");
        btnPooled.setEnabled(false);

        btnStop = new JButton("Freno de Emergencia");
        btnStop.setBackground(Color.RED);
        btnStop.setForeground(Color.WHITE);
        btnStop.setEnabled(false);

        panelButtons.add(btnTestConn);
        panelButtons.add(btnRaw);
        panelButtons.add(btnPooled);
        panelButtons.add(btnStop);
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Log de Ejecución en Tiempo Real"));

        add(panelButtons, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        btnTestConn.addActionListener(e -> testSingleConnection());

        btnRaw.addActionListener(e -> {
            enableButtons(false);
            engine.runRawSimulation();
        });

        btnStop.addActionListener(e -> engine.stopSimulation());

        appendToGui("Sistema inicializado. Parámetros cargados desde config.properties");
    }

    private void testSingleConnection() {
        new Thread(() -> {
            appendToGui("Probando conexión a: " + DatabaseConfig.getUrl() + " ...");
            try (Connection conn = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPass())) {

                if (conn != null) {
                    appendToGui("¡ÉXITO! Conexión establecida correctamente.");
                    JOptionPane.showMessageDialog(this, "Conexión Exitosa a la DB");
                }
            } catch (SQLException ex) {
                appendToGui("ERROR de conexión: " + ex.getMessage());
            }
        }).start();
    }

    public void enableButtons(boolean enable) {
        btnTestConn.setEnabled(enable);
        btnRaw.setEnabled(enable);
        btnStop.setEnabled(!enable);
    }

    public void appendToGui(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}