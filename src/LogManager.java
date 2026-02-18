import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class LogManager {
    private static final String LOG_FILE = "simulacion_adelanto.log";

    public static void log(String mensaje) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String entry = String.format("[%s] %s", LocalDateTime.now(), mensaje);
            System.out.println(entry); // Mostrar también en consola
            out.println(entry);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}