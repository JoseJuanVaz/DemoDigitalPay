package org.josejuanvazquez.codi.servicios;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by José Juan Vázquez on 29/11/2018.
 */

public class ConexionServicios {

    /**
     *
     * @param recurso https://www.banxico.org.mx/pagospei-beta/registroInicial
     * @param json d={"numeroCelular":"5543599472","idHardware":"dd0845bac942f11b-com.josejuanvazquez.infocodi","informacionAdicional":{"so":"Android","versionSO":"7 Nougat","fabricante":"HTC","modelo":"HTC 10"}}
     * @return String con el resultado de la peticion
     */
    public String llamarServicio(String recurso, String json){
        Log.i(this.getClass().getName(), "Iniciando llamado de servicio: "+recurso);
        Log.i(this.getClass().getName(), "json: "+json);

        String output = null;
        try {

            URL url = new URL(recurso);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain");//application/json

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();

            //Revisar
           /*
            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                           throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
           }*/

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));


            Log.i(this.getClass().getName(), "Respuesta del servicio: .... \n");
            StringBuilder respuesta = new StringBuilder();
            while ( (output = br.readLine()) != null) {
                Log.i(this.getClass().getName(), output);
                respuesta.append(output);
            }
            conn.disconnect();

            if (respuesta.length() > 0) {
                output = respuesta.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }
}
