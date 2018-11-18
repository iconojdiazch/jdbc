/*
 * Principal.java
 *
 * Created on 22 de noviembre de 2006, 10:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.curso.usojdbcmysql;

import static java.lang.Class.forName;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import java.sql.CallableStatement;
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.sql.Types.INTEGER;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author user
 */
public class Principal {

    private static final String TABLAPERSONASDEF = "CREATE TABLE  personas ("
            + "id int(10) unsigned NOT NULL auto_increment,"
            + "nombre varchar(255) NOT NULL,"
            + "PRIMARY KEY  (id)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
    private static final String DB = "jdbc:mysql://localhost/test?user=root&password=root";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private Connection con = null;

    /**
     * Creates a new instance of Principal
     */
    public Principal() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Principal principal = new Principal();
            principal.abrirConexion();
            principal.crearTablaPersonas();
            for (int i = 0; i < 5; i++) {
                principal.ejecutarProcedimientoInsercion();
            }
            principal.mostrarPersonas();
            principal.ejecutarProcedimientoEntrada();
            principal.ejecutarProcedimientoSalida();
            principal.ejecutarProcedimientoMixto();
            principal.mostrarPersonas();
            principal.cerrarConexion();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            getLogger(Principal.class.getName()).log(SEVERE, null, ex);
        }
    }

    private void abrirConexion() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        forName(DRIVER).newInstance();
        con = getConnection(DB);
        out.println("Conexión establecida con la base de datos");
    }

    private void cerrarConexion() throws SQLException {
        con.close();
        out.println("Conexión con la base de datos cerrada");
    }

    private void crearTablaPersonas() throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS personas");
            st.executeUpdate(TABLAPERSONASDEF);
        }
    }

    private void ejecutarProcedimientoEntrada() throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DROP PROCEDURE IF EXISTS parametrosEntrada");
            st.executeUpdate("create procedure parametrosEntrada(IN p1 VARCHAR(255))\n" + "begin\n" + " select p.nombre from personas p where p.nombre like p1;\n" + "end\n");
        }
        try (CallableStatement cs = con.prepareCall("{call parametrosEntrada(?)}")) {
            out.println("Buscando personas cuyo nombre contenga la palabra \"cualquiera\"");
            cs.setString(1, "%cualquiera%");
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    out.println("Nombre:" + rs.getString(1));
                }
            }
        }
    }

    private void ejecutarProcedimientoSalida() throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DROP PROCEDURE IF EXISTS parametrosSalida");
            st.executeUpdate("create procedure parametrosSalida(out p1 int)\n" + "begin\n" + " select count(*) from personas into p1;\n" + "end\n");
        }
        try (CallableStatement cs = con.prepareCall("{call parametrosSalida(?)}")) {
            cs.registerOutParameter(1, INTEGER);
            cs.executeUpdate();
            int resultado = cs.getInt(1);
            out.printf("Hay %d personas en la base de datos%n", resultado);
        }
    }

    private void ejecutarProcedimientoInsercion() throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DROP PROCEDURE IF EXISTS insertarPersona");
            st.executeUpdate("create procedure insertarPersona(in p1 varchar(255))\n" + "begin\n" + " insert into personas (nombre) values(p1);\n" + "end\n");
        }
        try (CallableStatement cs = con.prepareCall("{call insertarPersona(?)}")) {
            cs.setString(1, "persona cualquiera" + currentTimeMillis());
            cs.executeUpdate();
        }
    }

    private void mostrarPersonas() throws SQLException {
        try (Statement st = con.createStatement()) {
            out.println("Personas existentes en la base de datos");
            try (ResultSet rs = st.executeQuery("select nombre from personas")) {
                while (rs.next()) {
                    out.println("Nombre: " + rs.getString(1));
                }
            }
        }
    }

    private void ejecutarProcedimientoMixto() throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DROP PROCEDURE IF EXISTS procedimientoMixto");
            st.executeUpdate("create procedure procedimientoMixto(in p1 varchar(255))\n" + "begin\n" + " insert into personas (nombre) values(p1);\n" + " select nombre from personas;\n" + "end\n");
        }
        try (CallableStatement cs = con.prepareCall("{call procedimientoMixto(?)}")) {
            cs.setString(1, "persona desde proc mixto" + currentTimeMillis());
            boolean r = cs.execute();
            if (r) {
                out.println("El primer resultado es un ResultSet");
                ResultSet rs = cs.getResultSet();
                while (rs.next()) {
                    out.println("Nombre: " + rs.getString(1));
                }
                boolean hayMasResultSet = cs.getMoreResults();
                if (hayMasResultSet) {
                    out.println("Hay más ResultSets");
                } else {
                    out.println("El siguiente resultado no es un ResultSet o no hay más resultados");
                    int nFilas = cs.getUpdateCount();
                    out.println("Filas afectadas:" + nFilas);
                }
                if ((!cs.getMoreResults()) && (cs.getUpdateCount() == -1)) {
                    out.println("No hay más resultados que mostrar");
                } else {
                    out.println("Hay más resultados que mostrar");
                }
            } else {
                out.println("El primer resultado no es un ResultSet o no hay resultados");
            }
        }
    }
}
