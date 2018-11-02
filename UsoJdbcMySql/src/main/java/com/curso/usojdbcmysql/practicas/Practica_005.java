/* Generated by Together */
package com.curso.usojdbcmysql.practicas;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modifica Practica_004 para que algunas de las operaciones que realiza, o todas ellas, empleen sentencias preparadas.
 * @author JMD
 * @version 1.0
 */
public class Practica_005 extends PracticaBase {

    public static void main(String[] args) {
        try {
            Practica_005 p = new Practica_005();
            p.ejecutar();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Practica_005.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insercionConSentenciaPreparada() throws SQLException {
        PreparedStatement st = null;
        final String ordenSQL = "INSERT INTO CONTACTOS (NOMBRE,APELLIDOS) VALUES(?,?)";
        st = con.prepareStatement(ordenSQL);
        System.out.println("Realizando inserci�n...");
        st.setString(1, "ABC");
        st.setString(2, "XYZ");
        int filas = st.executeUpdate();
        System.out.println("Filas afectadas: " + filas);
        if (st != null) {
            st.close();
        }
    }

    private void actualizacion() throws SQLException {
        Statement st = null;
        st = con.createStatement();
        final String ordenSQL = "UPDATE CONTACTOS SET APELLIDOS = '123' WHERE NOMBRE = 'ABC'";
        System.out.println("Realizando actualizaci�n...");
        int filas = st.executeUpdate(ordenSQL);
        System.out.println("Filas afectadas: " + filas);
        if (st != null) {
            st.close();
        }
    }

    private void borrado() throws SQLException {
        Statement st = null;
        st = con.createStatement();
        final String ordenSQL = "DELETE FROM CONTACTOS WHERE APELLIDOS = '123'";
        System.out.println("Realizando borrado...");
        int filas = st.executeUpdate(ordenSQL);
        System.out.println("Filas afectadas: " + filas);
        if (st != null) {
            st.close();
        }
    }

    protected void accionEspecifica() throws SQLException {
        insercionConSentenciaPreparada();
        seleccion();
        actualizacion();
        borrado();
        seleccion();
    }
}
