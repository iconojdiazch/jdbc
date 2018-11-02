/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.curso.aproximacionreactiva;

import com.github.davidmoten.rx.jdbc.Database;
import static java.lang.System.out;

/**
 *
 * @author usuario
 */
public class Principal implements ConstantesDb {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Database db = Database.from(DB, USUARIO, CLAVE);
        db
                .select("select numero from telefonos where persona_id=?")
                .parameter(1000)
                .getAs(String.class)
                .subscribe(
                        out::println,
                        error -> out.println(error),
                        ()->out.println("Completado")
                );
        db.close();        
    }

}
