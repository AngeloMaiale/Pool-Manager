import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static Properties prop = new Properties();

    static {
        File file = new File("config.properties");
        System.out.println("--- DIAGNÓSTICO DE CONFIGURACIÓN ---");
        System.out.println("Ruta absoluta donde busco: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.err.println("¡ERROR!: El archivo 'config.properties' NO EXISTE en esa ruta.");
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                prop.load(fis);
                System.out.println("Archivo cargado. Total de llaves encontradas: " + prop.size());
                // Listar las llaves que Java leyó para ver si hay errores de escritura
                prop.list(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("------------------------------------");
    }

    public static String getUrl() { return prop.getProperty("db.url"); }
    public static String getUser() { return prop.getProperty("db.user"); }
    public static String getPass() { return prop.getProperty("db.password"); }
}