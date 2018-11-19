/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.aproximacionfuncional;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.util.Arrays.stream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.tools.RunScript;

/**
 *
 * @author usuario
 */
public class Principal1 {

    private static final String DB = "jdbc:mysql://localhost/test";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final Logger LOG = Logger.getLogger(Principal.class.getName());
    private static final String H2PASSWORD = "";
    private static final String H2USER = "sa";
    private static final String JDBCH2MEMTEST = "jdbc:h2:mem:test";
    private static final String ORGH2_DRIVER = "org.h2.Driver";

    @FunctionalInterface
    interface Operacion<T> {

        T ejecutar(Connection c) throws SQLException;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Principal1().run(new String[]{"h2"});
    }

    private void run(String[] args) {
        String r = stream(args)
                .findFirst()
                .map(this::crearConexion)
                .map(
                        con -> enTransaccion(con.get(),
                                c -> c.createStatement().executeQuery("select numero from telefonos where persona_id=1000")
                        ))                
                .map(this::mostrarTelefonos)
                .map(this::cerrarConexion)
                .orElse("Nada");
        LOG.info(r);
    }

    private Optional<Connection> mostrarTelefonos(Optional<ResultSet> opt) {
        return opt.map(
                rs -> {
                    try {
                        while (rs.next()) {
                            System.out.println("Teléfono: " + rs.getString("numero"));
                        }
                        return Optional.ofNullable(rs.getStatement().getConnection());
                    } catch (SQLException ex) {
                        LOG.log(Level.SEVERE, "Error", ex);
                    }
                    return Optional.<Connection>empty();
                }
        ).orElse(Optional.<Connection>empty());        
    }

    private <T> Optional<T> enTransaccion(Connection conexion, Principal.Operacion<T> op) {
        Optional<T> resultado = Optional.empty();
        try {
            conexion.setAutoCommit(false);
            resultado = Optional.ofNullable(op.ejecutar(conexion));
            conexion.commit();
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Error en rollback", ex);
            }
            LOG.log(Level.SEVERE, "Error en enTransaccion: {0}", e);
        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Error en close", ex);
            }
        }
        return resultado;
    }

    private Optional<Connection> crearConexion(String cual) {
        switch (cual.toLowerCase()) {
            case "mysql":
                return Optional.ofNullable(abrirConexionMySql());
            case "h2":
                return Optional.ofNullable(abrirConexionH2());
            default:
                return Optional.empty();
        }
    }

    private Connection abrirConexionMySql() {
        Connection con = null;
        LOG.info("Usando MySql");
        try {
            Class.forName(DRIVER).newInstance();
            con = DriverManager.getConnection(DB, "root", "root");
            LOG.info("Conexión establecida con la base de datos");
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return con;
    }

    private Connection abrirConexionH2() {
        Connection con = null;
        LOG.info("Usando H2");
        try {
            Class.forName(ORGH2_DRIVER).newInstance();
            con = DriverManager.getConnection(JDBCH2MEMTEST, H2USER, H2PASSWORD);
            LOG.info("Conexión establecida con la base de datos");
            RunScript.execute(con, new FileReader("src/main/resources/crearDb.sql"));
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return con;
    }

    private String cerrarConexion(Optional<Connection> con) {
        con.ifPresent(c -> {
            try {
                c.close();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        });
        return ("Conexión con la base de datos cerrada");
    }
}
