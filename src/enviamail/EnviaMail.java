/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enviamail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 *
 * @author SebaTordecillaDiaz
 */
public class EnviaMail {

Properties prop;
  
  Session session;
  
  MimeMessage mensajemail;
  
  Transport t;
  
  public EnviaMail() throws Exception {
    this.prop = new Properties();
    this.prop.setProperty("mail.smtp.host", "mail.micrologica.cl");
    //this.prop.setProperty("mail.smtp.host", "smtp.office365.com");
    Log.log("Host: mail.micrologica.cl");
    this.prop.setProperty("mail.smtp.port", "25");
    //this.prop.setProperty("mail.smtp.port", "587");
    Log.log("Port: 25");
    this.prop.setProperty("mail.smtp.user", "ingenieria@micrologica.cl");
    //this.prop.setProperty("mail.smtp.user", "stordecilla@micrologica.cl");
    Log.log("User: ingenieria@micrologica.cl");
    this.prop.setProperty("mail.smtp.auth", "true");
    this.session = Session.getDefaultInstance(this.prop);
    this.session.setDebug(true);
    this.mensajemail = new MimeMessage(this.session);
  }
  
  public boolean eviarMail(String mensaje, String asunto, String mail) {
    try {
      this.mensajemail.setFrom((Address)new InternetAddress("ingenieria@micrologica.cl"));  
      //this.mensajemail.setFrom((Address)new InternetAddress("stordecilla@micrologica.cl"));
      this.mensajemail.setRecipient(Message.RecipientType.TO, (Address)new InternetAddress(mail));
      this.mensajemail.setSubject(asunto);
      this.mensajemail.setText(mensaje);
      this.t.sendMessage((Message)this.mensajemail, this.mensajemail.getAllRecipients());
      Log.log("Correo enviado");
      return true;
    } catch (Exception e) {
      Log.log(e.toString());
      return false;
    } 
  }
  
  public boolean eviarMail(String mensaje, String asunto) {
    try {
      //this.mensajemail.setFrom((Address)new InternetAddress("stordecilla@micrologica.cl"));
      this.mensajemail.setFrom((Address)new InternetAddress("ingenieria@micrologica.cl"));
      this.mensajemail.setSubject(asunto);
      this.mensajemail.setText(mensaje);
      this.t.sendMessage((Message)this.mensajemail, this.mensajemail.getAllRecipients());
      Log.log("Correo enviado");
      return true;
    } catch (Exception e) {
      Log.log(e.toString());
      e.printStackTrace();
      return false;
    } 
  }
  
  public void agregaDestinatario(String mail) {
    try {
      this.mensajemail.addRecipient(Message.RecipientType.TO, (Address)new InternetAddress(mail));
    } catch (Exception e) {
      Log.log(e.toString());
    } 
  }
  
  public void eliminaDestinatario() {
    try {
      this.mensajemail = null;
      this.mensajemail = new MimeMessage(this.session);
    } catch (Exception e) {
      Log.log(e.toString());
    } 
  }
  
