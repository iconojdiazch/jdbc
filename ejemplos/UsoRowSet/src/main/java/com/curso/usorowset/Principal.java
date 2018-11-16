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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static void main(String[] args) {
        try {
            Principal p = new Principal();
            p.cargarDriver();
            if (args == null) {
                p.usarConexion();
            } else if (args[0].equals("datasource")) {
                p.usarDataSource();
            } else {
                p.usarConexion();
            }
            p.usoRowSet();
            p.usoRowSetListener();
            p.usoCachedRowset();
            p.serializarCachedRowSet();
            p.usoWebRowSet();
            p.usoClavesGeneradas();
            p.usoSavePoint("abc", 1);
            p.cerrarConexion();
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    private void usarConexion() throws SQLException {
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/sample", "app", "app");
        System.out.println("Conectado a la base de datos usando el DriverManager");
    }

    private void cargarDriver() throws ClassNotFoundException {
        Class.forName("org.apache.derby.jdbc.ClientDriver");
    }

    private void cerrarConexion() throws SQLException {
        if (con != null) {
            con.close();
            System.out.println("Desconectado de la base de datos");
        }
    }

    private void serializarCachedRowSet() throws SQLException, IOException, ClassNotFoundException {
        try (CachedRowSet rowSet = new CachedRowSetImpl()) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            System.out.println("Serializando el RowSet...");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rowset.ser"))) {
                oos.writeObject(rowSet);
            }
            System.out.println("Serialización completa");
            System.out.println("Resuperando el rowset del disco...");
            CachedRowSet nuevo;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("rowset.ser"))) {
                nuevo = (CachedRowSet) ois.readObject();
            }
            System.out.println("Recuperación completa");
            System.out.println("Mostrando los datos...");
            while (nuevo.next()) {
                System.out.format("Id = %d. Nombre = %s%n", nuevo.getInt("customer_id"), nuevo.getString("name"));
            }
        }
    }

    private void usarDataSource() throws SQLException {
        ds = new ClientConnectionPoolDataSource();
        ds.setUser("app");
        ds.setPassword("app");
        ds.setServerName("localhost");
        ds.setDatabaseName("sample");
        con = ds.getConnection();
        Integer hold = con.getHoldability();
        switch (hold) {
            case ResultSet.HOLD_CURSORS_OVER_COMMIT:
                System.out.println("Tipo de \"Holding\": HOLD_CURSORS_OVER_COMMIT");
                break;
            case ResultSet.CLOSE_CURSORS_AT_COMMIT:
                System.out.println("Tipo de \"Holding\": CLOSE_CURSORS_AT_COMMIT");
                break;
            default:
                System.out.println("Tipo de holding inesperado");
                break;
        }

        System.out.println("Conectado a la base de datos usando una datasource");
    }

    private void usoSavePoint(String nombre, Integer veces) throws SQLException {
        DatabaseMetaData dbmd = con.getMetaData();
        Boolean b = dbmd.supportsSavepoints();
        System.out.println(b ? "Savepoints soportados" : "Savepoints no soportados");
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
                    System.out.println("Rollback sobre el savepoint");
                }
                con.commit();
            } catch (SQLException e) {
                while (e != null) {
                    System.out.println(e);
                    e = e.getNextException();
                }
                con.rollback();
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void usoCachedRowset() throws SQLException {
        try (CachedRowSet rowSet = new CachedRowSetImpl()) {
            rowSet.addRowSetListener(new RowSetListener() {
                
                @Override
                public void rowSetChanged(RowSetEvent event) {
                    System.out.println("El rowset ha cambiado");
                    System.out.println(event.toString());
                    System.out.println(event.getSource().toString());
                }
                
                @Override
                public void rowChanged(RowSetEvent event) {
                    System.out.println("Una fila del rowset ha cambiado");
                    System.out.println(event.toString());
                }
                
                @Override
                public void cursorMoved(RowSetEvent event) {
                    System.out.println("Nos hemos desplazado por el RowSet");
                    System.out.println(event.toString());
                }
            });
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            while (rowSet.next()) {
                System.out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoClavesGeneradas() throws SQLException {
        DatabaseMetaData dbmd = con.getMetaData();
        Boolean b = dbmd.supportsGetGeneratedKeys();
        System.out.println(b ? "Hay soporte para obtener claves generadas" : "No hay soporte para obtener claves generadas");
        if (b) {
            String[] cols = {"CUSTOMER_ID"};
            PreparedStatement ps = con.prepareStatement("INSERT INTO CUSTOMER (NAME) VALUES (?)", cols);
            ps.setString(1, "En claves generadas");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                System.out.println("Clave: " + rs.getString(1));
            }
        }
    }

    private void usoRowSet() throws SQLException {
        try (JdbcRowSet rowSet = new JdbcRowSetImpl(con)) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute();
            while (rowSet.next()) {
                System.out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoRowSetListener() throws SQLException {
        try (JdbcRowSet rowSet = new com.sun.rowset.JdbcRowSetImpl(con)) {
            rowSet.addRowSetListener(new RowSetListener() {
                
                @Override
                public void rowSetChanged(RowSetEvent event) {
                    System.out.println("El rowset ha cambiado");
                    System.out.println(event.toString());
                    System.out.println(event.getSource().toString());
                }
                
                @Override
                public void rowChanged(RowSetEvent event) {
                    System.out.println("Una fila del rowset ha cambiado");
                    System.out.println(event.toString());
                }
                
                @Override
                public void cursorMoved(RowSetEvent event) {
                    System.out.println("Nos hemos desplazado por el RowSet");
                    System.out.println(event.toString());
                }
            });
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute();
            while (rowSet.next()) {
                System.out.format("Id = %d. Nombre = %s%n", rowSet.getInt("customer_id"), rowSet.getString("name"));
            }
        }
    }

    private void usoWebRowSet() throws SQLException, IOException {
        try (WebRowSet rowSet = new WebRowSetImpl()) {
            rowSet.setCommand("select customer_id, name from customer order by name");
            rowSet.execute(con);
            try (FileWriter xmlSalida = new FileWriter("rowset.xml")) {
                System.out.println("Escribiendo los datos en el archivo \"rowset.xml\"...");
                rowSet.writeXml(xmlSalida);
                System.out.println("Datos escritos");
            }
            WebRowSet nuevo = new WebRowSetImpl();
            try (FileReader entradaXml = new FileReader("rowset.xml")) {
                System.out.println("Recuperando los datos del archivo \"rowset.xml\"...");
                nuevo.readXml(entradaXml);
                System.out.println("Datos recuperados");
            }
            System.out.println("Mostrando los datos...");
            nuevo.beforeFirst();
            while (nuevo.next()) {
                System.out.format("Id = %d. Nombre = %s%n", nuevo.getInt("customer_id"), nuevo.getString("name"));
            }
        }
    }
}
