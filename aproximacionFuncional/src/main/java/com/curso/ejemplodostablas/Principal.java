/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.ejemplodostablas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author usuario
 */
public class Principal {

    private static final String DB = "jdbc:mysql://localhost/test";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private Connection conexion = null;
    private static final Logger LOG = Logger.getLogger(Principal.class.getName());

    @FunctionalInterface
    interface Operacion<T> {

        T ejecutar(Connection c) throws SQLException;
    }

    public static void main(String[] args) {
        try {
            Principal p = new Principal();
            p.abrirConexion();
            p.enTransaccion(
                    c -> c.createStatement().executeQuery("select numero from telefonos where persona_id=1000")
            ).map(Stream::of)
                    .orElseThrow(RuntimeException::new)
                    .forEach(p::mostrarTelefonos);
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

    private void abrirConexion() {
        try {
            Class.forName(DRIVER).newInstance();
            conexion = DriverManager.getConnection(DB, "root", "");
            LOG.info("Conexión establecida con la base de datos");
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void cerrarConexion() throws SQLException {
        conexion.close();
        LOG.info("Conexión con la base de datos cerrada");
    }
}
