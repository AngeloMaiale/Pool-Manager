import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SimpleConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final int poolSize;

    public SimpleConnectionPool(int size) throws SQLException {
        this.poolSize = size;
        this.pool = new ArrayBlockingQueue<>(size);
        initializePool();
    }

    private void initializePool() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            Connection conn = createNewConnection();
            pool.offer(conn);
        }
        System.out.println("Pool inicializado con " + poolSize + " conexiones.");
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
    }

    public Connection getConnection() throws InterruptedException {
        long startWait = System.currentTimeMillis();
        Connection conn = pool.poll(3, TimeUnit.SECONDS);
        long waitTime = System.currentTimeMillis() - startWait;
        if (conn == null) {
            System.err.println("[ADVERTENCIA] Pool vacío. Hilos esperando demasiado.");
            throw new InterruptedException("Tiempo de espera de pool agotado");
        }
        if (waitTime > 4000) {
            System.out.println("[OPTIMIZACIÓN] Sugerencia: El pool está saturado. Aumenta 'pool.size' en config.properties.");
        }

        return conn;
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }

    public void shutdown() {
        for (Connection conn : pool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        pool.clear();
    }
}