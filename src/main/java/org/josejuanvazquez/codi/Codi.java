package org.josejuanvazquez.codi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.josejuanvazquez.codi.cifrados.AES_CBC_128;
import org.josejuanvazquez.codi.servicios.ConexionServicios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Codi extends AppCompatActivity {

    byte[] keySource = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codi);

        //Consulta el ANDROID_ID
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        System.out.println("Valor de Android_id:" + android_id);

        EditText editTxt_AndroidId = (EditText) findViewById(R.id.editTxt_AndroidId);
        editTxt_AndroidId.setText(android_id);

        String appId = "org.josejuanvazquez.codi";
        EditText editTxt_AppId = (EditText) findViewById(R.id.editTxt_AppId);
        editTxt_AppId.setText(appId);

        final StringBuilder idH = new StringBuilder(android_id).append("-").append(appId);
        EditText editTxt_ANDROID_ID_AppId = (EditText) findViewById(R.id.editTxt_ANDROID_ID_AppId);
        editTxt_ANDROID_ID_AppId.setText(idH.toString());


        Button btnRegistroDispositivo = (Button) findViewById(R.id.bttn_RegistroDispositivo);
        btnRegistroDispositivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            StringBuilder json = new StringBuilder();
                            json.append("d={\"nc\":\"");
                            json.append(((EditText) findViewById(R.id.editTxt_NumeroCel)).getText());
                            json.append("\",\"idH\":\"");
                            json.append(idH.toString());
                            json.append("\",\"ia\":{\"so\":\"Android\",\"vSO\":\"7 Nougat\",\"fab\":\"HTC\",\"mod\":\"HTC 10\"}}");

                            String respuesta = new ConexionServicios().llamarServicio("https://www.banxico.org.mx/pagospei-beta/registroInicial", json.toString());

                            String[] parts = respuesta.split(",");
                            if (parts.length > 2) {
                                ((EditText) findViewById(R.id.editTxt_IdGoogle_Firebase)).setText(parts[0]);
                                ((EditText) findViewById(R.id.editTxt_DigVerif)).setText(parts[1]);
                            }

                            Log.i(this.getClass().getName(), "Respuesta: " + respuesta);
                        } catch (Exception e) {
                            Log.e(this.getClass().getName(), e.getMessage());
                        }
                    }
                });
                thread.start();
            }
        });

        //Proceso de Generacion de KeySource
        Button btnGeneracionKeySource = (Button) findViewById(R.id.bttn_GenerarKeySource);
        btnGeneracionKeySource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(this.getClass().getName(), "---Iniciando Proceso Generacion de KeySource---");

                String codR = ((EditText) findViewById(R.id.editTxt_OTP)).getText().toString(); //SMS
                String numeroCelular = ((EditText) findViewById(R.id.editTxt_NumeroCel)).getText().toString();

                Log.i(this.getClass().getName(), "CodR: " + codR);
                Log.i(this.getClass().getName(), "numeroCelular: " + numeroCelular);
                Log.i(this.getClass().getName(), "idHardware: " + idH.toString());

                byte[] sha512CodR = DigestUtils.sha512(codR);
                String sha512CodRHexa = new String(Hex.encodeHex(sha512CodR));//Asi se muestra en cadena Hexadecimal

                Log.i(this.getClass().getName(), "sha512CodR tamaño: " + sha512CodR.length);
                Log.i(this.getClass().getName(), "sha512CodR Hexa:" + sha512CodRHexa);

                /* Formas errones a de convertir a texto (Mal)
                new String(sha512CodR));
                Base64.encodeBase64(sha512CodR));
                new String(Base64.encodeBase64(sha512CodR)));
                android.util.Base64.encode(sha512CodR, android.util.Base64.DEFAULT));
                */

                //Concatenando cadenas
                StringBuilder sha512CodRIdHNumCelString = new StringBuilder();
                sha512CodRIdHNumCelString.append(sha512CodRHexa).append(idH.toString()).append(numeroCelular);

                Log.i(this.getClass().getName(), "Concatencacion: " + sha512CodRIdHNumCelString.toString());

                keySource = DigestUtils.sha512(sha512CodRIdHNumCelString.toString());
                String keySourceHexa = new String(Hex.encodeHex(keySource));//Asi se muestra en cadena Hexadecimal de keySource
                Log.i(this.getClass().getName(), "keySource tamaño: " + keySource.length);
                Log.i(this.getClass().getName(), "keySourceHexa:" + keySourceHexa);

                //editTxt_Keysource
                ((EditText) findViewById(R.id.editTxt_Keysource)).setText(keySourceHexa);
            }
        });

