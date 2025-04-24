
package es.gva.edu.iesjuandegaray.valenbiciv2;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConexionBDD extends JFrame {

    private DatosJSon dJSon;

    private JLabel jLabelTitulo, jLabelDatosEstaciones, jLabelAddBDD;
    private JTextField jTextFieldNumEst;
    private JTextArea jTextArea1;
    private JButton jButtonConectarBDD, jButtonDatos, jButtonAddBDD, jButtonCerrarCxBDD;

    private static Connection con;
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/valenbicibd";
    private static final String user = "root";
    private static final String pass = "mysql123";

    public ConexionBDD() {
        setTitle("Conexión a Base de Datos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelSuperior = new JPanel(new FlowLayout());
        jLabelTitulo = new JLabel("Conexión a Base de Datos");
        jLabelTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelSuperior.add(jLabelTitulo);

        JPanel panelCentro = new JPanel(new GridLayout(4, 1, 10, 10));
        JPanel panelInputs = new JPanel();
        panelInputs.add(new JLabel("Número de estaciones:"));
        jTextFieldNumEst = new JTextField(5);
        panelInputs.add(jTextFieldNumEst);

        JPanel panelBotones = new JPanel();
        jButtonConectarBDD = new JButton("Conectar");
        jButtonDatos = new JButton("Mostrar Datos");
        jButtonAddBDD = new JButton("Añadir a BDD");
        jButtonCerrarCxBDD = new JButton("Cerrar Conexión");

        panelBotones.add(jButtonConectarBDD);
        panelBotones.add(jButtonDatos);
        panelBotones.add(jButtonAddBDD);
        panelBotones.add(jButtonCerrarCxBDD);

        panelCentro.add(panelInputs);
        panelCentro.add(panelBotones);

        JPanel panelLabelDatos = new JPanel();
        jLabelDatosEstaciones = new JLabel("Datos estaciones:");
        panelLabelDatos.add(jLabelDatosEstaciones);
        panelCentro.add(panelLabelDatos);

        jTextArea1 = new JTextArea(10, 50);
        JScrollPane scrollPane = new JScrollPane(jTextArea1);

        jLabelAddBDD = new JLabel("");

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
//        add(scrollPane, BorderLayout.SOUTH);
        panelCentro.add(scrollPane); //
        add(jLabelAddBDD, BorderLayout.PAGE_END);

        jButtonConectarBDD.addActionListener(e -> conector());

        jButtonDatos.addActionListener(e -> {
            try {
                int n = Integer.parseInt(jTextFieldNumEst.getText());
                dJSon = new DatosJSon(n);
                dJSon.mostrarDatos();
                jTextArea1.setText(dJSon.getDatos());
                jLabelDatosEstaciones.setText("Datos de estaciones:");
            } catch (NumberFormatException ex) {
                jLabelAddBDD.setText("Introduce un número válido.");
            }
        });

        jButtonAddBDD.addActionListener(e -> {
            if (dJSon == null || con == null) {
                jLabelAddBDD.setText("Conexión o datos no disponibles.");
                return;
            }

            String[][] datos = dJSon.getValues();
            String insertSQL = "INSERT INTO historico (estacion_id, direccion, bicis_disponibles, anclajes_libres, estado_operativo, ubicación) VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?))";

            try {
                PreparedStatement ps = con.prepareStatement(insertSQL);

                for (String[] fila : datos) {
                    ps.setInt(1, Integer.parseInt(fila[0]));
                    ps.setString(2, fila[1]);
                    ps.setInt(3, Integer.parseInt(fila[2]));
                    ps.setInt(4, Integer.parseInt(fila[3]));
                    ps.setBoolean(5, fila[4].equals("1"));
                    ps.setString(6, "POINT(" + fila[5] + ")");
                    ps.executeUpdate();
                }

                jLabelAddBDD.setText("Datos insertados correctamente.");
            } catch (SQLException ex) {
                jLabelAddBDD.setText("Error al insertar datos: " + ex.getMessage());
                ex.printStackTrace();
            }
        });


        jButtonCerrarCxBDD.addActionListener(e -> {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                    jLabelAddBDD.setText("Conexión cerrada correctamente.");
                    System.out.println("Conexión cerrada correctamente.");
                } else {
                    jLabelAddBDD.setText("No hay conexión activa.");
                }
            } catch (SQLException ex) {
                jLabelAddBDD.setText("Error al cerrar la conexión: " + ex.getMessage());
                ex.printStackTrace();
            }
        });


        setVisible(true);
    }

    public void conector() {
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, pass);
            jLabelAddBDD.setText("Conexión establecida correctamente.");
            System.out.println("Conectado a la base de datos.");
        } catch (ClassNotFoundException | SQLException e) {
            jLabelAddBDD.setText("Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConexionBDD::new);
    }
}
