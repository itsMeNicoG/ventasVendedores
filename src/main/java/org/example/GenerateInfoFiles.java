package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateInfoFiles {

    public static void main(String[] args) {
        try {
            generarProductos();
            generarVendedores();
            generarVentas();
            System.out.println("Finalización exitosa: Archivos de prueba generados correctamente.");
        } catch (Exception e) {
            System.err.println("Error durante la generación de archivos: " + e.getMessage());
        }
    }

    private static void generarProductos() throws IOException {
        String[] nombres = {"Televisor", "Computador", "Teclado", "Mouse", "Monitor", "Audifonos", "Microfono", "Camara", "Impresora", "Router"};
        Random rand = new Random();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("productos.csv"))) {
            for (int i = 0; i < nombres.length; i++) {
                // ID;Nombre;Precio
                double precio = 50000 + (rand.nextDouble() * 2000000);
                writer.write((i + 1) + ";" + nombres[i] + ";" + String.format("%.2f", precio).replace(",", "."));
                writer.newLine();
            }
        }
    }

    private static void generarVendedores() throws IOException {
        String[] nombres = {"Carlos", "Maria", "Juan", "Ana", "Luis"};
        String[] apellidos = {"Gomez", "Perez", "Rodriguez", "Martinez", "Lopez"};
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vendedores.csv"))) {
            for (int i = 0; i < nombres.length; i++) {
                // TipoDoc;NumDoc;Nombres;Apellidos
                writer.write("CC;" + (10000000 + i) + ";" + nombres[i] + ";" + apellidos[i]);
                writer.newLine();
            }
        }
    }

    private static void generarVentas() throws IOException {
        Random rand = new Random();
        for (int i = 0; i < 5; i++) { // 5 Vendedores
            long numDoc = 10000000 + i;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("vendedor_" + numDoc + ".csv"))) {
                int cantidadVentas = rand.nextInt(10) + 1; // Entre 1 y 10 ventas por vendedor
                for (int j = 0; j < cantidadVentas; j++) {
                    int idProducto = rand.nextInt(10) + 1; // IDs de productos del 1 al 10
                    int cantidadVendida = rand.nextInt(5) + 1;
                    writer.write(idProducto + ";" + cantidadVendida);
                    writer.newLine();
                }
            }
        }
    }
}