//Proceso de Desencripcion de cGoogleID
        Button btnDecryptCgoogleID = (Button) findViewById(R.id.bttn_DecryptCgoogleID);
        btnDecryptCgoogleID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(this.getClass().getName(), "---Iniciando Desencripcion de cGoogleID---");

                Log.i(this.getClass().getName(), "keySource leng: " + keySource.length);

                //Copiando 1ros 16 bytes
                byte[] key = new byte[16];
                System.arraycopy(keySource, 0, key, 0, 16);
                String heyHexa = new String(Hex.encodeHex(key));
                Log.i(this.getClass().getName(), "key tamaño: " + key.length);
                Log.i(this.getClass().getName(), "heyHexa Hexa:" + heyHexa);

                //Copiando 2dos 16 bytes
                byte[] vectorInicializacion = new byte[16];
                System.arraycopy(keySource, 16, vectorInicializacion,0, 16);
                String vectorInicializacionHexa = new String(Hex.encodeHex(vectorInicializacion));
                Log.i(this.getClass().getName(), "vectorInicializacion tamaño: " + vectorInicializacion.length);
                Log.i(this.getClass().getName(), "vectorInicializacionHexaHexa Hexa:" + vectorInicializacionHexa);

                //Nota: El gId esta expresadp en Hexadecimal, es decir es una cadena de 32 caracteres
                //Si estuviera en Base64 seria una cadena de 24 caracteres

                String cGoogleID = ((EditText) findViewById(R.id.editTxt_IdGoogle_Firebase)).getText().toString(); //id encriptado de Google enviado por Banxico

                String cGoogleIdDesencriptado = null;
                try {
                    AES_CBC_128 aes_cbc_128 = new AES_CBC_128();
//Inicio de Ejemplo
                    /*
                    Log.i(this.getClass().getName(), "Incio de prueba de desencipcion");
                    String llave = "92AE31A79FEEB2A3"; //llave
                    String iv = "0123456789ABCDEF"; // vector de inicialización
                    String cleartext = "hola";

                    String encriptado = aes_cbc_128.encrypt(llave, iv, cleartext);

                    Log.i(this.getClass().getName(), "encriptado: "+ encriptado);

                    byte[] llaveArray = llave.getBytes();
                    Log.i(this.getClass().getName(), "llave length: "+ llaveArray.length);
                    byte[] ivArray = iv.getBytes();
                    Log.i(this.getClass().getName(), "ivArray length: "+ ivArray.length);


                    String desencriptado = aes_cbc_128.decrypt(llaveArray,ivArray,encriptado );

                    Log.i(this.getClass().getName(), "desencriptado: "+ desencriptado);
                    Log.i(this.getClass().getName(), "Fin de prueba de desencipcion");
                    */
//Fin de Ejemplo

//Solicitud de desencripcion
                    cGoogleIdDesencriptado = aes_cbc_128.decrypt(key, vectorInicializacion, cGoogleID);
                    Log.i(this.getClass().getName(), "cGoogleIdDesencriptado: "+ cGoogleIdDesencriptado);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ((EditText) findViewById(R.id.editTxt_DecryptCgoogleID)).setText(cGoogleIdDesencriptado);
            }
        });


//Proceso de Registro en Firebase
        Button btnRegistroFirebase = (Button) findViewById(R.id.bttn_RegFirebase);
        btnRegistroFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String cGoogleID = "642578255008";//((EditText) findViewById(R.id.editTxt_IdGoogle_Firebase)).getText().toString(); //id encriptado de Google enviado por Banxico
                    String androidId = "195fdf0d6ce64575";//Dato Obtenido al registrar la app en FireBase
                    setInicializaAppSecundaria(cGoogleID, androidId);
                    new Token2().execute(cGoogleID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(Codi.this, "Notificacion 2", Toast.LENGTH_SHORT).show();
            }

        });

