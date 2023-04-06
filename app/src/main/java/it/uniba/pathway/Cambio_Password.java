package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.tntkhang.gmailsenderlibrary.GMailSender;
import com.github.tntkhang.gmailsenderlibrary.GmailListener;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class Cambio_Password extends AppCompatActivity {

    private int idMuseo=-1;
    private TextInputLayout password;
    private TextInputLayout confermaPassword;

    private String email="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_password);
        password = (TextInputLayout) findViewById(R.id.nuovaPassword);
        confermaPassword = (TextInputLayout) findViewById(R.id.confermaNuovaPassword);

        setExternalUserIdAndMail();

        if(savedInstanceState!=null)//questo serve a ripristinare i valori precedentemente salvati (in caso di landscape o portrait)
        {
            this.password.getEditText().setText(savedInstanceState.getString("password"));
            this.confermaPassword.getEditText().setText(savedInstanceState.getString("confermaPassword"));
            this.idMuseo=savedInstanceState.getInt("idMuseo");
            this.email=savedInstanceState.getString("Email");
        }

        //quando cliccato, disattiva l'errore se mostrato precedentemente
        password.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    password.setError(null);
                }
            }
        });

        confermaPassword.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    confermaPassword.setError(null);
                }
            }
        });

    }

    private void setExternalUserIdAndMail() {
        if(getIntent() != null && getIntent().getData() != null)
        {
            String[] variabili = getIntent().getDataString().split("&");
            //index=0 url | index=1 accounts.idMuseo | index=2 accounts.Email

            if(variabili.length==1)
            {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();//dopo che viene reinderizzato alla Homepage, qualcora il link non avesse parametri, l'activity del Cambio_password.java viene terminata
            }
            else
            {
                this.idMuseo = Integer.parseInt(variabili[1]);
                this.email = variabili[2];
            }
        }
    }

    //-1 Almeno uno dei due campi è vuoto
    //0 Le password inserite sono diverse
    //1 Le password inserite sono uguali e non vuote
    private int passwordChecker() {
        int FLAG=0;

        if(password.getEditText().getText().toString().trim().isEmpty() || confermaPassword.getEditText().getText().toString().trim().isEmpty())
        {
            FLAG = -1;
        }
        else if(password.getEditText().getText().toString().equals(confermaPassword.getEditText().getText().toString()))
        {
            FLAG = 1;
        }
        return FLAG;
    }

    public void cambioPassword(View view) {
        if(idMuseo!=-1)
        {

            int FLAG = passwordChecker();
            if(FLAG==1)
            {
                //1. query al db
                Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                    @Override
                    public void processFinish(String output) {
                        if(output=="true")
                        {
                            GMailSender.withAccount("pathway.sms.2021@gmail.com","esameSMS2021")
                                    .withTitle(getString(R.string.OggettoMailCambioPSW))
                                    .withBody(getString(R.string.ContenutoCambioPSW)+" "+Calendar.getInstance().getTime()+" "+getString(R.string.ContenutoCambioPSW2))
                                    .withSender(getString(R.string.app_name))
                                    .toEmailAddress(email)
                                    .withListenner(new GmailListener() {
                                        @Override
                                        public void sendSuccess() {
                                            Toast.makeText(Cambio_Password.this, getString(R.string.InvioMail), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void sendFail(String err) {
                                            Toast.makeText(Cambio_Password.this, getString(R.string.ErroreInvioMail), Toast.LENGTH_SHORT).show();
                                        }
                                    }).send();
                            apriActivityLogin();
                        }
                    }
                }).execute(Database.FLAG_UPDATE, "UPDATE accounts SET Password='"+password.getEditText().getText().toString()+"' where idMuseo = "+this.idMuseo);
            }
            else if(FLAG==-1)
            {
                Toast.makeText(Cambio_Password.this, R.string.campoMancante, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Cambio_Password.this, R.string.passwordNonCoincidenti, Toast.LENGTH_LONG).show();
                password.setError(getString(R.string.passwordNonCoincidenti));
                confermaPassword.setError(getString(R.string.passwordNonCoincidenti));
            }
        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!password.getEditText().getText().toString().isEmpty()) outState.putString("password",this.password.getEditText().getText().toString());
        if(!confermaPassword.getEditText().getText().toString().isEmpty()) outState.putString("confermaPassword",this.confermaPassword.getEditText().getText().toString());
        outState.putInt("idMuseo",this.idMuseo);
        outState.putString("Email",this.email);
    }

    private void apriActivityLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}