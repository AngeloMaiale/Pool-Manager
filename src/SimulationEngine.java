import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulationEngine {
    private MainFrame gui;
    private volatile boolean stopRequested = false;

    public SimulationEngine(MainFrame gui) {
        this.gui = gui;
    }

    public void stopSimulation() {
        this.stopRequested = true;
        gui.appendToGui("¡FRENO ACTIVADO! Deteniendo hilos...");
        LogManager.log("SIMULACIÓN ABORTADA POR EL USUARIO.");
    }

    public void runRawSimulation() {
        stopRequested = false;
        int samples = DatabaseConfig.getSamples();
        int maxRetries = DatabaseConfig.getRetries();

        gui.appendToGui("Iniciando Simulación RAW con " + samples + " hilos concurrentes...");
        LogManager.log("--- INICIO SIMULACIÓN RAW ---");

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(samples);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger totalRetries = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= samples; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startSignal.await(); // Esperar la señal de salida
                    if (stopRequested) return;

                    ejecutarMuestraRaw(id, maxRetries, successCount, failCount, totalRetries);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown();
                }
            }).start();
        }

        startSignal.countDown();

        new Thread(() -> {
            try {
                doneSignal.await();
                long duration = System.currentTimeMillis() - startTime;

                double avgRetries = totalRetries.get() / (double) samples;
                String reporte = String.format("FIN RAW | Tiempo: %d ms | Éxitos: %d | Fallos: %d | Promedio Reintentos: %.2f",
                        duration, successCount.get(), failCount.get(), avgRetries);

                gui.appendToGui("--------------------------------------------------");
                gui.appendToGui(reporte);
                gui.appendToGui("--------------------------------------------------");
                LogManager.log(reporte);

                gui.enableButtons(true);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void ejecutarMuestraRaw(int id, int maxRetries, AtomicInteger successCount, AtomicInteger failCount, AtomicInteger totalRetries) {
        int attempt = 0;
        boolean success = false;
        String status = "FALLIDA";

        while (attempt <= maxRetries && !success && !stopRequested) {
            try (Connection conn = DriverManager.getConnection(
                    DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPass());
                 Statement stmt = conn.createStatement()) {

                stmt.execute(DatabaseConfig.getQuery());
                success = true;
                status = "EXITOSA";

            } catch (Exception e) {
                attempt++;
                totalRetries.incrementAndGet();
                try { Thread.sleep(50); } catch (InterruptedException ex) {}
            }
        }

        if (success) {
            successCount.incrementAndGet();
        } else {
            failCount.incrementAndGet();
        }

        LogManager.log(String.format("Muestra ID: %03d | Estado: %s | Reintentos: %d", id, status, attempt));
    }
}
