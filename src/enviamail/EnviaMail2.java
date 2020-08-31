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
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
/**
 *
 * @author SebaTordecillaDiaz
 */
public class EnviaMail2 {

Properties prop;
  
  Session session;
  
  MimeMessage mensajemail;
  
  Transport t;
  
    public EnviaMail2() {
    this.prop = new Properties();
    this.prop.setProperty("mail.smtp.host", "smtp.office365.com");
    this.prop.setProperty("mail.smtp.port", "587");
    this.prop.setProperty("mail.smtp.user", "salfalink@salfa.cl");
    this.prop.setProperty("mail.smtp.auth", "true");
    this.prop.setProperty("mail.smtp.starttls.enable", "true");
    this.session = Session.getDefaultInstance(this.prop);
    this.session.setDebug(true);
    this.mensajemail = new MimeMessage(this.session);
  }
  
  public static void main(String[] args) {
    try {
      System.out.println("Inicio");
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      //Connection con = DriverManager.getConnection("jdbc:sqlserver://150.0.20.202;databaseName=SalfaLink", "sa", "Micrologica2014");
      Connection con = DriverManager.getConnection("jdbc:sqlserver://;servername=DESKTOP-5D5GRD2;databaseName=SalfaLink","seba","Micrologica2020");
      while (true) {
        try {
          //con = DriverManager.getConnection("jdbc:sqlserver://150.0.20.202;databaseName=SalfaLink", "sa", "Micrologica2014");
          con = DriverManager.getConnection("jdbc:sqlserver://;servername=DESKTOP-5D5GRD2;databaseName=SalfaLink","seba","Micrologica2020");
        } catch (SQLException e) {
          e.printStackTrace();
          if (con != null)
            con.close(); 
          int c = 0;
          while (c == 0) {
            try {
              //con = DriverManager.getConnection("jdbc:sqlserver://150.0.20.202;databaseName=SalfaLink", "sa", "Micrologica2014");
              con = DriverManager.getConnection("jdbc:sqlserver://;servername=DESKTOP-5D5GRD2;databaseName=SalfaLink","seba","Micrologica2020");
              c = 1;
            } catch (SQLException s) {
              System.out.println("Err: " + s.toString());
            } 
            Thread.sleep(15000L);
          } 
        } 
        try {
          Statement st = con.createStatement();
          //ResultSet rs = st.executeQuery("select r.CodReporte,r.Asunto,r.Cuerpo,ra.ListaDistribucion,r.NomArchivo from Reporte r inner join ReporteAutomatico ra on ra.CodRA=r.ReporteAutomatico_CodRA where r.Estado=0");
          ResultSet rs = st.executeQuery("select r.CodReporte,r.Asunto,r.Cuerpo,'stordecilla@micrologica.cl' as ListaDistribucion,r.NomArchivo from Reporte r inner join ReporteAutomatico ra on ra.CodRA=r.ReporteAutomatico_CodRA where r.Estado=0");
          System.out.println("Consulta");
          if (rs.getFetchSize() > 0)
            while (rs.next()) {
              try {
                System.out.println("Inicio App");
                EnviaMail2 tx = new EnviaMail2();
                tx.t = tx.session.getTransport("smtp");
                tx.t.connect("salfalink@salfa.cl", "Portal2012");
                String mensaje = rs.getObject("Cuerpo").toString();
                String asunto = rs.getObject("Asunto").toString();
                String email = rs.getObject("ListaDistribucion").toString();
                email = email.replace(';', ',');
                String archivo = rs.getObject("NomArchivo").toString();
                tx.mensajemail.setFrom((Address)new InternetAddress("salfalink@salfa.cl"));
                StringTokenizer stk = new StringTokenizer(email, ",");
                while (stk.hasMoreTokens())
                tx.mensajemail.addRecipient(Message.RecipientType.TO, (Address)new InternetAddress(stk.nextToken())); 
                tx.mensajemail.setSubject(asunto);
                MimeMultipart contentPart = new MimeMultipart("mixed");
                MimeMultipart bodyPart = new MimeMultipart("alternative");
                MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
                mimeBodyPart1.setContent(mensaje, "text/html");
                bodyPart.addBodyPart((BodyPart)mimeBodyPart1);
                MimeBodyPart bodyContent = new MimeBodyPart();
                bodyContent.setContent((Multipart)bodyPart);
                contentPart.addBodyPart((BodyPart)bodyContent);
                MimeBodyPart mimeBodyPart2 = new MimeBodyPart();
                try {
                  FileDataSource fileDataSource = new FileDataSource("Z:/" + archivo);
                  mimeBodyPart2.setDataHandler(new DataHandler((DataSource)fileDataSource));
                  mimeBodyPart2.setFileName(archivo);
                  contentPart.addBodyPart((BodyPart)mimeBodyPart2);
                  tx.mensajemail.setContent(contentPart, "text/html; charset=utf-8");
                  tx.t.sendMessage((Message)tx.mensajemail, tx.mensajemail.getAllRecipients());
                  st = con.createStatement();
                  st.executeUpdate("update Reporte set Estado=1 where CodReporte=" + rs.getObject("CodReporte"));
                  tx.t.close();
                } catch (Exception e) {
                  e.printStackTrace();
                  st = con.createStatement();
                  st.executeUpdate("update Reporte set Estado=9 where CodReporte=" + rs.getObject("CodReporte"));
                  tx.t.close();
                } 
              } catch (Exception e) {
                e.printStackTrace();
              } 
            }  
          Thread.sleep(3600000L);
          con.close();
        } catch (SQLException e) {
          e.printStackTrace();
          System.out.println("Err: " + e.toString());
          con.close();
        } catch (Exception e) {
          e.printStackTrace();
        } 
      } 
    } catch (Exception exception) {
      return;
    } 
  }
    
}
