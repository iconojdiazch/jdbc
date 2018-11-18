/*
 * PracticaBase.java
 *
 * Created on 13-sep-2007, 3:17:09
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.usojdbcmysql.practicas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author user
 */
public abstract class PracticaBase {

    private static final String TABLACONTACTOS = "CREATE TABLE contactos (" + "id int(10) unsigned NOT NULL auto_increment," + "nombre varchar(255) NOT NULL," + "  apellidos varchar(255) NOT NULL," + "PRIMARY KEY  (id)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    protected Connection con = null;
    private static final String DRIVERJDBC = "com.mysql.jdbc.Driver";
    private static final String URLJDBC = "jdbc:mysql://localhost/test?user=root&password=root";
    private static final int ERRORDRIVER = -2;
    private static final int ERRORCONEXION = -1;

    public PracticaBase() {
    }

    public void ejecutar() throws ClassNotFoundException, SQLException {
        conectar();
        crearTablaContactos();
        accionEspecifica();
        desconectar();
    }

    protected abstract void accionEspecifica() throws SQLException;

    private void conectar() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVERJDBC);
        con = DriverManager.getConnection(URLJDBC);
        System.out.println("Conexión establecida con la base de datos");
    }

    private void crearTablaContactos() throws SQLException {
        Statement st = con.createStatement();
        st.executeUpdate("DROP TABLE IF EXISTS contactos");
        st.executeUpdate(TABLACONTACTOS);
    }

    private void desconectar() throws SQLException {
        if (con != null) {
            con.close();
            System.out.println("Desconexión de la base de datos realizada con éxito");
        }
    }

    private void mostrarResultados(final ResultSet rs) throws SQLException {
        for (int i = 1; rs.next(); i++) {
            String nombre = rs.getString("Nombre");
            String apellido = rs.getString("Apellidos");
            System.out.println("Fila " + i + ": " + nombre + " " + apellido);
        }
    }

    protected void seleccion() throws SQLException {
        Statement st;
        ResultSet rs;
        st = con.createStatement();
        final String ordenSQL = "SELECT * FROM CONTACTOS";
        System.out.println("Realizando consulta...");
        rs = st.executeQuery(ordenSQL);
        mostrarResultados(rs);
        if (rs != null) {
            rs.close();
        }
        st.close();
    }
}
