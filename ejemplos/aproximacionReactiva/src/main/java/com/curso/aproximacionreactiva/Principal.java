/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.aproximacionreactiva;

import com.github.davidmoten.rx.jdbc.Database;
import java.io.FileNotFoundException;
import java.io.FileReader;
import static java.lang.System.out;
import java.sql.Connection;
import java.sql.SQLException;
import static java.util.Arrays.stream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.tools.RunScript;

/**
 *
 * @author usuario
 */
public class Principal implements ConstantesDb {

    private static final Logger LOG = Logger.getLogger(Principal.class.getName());
    private static final Consumer<Database> CONSULTA = db -> {
        db.select("select numero from telefonos where persona_id=?")
                .parameter(1000)
                .getAs(String.class)
                .subscribe(
                        out::println,
                        error -> out.println(error),
                        () -> out.println("Completado")
                );
        db.close();
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        stream(args).findFirst().map(Principal::obtenerDatabase).ifPresent(CONSULTA);
    }

    private static Database obtenerDatabase(String cual) {
        try {
            switch (cual.toLowerCase()) {
                case "mysql":
                    LOG.info("Usando MySql");
                    return Database.from(DB, USUARIO, CLAVE);
                case "h2":
                    LOG.info("Usando H2");
                    Database db = Database.from(JDBCH2MEMTEST, H2USER, H2PASSWORD);
                    Connection con = db.getConnectionProvider().get();
                    RunScript.execute(con, new FileReader("src/main/resources/crearDb.sql"));
                    return db;
                default:
                    throw new IllegalArgumentException("Base de datos desconocida");
            }
        } catch (FileNotFoundException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
