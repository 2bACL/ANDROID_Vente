package com.a2bsystem.vente.Activities.connection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.a2bsystem.vente.Activities.config.Config;
import com.a2bsystem.vente.Activities.listeventes.VentesList;
import com.a2bsystem.vente.Helper;
import com.a2bsystem.vente.R;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    // Composants XML
    BottomNavigationView bottomNavigationView;
    EditText eUser;
    EditText eMdp;


    ProgressBar pbbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFields();
        initListeners();

    }

    public void initFields() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_main);
        eUser = findViewById(R.id.main_user);
        eMdp = findViewById(R.id.main_mdp);
        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);
        eUser.setText("");
        eMdp.setText("");
    }

    public void initListeners() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.deco_onglet:

                        setDeconnect();

                        break;

                    case R.id.config_onglet:

                        Intent configActivity = new Intent(MainActivity.this, Config.class);
                        startActivity(configActivity);

                        break;
                    case R.id.connec_onglet:

                        setConnect();

                        break;
                }
                return false;
            }
        });
    }


    private void lockUI()
    {
        bottomNavigationView.setEnabled(false);
        eMdp.setEnabled(false);
        eUser.setEnabled(false);
    }

    private void unlockUI()
    {
        bottomNavigationView.setEnabled(true);
        eMdp.setEnabled(true);
        eUser.setEnabled(true);
    }

    public void showError(String message, DialogInterface.OnClickListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Erreur");
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", listener);
        builder.show();
    }


    private void setDeconnect() {
        if(!eUser.getText().toString().equalsIgnoreCase("") && !eMdp.getText().toString().equalsIgnoreCase("")){

            Helper.User = eUser.getText().toString();
            Helper.Mdp = eMdp.getText().toString();

            RequestParams params = Helper.GenerateParams(MainActivity.this);
            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            params.put("PhoneID",android_id);


            String URL = Helper.GenereateURI(MainActivity.this, params, "deconnect");

            Deconnect task = new Deconnect();
            task.execute(new String[] { URL });
        }
    }

    private void setConnect() {

        // Save logs
        Helper.User = eUser.getText().toString();
        Helper.Mdp = eMdp.getText().toString();

        pbbar.setVisibility(View.VISIBLE);

        // Construction de l'URL
        RequestParams params = Helper.GenerateParams(MainActivity.this);
        String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        params.put("PhoneID",android_id);
        String URL = Helper.GenereateURI(MainActivity.this, params, "connection");

        //Verouillage de l'interface
        lockUI();

        // Call API JEE
        Connect task = new Connect();
        task.execute(new String[] { URL });

    }



    private class Connect extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return output.toString();
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String output) {
            unlockUI();
            pbbar.setVisibility(View.GONE);
            if(output.equalsIgnoreCase("-1"))
            {
                showError("Identifiants incorrects", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eUser.setText("");
                        eMdp.setText("");
                    }
                });
            }
            else {
                try {

                    JSONObject jsonObject = new JSONObject(output);


                    Helper.paramLot = jsonObject.getInt("ParamLot");
                    Helper.depot = jsonObject.getString("Depot");
                    Helper.monoVente = jsonObject.getInt("MonoVente");
                    Helper.saisiePrix = jsonObject.getInt("SaisiePrix");
                    Helper.vendeur = jsonObject.getString("Vendeur");
                    Helper.artOrLot = jsonObject.getInt("ArtOrLot");
                    Helper.displayPA = jsonObject.getInt("DisplayPA");
                    Helper.BlocagePUifDLCsup = jsonObject.getInt("BlocagePUifDLCsup");


                    if(jsonObject.getInt("Connexion") == 1) {
                        Intent VentesList = new Intent(MainActivity.this, VentesList.class);
                        startActivity(VentesList);
                    }
                    else {
                        showError("Nombre de licences maximum dépassé", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Connexion impossible", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            eUser.setText("");
                            eMdp.setText("");
                        }
                    });
                    Helper.displayPA = 1;
                    Helper.BlocagePUifDLCsup = 0;
                }

            }
        }
    }


    private class Deconnect extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return output.toString();
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }

        @Override
        protected void onPostExecute(String output) {
            unlockUI();
            pbbar.setVisibility(View.GONE);
            if(output.equalsIgnoreCase("-1"))
            {
                showError("Identifiants incorrects", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eUser.setText("");
                        eMdp.setText("");
                    }
                });
            }
            else {
                MainActivity.this.finish();
            }
        }
    }

}
