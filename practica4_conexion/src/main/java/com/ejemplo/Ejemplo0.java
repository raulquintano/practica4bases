package com.ejemplo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Ejemplo0 {

    public static void main(String[] args) {
        String csv = "C:/Users/raulq/Documents/2ºCarrera/BasesDeDatos II/Practica04/padron_espana.csv"; // Usa ruta relativa
        int batchSize = 100;

        Connection conexion = null;
        PreparedStatement statement = null;
        BufferedReader lector = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String mysqlURL = "jdbc:mysql://localhost:3306/practica4";
            conexion = DriverManager.getConnection(mysqlURL, "root", "1234");
            conexion.setAutoCommit(false);
            System.out.println("Autocommit: " + conexion.getAutoCommit()); // Verifica autocommit

            String sql = "INSERT INTO datos_demograficos (total_nacional, comunidad_autonoma, provincia, españoles_extranjeros, edad, sexo, periodo, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = conexion.prepareStatement(sql);
            lector = new BufferedReader(new FileReader(csv));

            String linea;
            int contador = 0;
            lector.readLine();

            long inicioTemp = System.nanoTime();

            while ((linea = lector.readLine()) != null) {
                String[] datos = linea.split("\t");

                if (datos.length < 8) {
                    System.out.println("Línea inválida, saltando: " + linea);
                    continue;
                }

                try {
                    statement.setString(1,datos[0]);
                    statement.setString(2, datos[1]);
                    statement.setString(3, datos[2]);
                    statement.setString(4, datos[3]);
                    statement.setString(5, datos[4]);
                    statement.setString(6, datos[5]);
                    statement.setString(7, datos[6]);
                    statement.setString(8, datos[7]);

                    statement.addBatch();

                    if (++contador % batchSize == 0) {
                        statement.executeBatch();
                    }
                } catch (NumberFormatException | SQLException e) {
                    System.err.println("Error al procesar la línea: " + linea + ". Error: " + e.getMessage());
                    conexion.rollback(); // Rollback en error de formato o SQL
                    return; // Termina el proceso de inserción
                }
            }

            statement.executeBatch();
            conexion.commit(); // Commit si todo va bien

            long finTemp = System.nanoTime();
            long tiempoTotal = (finTemp - inicioTemp) / 1_000_000;
            System.out.println("Datos insertados con éxito.");
            System.out.println("El tiempo de carga fue de: " + tiempoTotal + " ms");

        } catch (ClassNotFoundException | SQLException | IOException ex) {
            ex.printStackTrace();
            if (conexion != null) {
                try {
                    conexion.rollback(); // Rollback en error de conexión o E/S
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            // Asegura que la conexión, el statement y el lector se cierren
            try {
                if (statement != null) {
                    statement.close();
                }
                if (conexion != null) {
                    conexion.close();
                }
                if (lector != null) {
                    lector.close();
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}