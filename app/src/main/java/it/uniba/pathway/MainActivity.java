package it.uniba.pathway;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 0;
    private Button  reindirizzamentoHome;
    private String text;
    private String idMuseo="-1";
    private static String email="";
    private static String psw="";
    private boolean primoAvvio;
    private TextInputLayout nomeUtente;
    private TextInputLayout password;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView accediConGoogle;
    private ImageView testApp;
    Toast toast;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            loginSession(extras.getString("idGoogle"));
        }

        //reindirizzamentoHome è id del pulsante
        reindirizzamentoHome = (Button) findViewById(R.id.reindirizzamentoHome);
        nomeUtente = (TextInputLayout) findViewById(R.id.NomeUtente) ;
        password = (TextInputLayout) findViewById(R.id.Password);
        accediConGoogle = (TextView) findViewById(R.id.accediConGoogle);
        testApp = (ImageView) findViewById(R.id.pulsanteProvami);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Inizio del flusso per il login con google
        accediConGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.accediConGoogle:
                        signIn();
                        break;
                }
            }
        });

        testApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nomeUtente.getEditText().setText("trial@trial.com");
                password.getEditText().setText("trial");
                login();
            }
        });



        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        this.primoAvvio = prefs.getBoolean("prefs",true);

        if(savedInstanceState!=null)
        {
            this.nomeUtente.getEditText().setText(savedInstanceState.getString("nomeUtente"));
            this.password.getEditText().setText(savedInstanceState.getString("password"));
        }

        reindirizzamentoHome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { login(); }

        });


        password.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    login();
                    return true;
                }

                return false;
            }
        });


    //Controllo lo stato della connessione ogni 2.5secondi. Non appena torna la connessione, reindirizzo l'utente ad homepage se ha precedentemente
    // effettuato l'accesso. Altrimenti non succede nulla
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(reteInternetDisponibile(MainActivity.this))
                {
                    controllaSessione();
                }
            }
        }, 0, 2500);


    }

    /**
     * Si occupa di verificare le credenziali inserite
     * */
    private void login()
    {
        if(reteInternetDisponibile(MainActivity.this))
        {
            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {

                    if(!output.isEmpty())
                    {

                        String[] credenziali = output.split(",");
                        idMuseo = credenziali[0];
                        email = credenziali[1];
                        psw = credenziali[2];

                        if(idMuseo.equals("-1") || email.isEmpty() || psw.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.LoginCredentialsError, Toast.LENGTH_LONG).show();
                        }
                        else{
                            loginSession(idMuseo+"");
                        }
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, R.string.LoginCredentialsError, Toast.LENGTH_LONG).show();
                    }
                }
            }).execute(Database.FLAG_SELECT, "SELECT idMuseo, Email, Password FROM musei NATURAL JOIN accounts where Email = '"+nomeUtente.getEditText().getText()+"' AND Password = '"+password.getEditText().getText()+"'","3");
        }
        else
        {
            Toast.makeText(MainActivity.this, getString(R.string.missingInternet), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Controlla lo stato di rete
     *
     * @return true se informazioniRete è diverso da null E se il metodo isConnected() restituisce true. Altrimenti retistuisce false
     * */
    public static boolean reteInternetDisponibile(Context contesto)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) contesto.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo informazioniRete = connectivityManager.getActiveNetworkInfo();
        return informazioniRete != null && informazioniRete.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //controllo della sessione
        //questo metodo viene runnato ogni volta che si apre l'app
        //se l'utente è loggato --> lo si reindirizza alla home
        controllaSessione();

    }

    /**
     * Si occupa di controllare se l'utente abbia già effettuato l'accesso l'ultima volta che ha aperto l'app,
     * eventualmente verrà reinderizzato in Homepage.java
     */
    private void controllaSessione() {
        GestioneDellaSessione sessione = new GestioneDellaSessione(MainActivity.this);
        String idMuseo = sessione.getSessione();

        if(reteInternetDisponibile(MainActivity.this))
        {
            if(!idMuseo.equals("-1"))
            {
                timer.cancel();
                if(idMuseo.length() < 21)
                {
                //L'utente sarà loggato e riportato a Homepage.java
                    apriActivityHome();
                }
                else
                {
                    loginSession(idMuseo);
                }
            }
            else
            {
                if(this.primoAvvio == false)
                {
                    //if(toast == null) toast.makeText(MainActivity.this, R.string.ExpiredToken, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("prefs", false).commit();
                    this.primoAvvio = prefs.getBoolean("prefs",false);
                }

            }
        }

    }

    /**
     * Controlla se id passato appartiene ad un account Google, eventualmente per una più facile gestione, viene preso dal server
     * id del museo a cui è legato id di Google.
     *
     * Altrimenti se l'id passato appartiene ad un museo
     * popola la classe "Utente", salva la sessione e lo reindirizza alla Homepage
     *
     * @param   id Variabile di ingresso che indicherà l'id del museo
     */
    public void loginSession(String id) //in questo caso il nome assumerà come "valore" la mail, ma la sessione avrà sempre l'id del museo e il nome del suddetto
    {
        if(id.length()>=21)
        {

            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {

                    if(!output.isEmpty())
                    {

                        String[] credenziali = output.split(",");
                        idMuseo = credenziali[0];

                        if(idMuseo.isEmpty()) {
                            Toast.makeText(MainActivity.this, R.string.LoginCredentialsError, Toast.LENGTH_LONG).show();
                        }
                        else{ //Salvo la sessione

                            GestioneDellaSessione gestioneSessione = new GestioneDellaSessione(MainActivity.this);
                            gestioneSessione.salvataggioSessione(idMuseo);

                            apriActivityHome();
                        }
                    }
                    else
                    {
                        Intent intent = new Intent(MainActivity.this, Registrazione_Google.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("idGoogle",id);
                        startActivity(intent);
                    }
                }
            }).execute(Database.FLAG_SELECT, "SELECT idMuseo FROM musei WHERE idGoogle = '"+id+"'","1");
        }
        else
        {

            GestioneDellaSessione gestioneSessione = new GestioneDellaSessione(MainActivity.this);
            gestioneSessione.salvataggioSessione(id);

            apriActivityHome();
        }
    }


    //Avvio l'activity tramite la dichiarazione di un intent dove gli passo in contesto (dov'è stata azionata) e l'activity/Classe dove deve andare
    private void apriActivityHome() {
        Intent intent = new Intent(this, Homepage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void apriActivityRegistrazione(View view) {
        Intent intent = new Intent(this, Registrazione.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void apriActivityRecuperoPassword(View view) {
        Intent intent = new Intent(this, Recupero_Password.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!this.nomeUtente.getEditText().getText().toString().isEmpty()) outState.putString("nomeUtente",this.nomeUtente.getEditText().getText().toString());
        if(!this.password.getEditText().getText().toString().isEmpty()) outState.putString("password",this.password.getEditText().getText().toString());
    }

    /**
     * Crea una finestra che mostra i vari account Google presenti sul telefono
     * */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * In base al esito e alla scelta dell'account Google verrà inizializzato il metodo di accesso all'account Google
     * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Se l'accesso all'account Google va a buon fine viene passato un oggetto "account" di tipo "GoogleSignInAccount"
     * dal quale sarà possibile ottenere informazioni come l'id e la mail del account
     * */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(this, getString(R.string.Accesso_google), Toast.LENGTH_SHORT).show();
            loginSession(account.getId()+""); //salvo quale account ha scelto l'utente con cui fare la registrazione o l'accesso
        } catch (ApiException e) {
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }

}