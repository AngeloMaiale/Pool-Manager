import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {
    private static final String FILE_NAME = "simulacion.log";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static synchronized void log(int id, String tipo, String estado, long ms) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            String timestamp = dtf.format(LocalDateTime.now());
            out.printf("%-23s | %-7s | ID: %-5d | %-10s | %d ms%n",
                    timestamp, tipo, id, estado, ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void log(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            String timestamp = dtf.format(LocalDateTime.now());
            out.println(timestamp + " | INFO | " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void logHeader(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            out.println("\n" + "=".repeat(20) + " " + message + " " + "=".repeat(20));
        } catch (Exception e) { e.printStackTrace(); }
    }
}