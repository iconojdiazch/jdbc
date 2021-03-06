/* Generated by Together */
package com.curso.usojdbcmysql.practicas;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modifica Practica_005 para que algunas de las operaciones que realiza, o todas ellas, empleen
 * actualizaciones en modo batch.
 * @author JMD
 * @version 1.0
 */
public class Practica_006 extends PracticaBase {

    public static void main(String[] args) {
        try {
            Practica_006 p = new Practica_006();
            p.ejecutar();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Practica_006.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insercionEnModoBatch() throws SQLException {
        PreparedStatement st = null;
        final String ordenSQL = "INSERT INTO CONTACTOS (NOMBRE,APELLIDOS) VALUES(?,?)";
        final int veces = 5;
        System.out.println("Realizando inserción en modo batch...");
        st = con.prepareStatement(ordenSQL);
        for (int i = 0; i < veces; i++) {
            st.setString(1, "ABC");
            st.setString(2, "XYZ");
            st.addBatch();
        }
        int[] filas = st.executeBatch();
        for (int i = 0; i < filas.length; i++) {
            System.out.println("Filas afectadas: " + filas[i]);
        }
        if (st != null) {
            st.close();
        }
    }

    private void actualizacion() throws SQLException {
        Statement st = null;
        st = con.createStatement();
        final String ordenSQL = "UPDATE CONTACTOS SET APELLIDOS = '123' WHERE NOMBRE = 'ABC'";
        System.out.println("Realizando actualización...");
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
        try {
            con.setAutoCommit(false);
            insercionEnModoBatch();
            con.commit();
        } catch (BatchUpdateException ex) {
            System.out.println("Filas afectadas por las órdenes ejecutadas correctamente: ");
            int[] filas = ex.getUpdateCounts();
            for (int i = 0; i < filas.length; i++) {
                System.out.print(filas[i] + "  ");
            }
            con.rollback();
            Logger.getLogger(Practica_006.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            con.setAutoCommit(true);
        }
        seleccion();
        actualizacion();
        borrado();
        seleccion();
    }
}
