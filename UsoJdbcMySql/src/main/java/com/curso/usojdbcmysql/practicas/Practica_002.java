package com.curso.usojdbcmysql.practicas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Se conecta y desconecta de la base de datos, informando de sus acciones a través de mensajes en consola.
 * @author JMD
 * @version 1.0
 */
public class Practica_002 {

    public static void main(String[] args) {
        try {
            Connection con = null;
            Class.forName(driverJdbc);
            con = DriverManager.getConnection(urlJdbc);
            System.out.println("Conexión establecida con la base de datos");
            if (con != null) {
                con.close();
                System.out.println("Desconexión de la base de datos realizada con éxito");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Practica_002.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static final String driverJdbc = "com.mysql.jdbc.Driver";
    private static final String urlJdbc = "jdbc:mysql://localhost/test?user=root&password=root";
}
