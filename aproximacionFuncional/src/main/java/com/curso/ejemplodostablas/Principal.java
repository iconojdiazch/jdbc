/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.ejemplodostablas;

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
public class Principal {

    private static final String DB = "jdbc:mysql://localhost/test";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private Connection conexion = null;
    private static final Logger LOG = Logger.getLogger(Principal.class.getName());
    private static final String H2PASSWORD = "";
    private static final String H2USER = "sa";
    private static final String JDBCH2MEMTEST = "jdbc:h2:mem:test";
    private static final String ORGH2_DRIVER = "org.h2.Driver";

    @FunctionalInterface
    interface Operacion<T> {

        T ejecutar(Connection c) throws SQLException;
    }

    public static void main(String[] args) {
        try {
            Principal p = new Principal();
            stream(args).findFirst().ifPresent(p::crearConexion);
            p.enTransaccion(
                    c -> c.createStatement().executeQuery("select numero from telefonos where persona_id=1000")
            ).ifPresent(p::mostrarTelefonos);
            p.cerrarConexion();
            System.out.println("Fin");
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Error: ", ex);
        }
    }

    private Boolean mostrarTelefonos(ResultSet rs) {
        try {
            while (rs.next()) {
                System.out.println("Teléfono: " + rs.getString("numero"));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error en mostrarDatos: {0}", e);
        }
        return true;
    }

    private <T> Optional<T> enTransaccion(Operacion<T> op) {
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

    private void crearConexion(String cual) {
        switch (cual.toLowerCase()) {
            case "mysql":
                abrirConexionMySql();
                break;
            case "h2":
                abrirConexionH2();
                break;
            default:
                throw new IllegalArgumentException("Basa de datos desconocida");
        }
    }

    private void abrirConexionMySql() {
        LOG.info("Usando MySql");
        try {
            Class.forName(DRIVER).newInstance();
            conexion = DriverManager.getConnection(DB, "root", "");
            LOG.info("Conexión establecida con la base de datos");
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void abrirConexionH2() {
        LOG.info("Usando H2");
        try {
            Class.forName(ORGH2_DRIVER).newInstance();
            conexion = DriverManager.getConnection(JDBCH2MEMTEST, H2USER, H2PASSWORD);
            LOG.info("Conexión establecida con la base de datos");
            RunScript.execute(conexion, new FileReader("src/main/resources/crearDb.sql"));
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cerrarConexion() throws SQLException {
        conexion.close();
        LOG.info("Conexión con la base de datos cerrada");
    }
}
