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

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallos = new AtomicInteger(0);

        gui.appendToGui("Iniciando Simulación POOLED (Muestras: " + samples + ", Pool: " + poolSize + ")...");
        LogManager.logHeader("INICIO SIMULACIÓN POOLED");

        try {
            SimpleConnectionPool pool = new SimpleConnectionPool(poolSize);
            CountDownLatch startSignal = new CountDownLatch(1);
            CountDownLatch doneSignal = new CountDownLatch(samples);

            for (int i = 1; i <= samples; i++) {
                final int id = i;
                new Thread(() -> {
                    long inicioMuestra = System.currentTimeMillis();
                    try {
                        startSignal.await();

                        if (stopRequested) {
                            fallos.incrementAndGet();
                            doneSignal.countDown();
                            return;
                        }

                        Connection conn = null;
                        try {
                            conn = pool.getConnection();
                            Statement stmt = conn.createStatement();
                            stmt.execute(DatabaseConfig.getQuery());

                            long tiempoRespuesta = System.currentTimeMillis() - inicioMuestra;
                            exitos.incrementAndGet();
                            LogManager.log(id, "POOLED", "EXITO", tiempoRespuesta);

                        } catch (Exception e) {
                            long tiempoFallo = System.currentTimeMillis() - inicioMuestra;
                            fallos.incrementAndGet();
                            LogManager.log(id, "POOLED", "FALLO", tiempoFallo);
                        } finally {
                            if (conn != null) pool.releaseConnection(conn);
                            doneSignal.countDown();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        doneSignal.countDown();
                    }
                }).start();
            }

            startSignal.countDown();

            new Thread(() -> {
                try {
                    doneSignal.await();
                    long totalTime = System.currentTimeMillis() - startTime;
                    double pctExito = (exitos.get() * 100.0) / samples;
                    double pctFallo = (fallos.get() * 100.0) / samples;

                    gui.appendToGui("\n--- RESULTADOS POOLED ---");
                    gui.appendToGui("Tiempo Total: " + totalTime + " ms");
                    gui.appendToGui("Éxitos: " + exitos.get() + " (" + String.format("%.2f", pctExito) + "%)");
                    gui.appendToGui("Fallos: " + fallos.get() + " (" + String.format("%.2f", pctFallo) + "%)");
                    gui.enableButtons(true);

                    pool.shutdown();
                } catch (InterruptedException e) {
                    gui.enableButtons(true);
                }
            }).start();

        } catch (SQLException e) {
            gui.appendToGui("Error al conectar: " + e.getMessage());
            gui.enableButtons(true);
        }
    }

    public void runRawSimulation() {
        this.stopRequested = false;
        int samples = DatabaseConfig.getSamples();
        long startTime = System.currentTimeMillis();

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallos = new AtomicInteger(0);

        gui.appendToGui("Iniciando Simulación RAW (Muestras: " + samples + ")...");
        LogManager.logHeader("INICIO SIMULACIÓN RAW");

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(samples);

        for (int i = 1; i <= samples; i++) {
            final int id = i;
            new Thread(() -> {
                long inicioMuestra = System.currentTimeMillis();
                try {
                    startSignal.await();

                    if (stopRequested) {
                        fallos.incrementAndGet();
                        doneSignal.countDown();
                        return;
                    }

                    try (Connection conn = DriverManager.getConnection(
                            DatabaseConfig.getUrl(),
                            DatabaseConfig.getUser(),
                            DatabaseConfig.getPassword())) {

                        Statement stmt = conn.createStatement();
                        stmt.execute(DatabaseConfig.getQuery());

                        long tiempoRespuesta = System.currentTimeMillis() - inicioMuestra;
                        exitos.incrementAndGet();
                        LogManager.log(id, "RAW", "EXITO", tiempoRespuesta);

                    } catch (Exception e) {
                        long tiempoFallo = System.currentTimeMillis() - inicioMuestra;
                        fallos.incrementAndGet();
                        LogManager.log(id, "RAW", "FALLO", tiempoFallo);
                    } finally {
                        doneSignal.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    doneSignal.countDown();
                }
            }).start();
        }

        startSignal.countDown();

        new Thread(() -> {
            try {
                doneSignal.await();
                long totalTime = System.currentTimeMillis() - startTime;

                double pctExito = (exitos.get() * 100.0) / samples;
                double pctFallo = (fallos.get() * 100.0) / samples;

                gui.appendToGui("\n--- RESULTADOS RAW ---");
                gui.appendToGui("Tiempo Total: " + totalTime + " ms");
                gui.appendToGui("Éxitos: " + exitos.get() + " (" + String.format("%.2f", pctExito) + "%)");
                gui.appendToGui("Fallos: " + fallos.get() + " (" + String.format("%.2f", pctFallo) + "%)");
                gui.enableButtons(true);
            } catch (InterruptedException e) {
                gui.enableButtons(true);
            }
        }).start();
    }
}