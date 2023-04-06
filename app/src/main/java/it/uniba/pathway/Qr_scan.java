package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.Result;

import java.sql.SQLException;

public class Qr_scan extends AppCompatActivity {
    TextView Id;
    TextView Title;
    TextView Zone;
    String Z;
    CodeScanner codeScanner;
    CodeScannerView codeScannerView;
    Button Go;
    Button Retry;
    ProgressBar Progress;
    int num;
    int act=1;

    BottomNavigationView bottomNavigationView;
    public static String ID_MUSEO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        GestioneDellaSessione sessione = new GestioneDellaSessione(Qr_scan.this);
        ID_MUSEO = sessione.getSessione();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar);

        bottomNavigationView.setSelectedItemId(R.id.scanner);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.scanner:
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), Homepage.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.impostazioni:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }

                return false;
            }
        });


        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        Go = (Button) findViewById(R.id.go);
        Retry = (Button) findViewById(R.id.retry);
        Title = (TextView) findViewById(R.id.title);
        Zone = (TextView) findViewById(R.id.zone);
        Id = (TextView) findViewById(R.id.id);
        Progress = (ProgressBar) findViewById(R.id.progress);
        codeScannerView = (CodeScannerView) findViewById(R.id.scanner_view);

        codeScanner = new CodeScanner(this, codeScannerView);

        codeScanner.setDecodeCallback(new DecodeCallback() {

            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Progress.setVisibility(View.VISIBLE);
                        Id.setText(result.getText());
                        isValid((String) Id.getText());
                        Title.setVisibility(View.VISIBLE);
                        codeScannerView.setVisibility(View.GONE);
                    }
                });
            }
        });

        Title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewActivity();
            }
        });

        Zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewActivity();
            }
        });

        Go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewActivity();
            }
        });

        Retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCamera();
    }

    /**
     * Controlla che sia presente la connessione internet, altrimenti si viene reindirizzati nella schermata di login
     * dove periodicamente verrà controllato lo stato della connessione ogni 2.5 secondi.
     * <p>
     * Non appena la connessione tornerà a funzionare o l'utente cambia wifi con uno che abbia l'accesso ad internet, l'app riprenderà il suo workflow da Homepage.java
     */
    @Override
    protected void onStart() {
        super.onStart();
        if(!MainActivity.reteInternetDisponibile(this))
        {
            Toast.makeText(this, R.string.missing_connection,Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void requestCamera() {
        codeScanner.startPreview();
    }

    public void openNewActivity(){
        Intent intent = new Intent(this, OperaDettagliata.class);
        intent.putExtra("NUM",num);
        intent.putExtra("ACT",act);
        this.startActivity(intent);
    }


    public void isValid(String str) {

        boolean flag;
        try {
            try {
                Integer numberqr = Integer.parseInt(str);
                num = numberqr;
                flag = true;
            }catch (NumberFormatException e)
            {
                flag = false;
            }
            if(flag)
            {
                Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        try {
                            while (Database.rs.next()) {
                                Z = String.valueOf(Database.rs.getInt("idOpere"));
                            }
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        if(Z!=null){

                            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                                @Override
                                public void processFinish(String output) {
                                    try {
                                        while (Database.rs.next()) {

                                            Retry.setVisibility(View.VISIBLE);
                                            Go.setVisibility(View.VISIBLE);
                                            Progress.setVisibility(View.GONE);
                                            Title.setText(Database.rs.getString("Nome"));
                                            Zone.setText(Database.rs.getString("Zona"));
                                        }
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                            }).execute(Database.FLAG_SELECT_RAW, "SELECT Nome,Zona FROM opere where idOpere=" + num, "3");
                        }

                        else {
                            Title.setText(getResources().getString(R.string.codice_QR_non_valido));
                            Retry.setVisibility(View.VISIBLE);
                            Progress.setVisibility(View.GONE);
                        }
                    }
                }).execute(Database.FLAG_SELECT_RAW, "SELECT idOpere FROM opere where idOpere="+ num +" && idMuseo="+ ID_MUSEO,"2");

            }else
            {
                Title.setText(getResources().getString(R.string.codice_QR_non_valido));
                Retry.setVisibility(View.VISIBLE);
                Progress.setVisibility(View.GONE);
            }

        } catch(NumberFormatException e){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_scan_QR), Toast.LENGTH_SHORT).show();
        }
    }
}