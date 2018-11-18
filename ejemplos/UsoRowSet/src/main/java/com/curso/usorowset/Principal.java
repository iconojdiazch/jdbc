/*
 * Main.java
 *
 * Created on 12-sep-2007, 14:55:04
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.usorowset;

import com.sun.rowset.CachedRowSetImpl;
import com.sun.rowset.JdbcRowSetImpl;
import com.sun.rowset.WebRowSetImpl;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Class.forName;
import static java.lang.System.out;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import static java.sql.DriverManager.getConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
import static java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.WebRowSet;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

/**
 *
 * @author user
 */
public class Principal {

    private Connection con = null;
    private ClientConnectionPoolDataSource ds = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        Principal p = new Principal();
        p.usoRowSet();
        p.usoRowSetListener();
        p.usoCachedRowset();
        p.serializarCachedRowSet();
        p.usoWebRowSet();
        p.usoClavesGeneradas();
        p.usoSavePoint("abc", 1);
        p.cerrarConexion();

    }

    private void usarConexion() throws SQLException {
        con = getConnection("jdbc:derby://localhost:1527/sample", "app", "app");
        out.println("Conectado a la base de datos usando el DriverManager");
    }

    private void cargarDriver() throws ClassNotFoundException {
        System.out.println("................EN cargarDriver");
        forName("org.apache.derby.jdbc.ClientDriver");
    }

    private void cerrarConexion() throws SQLException {
        if (con != null) {
            con.close();
            out.println("Desconectado de la base de datos");
        }
    }

    private void serializarCachedRowSet() throws SQLException, IOException, ClassNotFoundException {
        System.out.println("................EN serializarCachedRowSet");
        usarConexion();
        try (CachedRowSet rowSet = new CachedRowSetImpl()) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            out.println("Serializando el RowSet...");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rowset.ser"))) {
                oos.writeObject(rowSet);
            }
            out.println("Serialización completa");
            out.println("Resuperando el rowset del disco...");
            CachedRowSet nuevo;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("rowset.ser"))) {
                nuevo = (CachedRowSet) ois.readObject();
            }
            out.println("Recuperación completa");
            out.println("Mostrando los datos...");
            while (nuevo.next()) {
                out.format("Id = %d. Nombre = %s%n", nuevo.getInt("customer_id"), nuevo.getString("name"));
            }
        }
    }

    private void usarDataSource() throws SQLException {
        System.out.println("................EN usarDataSource");
        ds = new ClientConnectionPoolDataSource();
        ds.setUser("app");
        ds.setPassword("app");
        ds.setServerName("localhost");
        ds.setDatabaseName("sample");
        con = ds.getConnection();
        Integer hold = con.getHoldability();
        switch (hold) {
            case HOLD_CURSORS_OVER_COMMIT:
                out.println("Tipo de \"Holding\": HOLD_CURSORS_OVER_COMMIT");
                break;
            case CLOSE_CURSORS_AT_COMMIT:
                out.println("Tipo de \"Holding\": CLOSE_CURSORS_AT_COMMIT");
                break;
            default:
                out.println("Tipo de holding inesperado");
                break;
        }

        out.println("Conectado a la base de datos usando una datasource");
    }

    private void usoSavePoint(String nombre, Integer veces) throws SQLException {
        System.out.println("................EN usoSavePoint");

        usarConexion();
        DatabaseMetaData dbmd = con.getMetaData();
        Boolean b = dbmd.supportsSavepoints();
        out.println(b ? "Savepoints soportados" : "Savepoints no soportados");
        if (b) {
            Savepoint sv;
            try {
                con.setAutoCommit(false);
                sv = con.setSavepoint();
                PreparedStatement ps = con.prepareStatement("INSERT INTO MANUFACTURER (MANUFACTURER_ID,NAME) VALUES (?,?)");
                for (Integer i = 0; i < veces; i++) {
                    ps.setInt(1, 500 + i);
                    ps.setString(2, nombre + i);
                    ps.addBatch();
                }
                ps.executeBatch();
                if ((veces & 1) == 1) {
                    //Una manera rápida de comprobar si un número es impar
                    con.rollback(sv);
                    out.println("Rollback sobre el savepoint");
                }
                con.commit();
            } catch (SQLException e) {
                while (e != null) {
                    out.println(e);
                    e = e.getNextException();
                }
                con.rollback();
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void usoCachedRowset() throws SQLException {
        System.out.println("................EN usoCachedRowset");

        usarConexion();
        try (CachedRowSet rowSet = new CachedRowSetImpl()) {
            rowSet.addRowSetListener(new RowSetListener() {

                @Override
                public void rowSetChanged(RowSetEvent event) {
                    out.println("El rowset ha cambiado");
                    out.println(event.toString());
                    out.println(event.getSource().toString());
                }

                @Override
                public void rowChanged(RowSetEvent event) {
                    out.println("Una fila del rowset ha cambiado");
                    out.println(event.toString());
                }

                @Override
                public void cursorMoved(RowSetEvent event) {
                    out.println("Nos hemos desplazado por el RowSet");
                    out.println(event.toString());
                }
            });
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            while (rowSet.next()) {
                out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoClavesGeneradas() throws SQLException {
        System.out.println("................EN usoClavesGeneradas");
        usarConexion();
        DatabaseMetaData dbmd = con.getMetaData();
        Boolean b = dbmd.supportsGetGeneratedKeys();
        out.println(b ? "Hay soporte para obtener claves generadas" : "No hay soporte para obtener claves generadas");
        if (b) {
            String[] cols = {"CUSTOMER_ID"};
            PreparedStatement ps = con.prepareStatement("INSERT INTO CUSTOMER (NAME) VALUES (?)", cols);
            ps.setString(1, "En claves generadas");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                out.println("Clave: " + rs.getString(1));
            }
        }
    }

    private void usoRowSet() throws SQLException {
        System.out.println("................EN usoRowSet");
        usarConexion();
        try (JdbcRowSet rowSet = new JdbcRowSetImpl(con)) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute();
            while (rowSet.next()) {
                out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoRowSetListener() throws SQLException {
        System.out.println("................EN usoRowSetListener");
        usarConexion();
        try (JdbcRowSet rowSet = new com.sun.rowset.JdbcRowSetImpl(con)) {
            rowSet.addRowSetListener(new RowSetListener() {

                @Override
                public void rowSetChanged(RowSetEvent event) {
                    out.println("El rowset ha cambiado");
                    out.println(event.toString());
                    out.println(event.getSource().toString());
                }

                @Override
                public void rowChanged(RowSetEvent event) {
                    out.println("Una fila del rowset ha cambiado");
                    out.println(event.toString());
                }

                @Override
                public void cursorMoved(RowSetEvent event) {
                    out.println("Nos hemos desplazado por el RowSet");
                    out.println(event.toString());
                }
            });
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute();
            while (rowSet.next()) {
                out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoWebRowSet() throws SQLException, IOException {
        System.out.println("................EN usoWebRowSet");
        usarConexion();
        try (WebRowSet rowSet = new WebRowSetImpl()) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            try (FileWriter xmlSalida = new FileWriter("rowset.xml")) {
                out.println("Escribiendo los datos en el archivo \"rowset.xml\"...");
                rowSet.writeXml(xmlSalida);
                out.println("Datos escritos");
            }
            WebRowSet nuevo = new WebRowSetImpl();
            try (FileReader entradaXml = new FileReader("rowset.xml")) {
                out.println("Recuperando los datos del archivo \"rowset.xml\"...");
                nuevo.readXml(entradaXml);
                out.println("Datos recuperados");
            }
            out.println("Mostrando los datos...");
            nuevo.beforeFirst();
            while (nuevo.next()) {
                out.format("Id = %d. Nombre = %s%n", nuevo.getInt("customer_id"), nuevo.getString("name"));
            }
        }
    }
}
