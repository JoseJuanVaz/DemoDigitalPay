package org.josejuanvazquez.codi.servicios;



import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by José Juan Vázquez on 04/12/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

private static final String LOGTAG = "MyFInstanceIDService";

    @Override
    public void onTokenRefresh(){
        //Se obtiene el token actualizado
        try{
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.d(LOGTAG, "Token actualizado: " + refreshedToken);
        }catch (Exception e){
            Log.d(LOGTAG, "Token actualizado error");
        }
    }
}
