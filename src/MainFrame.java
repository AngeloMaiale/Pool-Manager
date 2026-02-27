import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.Insets;


public class MainFrame extends JFrame {
    private JTextArea logArea;
    private JButton btnTestConn, btnRaw, btnPooled, btnStop;
    private SimulationEngine engine;

    public MainFrame() {
        setTitle("Simulador de Conexiones a BD - Entrega Final");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        engine = new SimulationEngine(this);

        Font fuenteConsola = new Font("Monospaced", Font.BOLD, 16);
        Font fuenteBotones = new Font("Arial", Font.BOLD, 14);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(fuenteConsola);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(0, 255, 100));

        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelButtons = new JPanel();
        panelButtons.setBackground(new Color(240, 240, 240));
        panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnTestConn = new JButton("Probar Conexión");
        btnRaw = new JButton("Simulación RAW");
        btnPooled = new JButton("Simulación POOLED");
        btnStop = new JButton("STOP");


        configurarBoton(btnTestConn, fuenteBotones, new Color(220, 220, 220));
        configurarBoton(btnRaw, fuenteBotones, new Color(255, 200, 200));
        configurarBoton(btnPooled, fuenteBotones, new Color(200, 255, 200));
        configurarBoton(btnStop, fuenteBotones, new Color(255, 100, 100));
        btnStop.setForeground(Color.WHITE);

        panelButtons.add(btnTestConn);
        panelButtons.add(btnRaw);
        panelButtons.add(btnPooled);
        panelButtons.add(btnStop);

        add(panelButtons, BorderLayout.SOUTH);

        btnTestConn.addActionListener(e -> {
            appendToGui("Verificando conexión con PostgreSQL...");
        });

        btnRaw.addActionListener(e -> {
            enableButtons(false);
            new Thread(() -> engine.runRawSimulation()).start();
        });

        btnPooled.addActionListener(e -> {
            enableButtons(false);
            new Thread(() -> engine.runPooledSimulation()).start();
        });

        btnStop.addActionListener(e -> engine.stopSimulation());
    }

    private void configurarBoton(JButton btn, Font f, Color c) {
        btn.setFont(f);
        btn.setBackground(c);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void appendToGui(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void enableButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            btnRaw.setEnabled(enabled);
            btnPooled.setEnabled(enabled);
            btnTestConn.setEnabled(enabled);
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) {}

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}