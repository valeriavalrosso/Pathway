package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.tntkhang.gmailsenderlibrary.GMailSender;
import com.github.tntkhang.gmailsenderlibrary.GmailListener;
import com.google.android.material.textfield.TextInputLayout;

public class Recupero_Password extends AppCompatActivity {

    private Button invioMailRecuperoBTN;
    private TextInputLayout emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recupero_password);
        emailField = (TextInputLayout) findViewById(R.id.Email);
        invioMailRecuperoBTN = (Button) findViewById(R.id.invioMailRecuperoBTN);

        if(savedInstanceState!=null)
        {
            this.emailField.getEditText().setText(savedInstanceState.getString("Email"));
        }

        invioMailRecuperoBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                    @Override
                    public void processFinish(String output) {

                        if(!output.isEmpty())
                        {

                            String[] credenziali = output.split(",");
                            String mail = credenziali[0];
                            String psw = credenziali[1];
                            int idMuseo = Integer.parseInt(credenziali[2]);

                            if(mail.isEmpty() || psw.isEmpty()) {
                                Toast.makeText(Recupero_Password.this,R.string.EmailMancante, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                sendMail(idMuseo,mail,psw);
                                apriActivityLogin();
                            }
                        }
                        else
                        {
                            Toast.makeText(Recupero_Password.this, R.string.EmailMancante, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute(Database.FLAG_SELECT, "SELECT Email, Password, idMuseo FROM accounts where Email = '"+emailField.getEditText().getText()+"'","3");
            }
        });
    }


    /**
     * Questo metodo permette l'invio della mail di recupero password sfruttando a sua volta alcuni metodi della libreria
     *
     * @param idMuseo
     * @param mail
     * @param password
     */
    private void sendMail(int idMuseo, String mail, String password)
    {
        GMailSender.withAccount("pathway.sms.2021@gmail.com","gootghjrlfvejhen")
                .withTitle(getString(R.string.OggettoMail))
                .withBody(getString(R.string.Contenuto)+" "+password+"\n\n"+getString(R.string.Contenuto2)+"&"+idMuseo+"&"+mail)
                .withSender(getString(R.string.app_name))
                .toEmailAddress(mail)
                .withListenner(new GmailListener() {
                    @Override
                    public void sendSuccess() {
                        Toast.makeText(Recupero_Password.this, getResources().getString(R.string.InvioMail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void sendFail(String err) {
                        Toast.makeText(Recupero_Password.this, getResources().getString(R.string.ErroreInvioMail), Toast.LENGTH_SHORT).show();
                    }
                }).send();
    }

    private void apriActivityLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!emailField.getEditText().getText().toString().isEmpty()) outState.putString("Email",this.emailField.getEditText().getText().toString());
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}