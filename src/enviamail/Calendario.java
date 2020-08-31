/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enviamail;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author SebaTordecillaDiaz
 */
public class Calendario {
    public static void main(String[] args) {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date(System.currentTimeMillis()));
    System.out.println(c.get(5));
    System.out.println(c.get(2) + 1);
    System.out.println(c.get(1));
  }
}
