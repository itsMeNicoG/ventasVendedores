package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    // Clases modelo internas para mantener todo autocontenido en la clase Main
    static class Producto implements Comparable<Producto> {
        private String id;
        private String nombre;
        private double precioUnitario;
        private int cantidadTotalVendida;
        private double ventasTotales;

        public Producto(String id, String nombre, double precioUnitario) {
            this.id = id;
            this.nombre = nombre;
            this.precioUnitario = precioUnitario;
            this.cantidadTotalVendida = 0;
            this.ventasTotales = 0.0;
        }

        public void sumarVenta(int cantidad) {
            this.cantidadTotalVendida += cantidad;
            this.ventasTotales += cantidad * this.precioUnitario;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public double getPrecioUnitario() { return precioUnitario; }
        public int getCantidadTotalVendida() { return cantidadTotalVendida; }
        public double getVentasTotales() { return ventasTotales; }

        @Override
        public int compareTo(Producto o) {
            // Orden descendente por ventas totales
            return Double.compare(o.getVentasTotales(), this.getVentasTotales());
        }
    }

    static class Vendedor implements Comparable<Vendedor> {
        private String tipoDoc;
        private String numDoc;
        private String nombres;
        private String apellidos;
        private double ventasTotales;

        public Vendedor(String tipoDoc, String numDoc, String nombres, String apellidos) {
            this.tipoDoc = tipoDoc;
            this.numDoc = numDoc;
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.ventasTotales = 0.0;
        }

        public void sumarVenta(double monto) {
            this.ventasTotales += monto;
        }

        public String getTipoDoc() { return tipoDoc; }
        public String getNumDoc() { return numDoc; }
        public String getNombres() { return nombres; }
        public String getApellidos() { return apellidos; }
        public double getVentasTotales() { return ventasTotales; }

        @Override
        public int compareTo(Vendedor o) {
            // Orden descendente por ventas totales
            return Double.compare(o.getVentasTotales(), this.getVentasTotales());
        }
    }

    public static void main(String[] args) {
        try {
            // 1. Cargar catálogos principales
            Map<String, Producto> mapaProductos = cargarProductos("productos.csv");
            Map<String, Vendedor> mapaVendedores = cargarVendedores("vendedores.csv");

            // 2. Procesar los archivos de ventas individuales de cada vendedor
            procesarArchivosVentas(mapaProductos, mapaVendedores);

            // 3. Generar los reportes finales consolidados
            generarReporteProductos(mapaProductos);
            generarReporteVendedores(mapaVendedores);

            // Mensaje de éxito exigido
            System.out.println("Finalización exitosa: Reportes generados correctamente.");

        } catch (Exception e) {
            // Mensaje de error exigido
            System.err.println("Error en la ejecución: " + e.getMessage());
        }
    }

    private static Map<String, Producto> cargarProductos(String ruta) throws IOException {
        Map<String, Producto> productos = new HashMap<>();
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            throw new IOException("El archivo " + ruta + " no existe.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split(";");
                if (datos.length >= 3) {
                    try {
                        String id = datos[0].trim();
                        String nombre = datos[1].trim();
                        double precio = Double.parseDouble(datos[2].trim().replace(",", "."));
                        productos.put(id, new Producto(id, nombre, precio));
                    } catch (NumberFormatException ignored) {
                        // Tolerancia a fallos: omite registros de precio inválidos
                    }
                }
            }
        }
        return productos;
    }

    private static Map<String, Vendedor> cargarVendedores(String ruta) throws IOException {
        Map<String, Vendedor> vendedores = new HashMap<>();
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            throw new IOException("El archivo " + ruta + " no existe.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split(";");
                if (datos.length >= 4) {
                    String tipoDoc = datos[0].trim();
                    String numDoc = datos[1].trim();
                    String nombres = datos[2].trim();
                    String apellidos = datos[3].trim();
                    vendedores.put(numDoc, new Vendedor(tipoDoc, numDoc, nombres, apellidos));
                }
            }
        }
        return vendedores;
    }

    private static void procesarArchivosVentas(Map<String, Producto> mapaProductos, Map<String, Vendedor> mapaVendedores) {
        File directorioActual = new File(".");
        File[] archivos = directorioActual.listFiles((dir, name) -> name.startsWith("vendedor_") && name.endsWith(".csv"));

        if (archivos == null) return;

        for (File archivo : archivos) {
            String nombreArchivo = archivo.getName();
            // Extraer el número de documento del nombre del archivo (vendedor_[ID].csv)
            String numDoc = nombreArchivo.replace("vendedor_", "").replace(".csv", "");
            Vendedor vendedor = mapaVendedores.get(numDoc);

            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (linea.trim().isEmpty()) continue;
                    String[] datos = linea.split(";");
                    if (datos.length >= 2) {
                        try {
                            String idProducto = datos[0].trim();
                            int cantidad = Integer.parseInt(datos[1].trim());

                            Producto producto = mapaProductos.get(idProducto);
                            if (producto != null) {
                                producto.sumarVenta(cantidad);
                                if (vendedor != null) {
                                    vendedor.sumarVenta(cantidad * producto.getPrecioUnitario());
                                }
                            }
                        } catch (NumberFormatException ignored) {
                            // Tolerancia a fallos: omite filas con cantidad corrupta
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Advertencia al leer el archivo " + nombreArchivo + ": " + e.getMessage());
            }
        }
    }

    private static void generarReporteProductos(Map<String, Producto> mapaProductos) throws IOException {
        List<Producto> listaProductos = new ArrayList<>(mapaProductos.values());
        Collections.sort(listaProductos); // Ordena de mayor a menor ventas

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("reporte_productos.csv"))) {
            for (Producto p : listaProductos) {
                // Formato: NombreProducto;PrecioUnitario;CantidadTotalVendida;MontoTotalRecaudado
                String linea = String.format("%s;%.2f;%d;%.2f",
                        p.getNombre(),
                        p.getPrecioUnitario(),
                        p.getCantidadTotalVendida(),
                        p.getVentasTotales()).replace(",", ".");
                bw.write(linea);
                bw.newLine();
            }
        }
    }

    private static void generarReporteVendedores(Map<String, Vendedor> mapaVendedores) throws IOException {
        List<Vendedor> listaVendedores = new ArrayList<>(mapaVendedores.values());
        Collections.sort(listaVendedores); // Ordena de mayor a menor ventas

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("reporte_vendedores.csv"))) {
            for (Vendedor v : listaVendedores) {
                // Formato: Nombres;Apellidos;MontoTotalRecaudado
                String linea = String.format("%s;%s;%.2f",
                        v.getNombres(),
                        v.getApellidos(),
                        v.getVentasTotales()).replace(",", ".");
                bw.write(linea);
                bw.newLine();
            }
        }
    }
}