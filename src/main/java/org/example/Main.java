package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando procesamiento de ventas...");

        Map<String, Producto> mapaProductos = cargarProductos("productos.csv");
        Map<String, Vendedor> mapaVendedores = cargarVendedores("vendedores.csv");

        procesarArchivosVentas(mapaProductos, mapaVendedores);

        generarReporteProductos(mapaProductos);
        generarReporteVendedores(mapaVendedores);

        System.out.println("¡Reportes generados!");
    }

    private static Map<String, Producto> cargarProductos(String ruta) {
        Map<String, Producto> productos = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length >= 3) {
                    String id = datos[0].trim();
                    String nombre = datos[1].trim();
                    double precio = Double.parseDouble(datos[2].trim().replace(",", "."));
                    productos.put(id, new Producto(id, nombre, precio));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
        }
        return productos;
    }

    private static Map<String, Vendedor> cargarVendedores(String ruta) {
        Map<String, Vendedor> vendedores = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length >= 4) {
                    String tipoDoc = datos[0].trim();
                    String numDoc = datos[1].trim();
                    String nombres = datos[2].trim();
                    String apellidos = datos[3].trim();
                    vendedores.put(numDoc, new Vendedor(tipoDoc, numDoc, nombres, apellidos));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar vendedores: " + e.getMessage());
        }
        return vendedores;
    }

    private static void procesarArchivosVentas(Map<String, Producto> productos, Map<String, Vendedor> vendedores) {
        File carpetaRaiz = new File(".");
        File[] archivosVentas = carpetaRaiz.listFiles((dir, name) -> name.startsWith("vendedor_") && name.endsWith(".csv"));

        if (archivosVentas == null) return;

        for (File archivo : archivosVentas) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                String primeraLinea = br.readLine();
                if (primeraLinea == null) continue;

                String[] cabecera = primeraLinea.split(";");
                if (cabecera.length < 2) continue;
                String numDocVendedor = cabecera[1].trim();

                Vendedor vendedor = vendedores.get(numDocVendedor);

                String linea;
                while ((linea = br.readLine()) != null) {
                    String[] datosVenta = linea.split(";");
                    if (datosVenta.length >= 2) {
                        String idProducto = datosVenta[0].trim();
                        int cantidad = Integer.parseInt(datosVenta[1].trim());

                        Producto prod = productos.get(idProducto);
                        if (prod != null) {
                            prod.sumarCantidad(cantidad);
                            if (vendedor != null) {
                                vendedor.sumarVenta(cantidad * prod.getPrecio());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Registro omitido en: " + archivo.getName());
            }
        }
    }

    private static void generarReporteProductos(Map<String, Producto> productos) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_productos.csv"))) {
            for (Producto p : productos.values()) {
                pw.println(p.getNombre() + ";" + p.getCantidadVendida() + ";" + (p.getCantidadVendida() * p.getPrecio()));
            }
        } catch (IOException e) {
            System.err.println("Error en reporte productos: " + e.getMessage());
        }
    }

    private static void generarReporteVendedores(Map<String, Vendedor> vendedores) {
        List<Vendedor> lista = new ArrayList<>(vendedores.values());
        lista.sort((v1, v2) -> Double.compare(v2.getVentasTotales(), v1.getVentasTotales()));

        try (PrintWriter pw = new PrintWriter(new FileWriter("reporte_vendedores.csv"))) {
            for (Vendedor v : lista) {
                pw.println(v.getNombres() + " " + v.getApellidos() + ";" + v.getVentasTotales());
            }
        } catch (IOException e) {
            System.err.println("Error en reporte vendedores: " + e.getMessage());
        }
    }
}

// Clases de modelo unificadas en el mismo archivo
class Producto {
    private String id, nombre;
    private double precio;
    private int cantidadVendida = 0;

    public Producto(String id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public void sumarCantidad(int c) { this.cantidadVendida += c; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidadVendida() { return cantidadVendida; }
}

class Vendedor {
    private String tipoDoc, numDoc, nombres, apellidos;
    private double ventasTotales = 0.0;

    public Vendedor(String td, String nd, String n, String a) {
        this.tipoDoc = td;
        this.numDoc = nd;
        this.nombres = n;
        this.apellidos = a;
    }

    public void sumarVenta(double monto) { this.ventasTotales += monto; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public double getVentasTotales() { return ventasTotales; }
}
