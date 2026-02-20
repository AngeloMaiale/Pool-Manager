import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static Properties prop = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("config.properties");
            prop.load(fis);
            fis.close();
            System.out.println("Configuración cargada correctamente.");
        } catch (IOException e) {
            System.err.println("Error: No se encontró config.properties en la raíz del proyecto.");
        }
    }

    public static String getUrl() { return prop.getProperty("db.url"); }
    public static String getUser() { return prop.getProperty("db.user"); }
    public static String getPass() { return prop.getProperty("db.password"); }
    public static String getQuery() { return prop.getProperty("test.query"); }
    public static int getSamples() { return Integer.parseInt(prop.getProperty("test.samples", "10")); }
    public static int getRetries() { return Integer.parseInt(prop.getProperty("test.retries", "3")); }
    public static int getPoolSize() { return Integer.parseInt(prop.getProperty("pool.size", "5")); }
}