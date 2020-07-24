package com.a2bsystem.vente;

import android.app.Activity;
import android.content.SharedPreferences;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.a2bsystem.vente.Activities.config.Config.Config;


public class Helper {

    // Vendeur
    public static String User;
    public static String Mdp;

    public static String vendeur;

    // Paramètres client
    public static int paramLot;
    public static String depot;
    public static int saisiePrix;
    public static int monoVente;
    public static int artOrLot; // 0 = art/num  1 = art/abc    2 = lot/num  3 = lot/abc
    public static int displayPA;
    public static int BlocagePUifDLCsup;



    // Génère les paramètres de la requêtes depuis la config
    public static RequestParams GenerateParams(Activity activity)
    {
        SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(Config, 0);
        String BDD = sharedPreferences.getString("BDD",null);
        String Foretagkod = sharedPreferences.getString("Foretagkod",null);
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("BDD", BDD);
        params.put("Foretagkod", Foretagkod);
        params.put("User", User);
        params.put("Mdp", Mdp);
        return params;
    }

    // Génère l'url
    public static String GenereateURI(Activity activity, RequestParams params, String route) {
        SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(Config, 0);
        String URL = sharedPreferences.getString("URL",null);

            return "http://"
                    + URL
                    + "/Vente/"
                    + route
                    + "?"
                    + params.toString();
    }

}