  public static void main(String[] args) {
    while (true) {
      try {
        try {
          EnviaMail em = new EnviaMail();
          System.out.println("Entro 1");
          Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
          Connection con = null;
          int contador = 0;
          while (true) {
            try {
              //con = DriverManager.getConnection("jdbc:sqlserver://150.0.20.202;databaseName=DB_GPS", "sa", "Micrologica2014");
              con = DriverManager.getConnection("jdbc:sqlserver://;servername=DESKTOP-5D5GRD2;databaseName=DB_GPS","seba","Micrologica2020");
            } catch (SQLException e) {
              e.printStackTrace();
              Log.log(e.toString());
              if (con != null)
                con.close(); 
              int c = 0;
              while (c == 0) {
                try {
                  //con = DriverManager.getConnection("jdbc:sqlserver://150.0.20.202;databaseName=DB_GPS", "sa", "Micrologica2014");
                  con = DriverManager.getConnection("jdbc:sqlserver://;servername=DESKTOP-5D5GRD2;databaseName=DB_GPS","seba","Micrologica2020");
                  Log.log("Se recupera conexion");
                  c = 1;
                } catch (SQLException s) {
                  System.out.println("Err: " + s.toString());
                } 
                Thread.sleep(15000L);
              } 
            } 
            try {
              System.out.println("Entro While");
              Statement st = con.createStatement(1004, 1007);
              ResultSet rs = st.executeQuery("select codmail,mensaje,asunto,email from mensajemail where estado=0 and (email not like '%tissue%' or email not like '%cmpc%')");
              rs.last();
              em.t = em.session.getTransport("smtp");
              em.t.connect("ingenieria@micrologica.cl", "IngMicro17");
              //em.t.connect("stordecilla@micrologica.cl", "SebTor2020");
              System.out.println("Conexion creada");
              if (rs.getRow() > 0) {
                rs.beforeFirst();
                while (rs.next()) {
                  if (contador > 80) {
                    em.t.close();
                    em.t.connect("ingenieria@micrologica.cl", "IngMicro17");
                    //em.t.connect("stordecilla@micrologica.cl", "SebTor2020");
                    contador = 0;
                  } 
                  Log.log("Conexicreada");
                  String mensaje = rs.getObject("mensaje").toString();
                  String asunto = rs.getObject("asunto").toString();
                  String email = rs.getObject("email").toString();
                  Log.log("Email: " + email + " Asunto: " + asunto + " Mensaje: " + mensaje);
                  if (em.eviarMail(mensaje, asunto, email)) {
                    st = con.createStatement();
                    st.executeUpdate("update mensajemail set estado=1 where codmail=" + rs.getObject("codmail"));
                    Log.log("Registro actualizado");
                    em.eliminaDestinatario();
                  } else {
                    st = con.createStatement();
                    st.executeUpdate("update mensajemail set estado=99 where codmail=" + rs.getObject("codmail"));
                    Log.log("Correo no enviado, actualizado como no enviado");
                    em.eliminaDestinatario();
                  } 
                  contador++;
                } 
              } 
              Thread.sleep(30000L);
              st = con.createStatement(1004, 1007);
              rs = st.executeQuery("select distinct mensaje from mensajemail where (email like '%tissue%' or email like '%cmpc%') and estado=0");
              rs.last();
              if (rs.getRow() > 0) {
                rs.beforeFirst();
                while (rs.next()) {
                  if (contador > 80) {
                    em.t.close();
                    em.t.connect("ingenieria@micrologica.cl", "IngMicro17");
                    //em.t.connect("stordecilla@micrologica.cl", "SebTor2020");
                    contador = 0;
                  } 
                  String mensaje = rs.getObject("mensaje").toString();
                  st = con.createStatement(1004, 1007);
                  ResultSet rs1 = st.executeQuery("select codmail,asunto,email from mensajemail where mensaje='" + mensaje + "' and estado=0");
                  rs1.last();
                  if (rs1.getRow() > 0) {
                    rs1.beforeFirst();
                    String asunto = "";
                    String email = "";
                    String codmail = "";
                    while (rs1.next()) {
                      asunto = rs1.getObject("asunto").toString();
                      email = rs1.getObject("email").toString();
                      em.agregaDestinatario(email);
                      Log.log("Email: " + email + " Asunto: " + asunto + " Mensaje: " + mensaje);
                      if (codmail.equalsIgnoreCase("")) {
                        codmail = codmail + rs1.getObject("codmail");
                        continue;
                      } 
                      codmail = codmail + "," + rs1.getObject("codmail");
                    } 
                    if (em.eviarMail(mensaje, asunto)) {
                      st = con.createStatement();
                      st.executeUpdate("update mensajemail set estado=1 where codmail in (" + codmail + ")");
                      Log.log("Registro actualizado");
                      em.eliminaDestinatario();
                      continue;
                    } 
                    st = con.createStatement();
                    st.executeUpdate("update mensajemail set estado=99 where codmail in (" + codmail + ")");
                    Log.log("Correo no enviado, actualizado como no enviado");
                    em.eliminaDestinatario();
                  } 
                } 
              } 
              em.t.close();
              con.close();
            } catch (SQLException e) {
              e.printStackTrace();
              System.out.println("Err: " + e.toString());
              Log.log("ERR: " + e.toString());
              con.close();
            } catch (MessagingException m) {
              Log.log(m.toString());
              em = null;
              em = new EnviaMail();
              em.t = em.session.getTransport("smtp");
              em.t.connect("ingenieria@micrologica.cl", "IngMicro17");
              //em.t.connect("stordecilla@micrologica.cl", "SebTor2020");
            } catch (Exception e) {
              e.printStackTrace();
              Log.log(e.toString());
            } 
          } 
          //break;
        } catch (Exception e) {
          e.printStackTrace();
          Log.log(e.toString());
        } 
      } catch (Exception e) {
        e.printStackTrace();
        Log.log(e.toString());
      } 
      try {
        Thread.sleep(30000L);
      } catch (InterruptedException ex) {
        Logger.getLogger(EnviaMail.class.getName()).log(Level.SEVERE, (String)null, ex);
      } 
    } 
  }
    
}
