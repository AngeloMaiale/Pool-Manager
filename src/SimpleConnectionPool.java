import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SimpleConnectionPool {
    // Cola que almacenará las conexiones disponibles
    private final BlockingQueue<Connection> pool;
    private final int poolSize;

    public SimpleConnectionPool(int size) throws SQLException {
        this.poolSize = size;
        this.pool = new ArrayBlockingQueue<>(size);
        initializePool();
    }

    // Crea las conexiones iniciales (Requisito: pool limitado)
    private void initializePool() throws SQLException {
        for (int i = 0; i < poolSize; i++) {
            Connection conn = createNewConnection();
            pool.offer(conn); // Agrega la conexión a la cola
        }
        System.out.println("Pool inicializado con " + poolSize + " conexiones.");
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPass()
        );
    }

    // Método para que un hilo "pida" una conexión
    public Connection getConnection() throws InterruptedException {
        // take() es clave: si la cola está vacía, el hilo espera (bloqueo) 
        // hasta que otro hilo devuelva una conexión.
        return pool.take();
    }

    // Método para que un hilo "devuelva" la conexión (Reciclaje)
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            // Intentamos devolverla a la cola para que otro hilo la use
            pool.offer(conn);
        }
    }

    // Cierra todas las conexiones al final de la simulación
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