//Proceso del registro Subsecuente
        Button btnRegistroSubsecuenteDisp = (Button) findViewById(R.id.bttn_RegSubsecuenteDisp);
        btnRegistroSubsecuenteDisp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String dv = ((EditText) findViewById(R.id.editTxt_DigVerif)).getText().toString(); //digo de verificación de registro (dv) completado a 3 dígitos con ceros a la izquierda
                        String cGoogleID = ((EditText) findViewById(R.id.editTxt_IdGoogle_Firebase)).getText().toString(); //id encriptado de Google enviado por Banxico
                        String numeroCelular = ((EditText) findViewById(R.id.editTxt_NumeroCel)).getText().toString();
                        String idN = "";

                        Log.i(this.getClass().getName(), "Llamando servicio de registro subsecuente");


                        //keySource = Sha512Hex( Sha512Hex( codR ) || idH || nc )
                        //String keySource = new String(Hex.encodeHex(DigestUtils.sha512(sha512HexCodR_idH_Nc.toString())));
                        //Log.i(this.getClass().getName(), "keySource:");
                        //Log.i(this.getClass().getName(), keySource.toString());
                        byte[] keySourceSinHex = new byte[16];

                        //(nc || dv completado a 3 dígitos con ceros a la izquierda || IdN )
                        StringBuilder ncDvIdN = new StringBuilder();
                        ncDvIdN.append(numeroCelular).append(dv).append(idN);

                        try {
                            //var hmac = Hmac_Sha256( keySource, nc || dv completado a 3 dígitos con ceros a la izquierda || IdN )
                            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                            //SecretKeySpec secretKey = new SecretKeySpec(keySource.getBytes(), "HmacSHA256");

                            //Clave de 32 bytes para inicializar el algoritmo
                            byte[] clv32BytesIniciAlgoritm = new byte[32];
                            System.arraycopy(keySourceSinHex, 32, clv32BytesIniciAlgoritm, 0, 32);

                            SecretKeySpec secretKey = new SecretKeySpec(clv32BytesIniciAlgoritm, "HmacSHA256");
                            sha256_HMAC.init(secretKey);

                            byte[] previo = sha256_HMAC.doFinal(ncDvIdN.toString().getBytes());
                            Log.i(this.getClass().getName(), "previo leng: " + previo.length);

                            Log.i(this.getClass().getName(), "previo:");
                            Log.i(this.getClass().getName(), new String(previo));

                            String hmac = new String(Base64.encodeBase64(previo));
                            Log.i(this.getClass().getName(), "hmac:");
                            Log.i(this.getClass().getName(), hmac);

                            Integer digVerfi = new Integer(dv);

                            StringBuilder json = new StringBuilder();
                            json.append("d={\"numeroCelular\":\"");
                            json.append(((EditText) findViewById(R.id.editTxt_NumeroCel)).getText());
                            json.append("\",\"idHardware\":\"").append(idH.toString());
                            json.append("\",\"informacionAdicional\":{\"so\":\"Android\",\"versionSO\":\"7 Nougat\",\"fabricante\":\"HTC\",\"modelo\":\"HTC 10\"},");
                            json.append("\"idNotificaciones\":\"").append(((EditText) findViewById(R.id.editTxt_IdGoogle_Firebase)).getText()).append("\",");//
                            json.append("\"hmac\":\"").append(hmac).append("\",\"digitoVerificador\":").append(digVerfi);//
                            json.append("}");

                            String respuesta = new ConexionServicios().llamarServicio("https://www.banxico.org.mx/pagospei-beta/registroSubsecuente", json.toString());

                            //TextView txtvHmacRegSubsDisp =(TextView)findViewById(R.id.txtVw_HmacRegSubsDisp);
                            //txtvHmacRegSubsDisp.setText("HMAC: ");
                            Log.i(this.getClass().getName(), "Respuesta: " + respuesta);

                        } catch (Exception e) {
                            Log.e(this.getClass().getName(), e.getMessage());
                        }

                    }
                });
                thread.start();

            }
        });
    }


    /**
     * Metodo para registrar la app en google firebase
     *
     * @param cGoogleID Dato recivido de Banxico en el registro inicial
     * @param androidId Identificador del dispositivo
     */
    private void setInicializaAppSecundaria(String cGoogleID, String androidId) {

        Log.i(this.getClass().getName(), "--Registrando la app en Firebase--");
        //“1:cGoogleID:androidID:”

        StringBuilder applicationId = new StringBuilder("1:").append(cGoogleID).append(":android:").append(androidId);
        Log.i(this.getClass().getName(), "applicationId: " + applicationId.toString());

        FirebaseOptions.Builder firebaseOptions = new FirebaseOptions.Builder().setApplicationId(applicationId.toString());

        boolean haSidoInicializado = false;
        FirebaseApp myApp = null;

        List<FirebaseApp> lstFirebaseApps = FirebaseApp.getApps(this);
        for (FirebaseApp app : lstFirebaseApps) {
            Log.i(this.getClass().getName(), "app.getName(): " + app.getName());
            Log.i(this.getClass().getName(), "FirebaseApp.DEFAULT_APP_NAME: " + FirebaseApp.DEFAULT_APP_NAME);
            if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                haSidoInicializado = true;
                myApp = app;
                Log.i(this.getClass().getName(), "Inicializado myApp: " + myApp);
            }
        }
        Log.i(this.getClass().getName(), "haSidoInicializado: " + haSidoInicializado);

        if (!haSidoInicializado) {
            myApp = FirebaseApp.initializeApp(this, firebaseOptions.build());
            Log.i(this.getClass().getName(), "No Inicializado myApp2: " + myApp);
        }
    }

    /**
     *
     */
    private class Token2 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String token = null;
            try {
                Log.i(this.getClass().getName(), "parametro: " + params[0]);
                token = FirebaseInstanceId.getInstance().getToken(params[0], FirebaseMessaging.INSTANCE_ID_SCOPE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(this.getClass().getName(), "token: " + token);

            return null;
        }
    }
}
