
package es.gva.edu.iesjuandegaray.valenbiciv2;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class DatosJSon {
    private static String API_URL;
    private String datos = "";
    private int numEst;
    private String[][] values;

    public DatosJSon(int nE) {
        this.numEst = nE;
        this.datos = "";
        generarUrl();
    }

    private void generarUrl() {
        API_URL = "https://valencia.opendatasoft.com/api/explore/v2.1/catalog/datasets/valenbisi-disponibilitat-valenbisi-dsiponibilidad/records?f=json&location=39.46447,-0.39308&distance=10&limit=" + numEst;
    }

    public void mostrarDatos() {
        datos = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(API_URL);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity);
                System.out.println(result);
                JSONObject jsonObject = new JSONObject(result);
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                values = new String[resultsArray.length()][6];

                for (int i = 0; i < resultsArray.length(); i++) {
                    try {
                        JSONObject est = resultsArray.getJSONObject(i);
                        String direccion = est.getString("address");
                        int estacion_id = est.getInt("number");
                        int disponibles = est.optInt("available_bikes", 0);
                        int anclajes = est.optInt("available_bike_stands", 0);
                        String estado = est.getString("open");

                        JSONObject geo = est.optJSONObject("geo_point_2d");
                        double lat = (geo != null) ? geo.getDouble("lat") : 0;
                        double lon = (geo != null) ? geo.getDouble("lon") : 0;

                        datos += String.format("Estación: %d - Dirección: %s - Bicis: %d - Anclajes: %d - Estado: %s\n",
                                estacion_id, direccion, disponibles, anclajes, estado);

                        values[i][0] = String.valueOf(estacion_id);
                        values[i][1] = direccion;
                        values[i][2] = String.valueOf(disponibles);
                        values[i][3] = String.valueOf(anclajes);
                        values[i][4] = estado.equals("T") ? "1" : "0";
                        values[i][5] = lat + " " + lon;
                    } catch (Exception ex) {
                        datos += " Error procesando estación " + i + ": " + ex.getMessage() + "\n";
                    }
                }
                setDatos(datos);
            }
        } catch (IOException | org.json.JSONException e) {
            datos = "Error al obtener o procesar datos: " + e.getMessage();
        }
        System.out.println("DEBUG DATOS FINAL:\n" + datos);

    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

    public String[][] getValues() {
        return values;
    }
}
