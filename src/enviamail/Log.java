package enviamail;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

public class Log {
  public static void log(String mensaje) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date(System.currentTimeMillis()));
    int mes = c.get(2) + 1;
    try {
      PrintWriter escribir = new PrintWriter(new FileWriter("log" + c.get(1) + mes + c.get(5) + ".txt", true));
      escribir.println(new Date(System.currentTimeMillis()) + " - " + mensaje);
      escribir.close();
    } catch (IOException iOException) {}
  }
}
