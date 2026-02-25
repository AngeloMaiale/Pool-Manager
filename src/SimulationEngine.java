import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
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

    public void runPooledSimulation() {
        this.stopRequested = false;
        int samples = DatabaseConfig.getSamples();
        int poolSize = DatabaseConfig.getPoolSize();
        long startTime = System.currentTimeMillis();

        gui.appendToGui("Iniciando Simulación POOLED (Muestras: " + samples + ", Pool: " + poolSize + ")...");
        LogManager.log("--- INICIO SIMULACIÓN POOLED ---");

        try {
            SimpleConnectionPool pool = new SimpleConnectionPool(poolSize);
            System.out.println("[DEBUG] Pool inicializado correctamente.");
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(samples);
            System.out.println("[DEBUG] Creando " + samples + " hilos...");
            for (int i = 1; i <= samples; i++) {
                final int id = i;
                new Thread(() -> {
                    try {
                        startSignal.await();

                        if (stopRequested) {
                            doneSignal.countDown();
                            return;
                        }

                        Connection conn = null;
                        try {
                            conn = pool.getConnection();
                            System.out.println("[DEBUG] Hilo " + id + " obtuvo conexión.");

                            Statement stmt = conn.createStatement();
                            stmt.execute(DatabaseConfig.getQuery());

                            LogManager.log("ID: " + id + " | Éxito | POOLED");
                        } catch (Exception e) {
                            System.err.println("[DEBUG] Error en hilo " + id + ": " + e.getMessage());
                            LogManager.log("ID: " + id + " | Fallo: " + e.getMessage() + " | POOLED");
                        } finally {
                            if (conn != null) {
                                pool.releaseConnection(conn);
                                System.out.println("[DEBUG] Hilo " + id + " devolvió conexión.");
                            }
                            doneSignal.countDown();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        doneSignal.countDown();
                    }
                }).start();
            }
            System.out.println("[DEBUG] ¡Dando señal de inicio a todos los hilos!");
            startSignal.countDown();
            new Thread(() -> {
                try {
                    doneSignal.await();
                    long endTime = System.currentTimeMillis();
                    long totalTime = endTime - startTime;
                    System.out.println("[DEBUG] Todos los hilos terminaron.");
                    gui.appendToGui("Simulación POOLED terminada en: " + totalTime + " ms.");
                    gui.enableButtons(true);
                    pool.shutdown();
                } catch (InterruptedException e) {
                    gui.enableButtons(true);
                }
            }).start();

        } catch (SQLException e) {
            System.err.println("[DEBUG] Error al crear el Pool: " + e.getMessage());
            gui.appendToGui("Error: No se pudo conectar a la base de datos.");
            gui.enableButtons(true);
        }
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
                    startSignal.await();
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
