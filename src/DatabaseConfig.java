import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static Properties prop = new Properties();

    static {
        try {
            prop.load(new FileInputStream("config.properties"));
            System.out.println("Configuración cargada correctamente.");
        } catch (IOException e) {
            System.err.println("Error cargando config.properties: " + e.getMessage());
        }
    }

    public static String getUrl() { return prop.getProperty("db.url"); }
    public static String getUser() { return prop.getProperty("db.user"); }
    public static String getPass() { return prop.getProperty("db.password"); }
    public static String getQuery() { return prop.getProperty("test.query"); }
}