/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.ejemploblob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author usuario
 */
public class Principal implements ConstantesDb {

    private static final Logger LOG = Logger.getLogger(Principal.class.getName());
    private static final DataSource DS = obtenerDataSource();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        prepararBd();
        insertarBlobClob();
        leerBlobClob();
    }

    private static void insertarBlobClob() {
        final String orden
                = "insert into personasfoto values(1000,'primer nombre',?,?)";
        Principal p = new Principal();
        try (Connection c = DS.getConnection();
                InputStream foto = p.getClass().getClassLoader().getResourceAsStream("icono.png");
                InputStream documento = p.getClass().getClassLoader().getResourceAsStream("ManualSQL.pdf")) {
            PreparedStatement ps = c.prepareStatement(orden);
            ps.setBinaryStream(1, foto);
            ps.setBinaryStream(2, documento);
            ps.executeUpdate();
            LOG.info("Foto y documento pdf insertados correctamente");
        } catch (SQLException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void leerBlobClob() {
        final String orden
                = "select foto, documento from personasfoto where id = ?";
        final String nombreArchivoFoto = "iconoLeido.png";
        final String nombreArchivoDoc = "manualSqlLeido.pdf";
        try (Connection c = DS.getConnection();
                FileOutputStream foto = new FileOutputStream(nombreArchivoFoto);
                FileOutputStream doc = new FileOutputStream(nombreArchivoDoc)) {
            PreparedStatement ps = c.prepareStatement(orden);
            ps.setInt(1, 1000);
            ResultSet rs = ps.executeQuery();
            LOG.log(Level.INFO, "Escribiendo en {0} y en {1}", new Object[]{new File(nombreArchivoFoto).getAbsolutePath(), new File(nombreArchivoDoc).getAbsolutePath()});
            while (rs.next()) {
                InputStream input = rs.getBinaryStream("foto");
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    foto.write(buffer);
                }
                input = rs.getBinaryStream("documento");
                buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    doc.write(buffer);
                }
            }
            LOG.info("Foto y documento recuperados correctamente");
        } catch (SQLException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void prepararBd() {
        final String orden = "delete from personasfoto";        
        try (Connection c = DS.getConnection()) {
            c.createStatement().executeUpdate(orden);
            LOG.info("Datos existentes en la tabla 'personasfoto' eliminados");
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static DataSource obtenerDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(DRIVER);
        ds.setUrl(DB);
        ds.setUsername(USUARIO);
        ds.setPassword(CLAVE);
        return ds;
    }
}
