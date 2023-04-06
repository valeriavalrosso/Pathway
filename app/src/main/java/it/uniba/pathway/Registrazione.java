package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class Registrazione extends AppCompatActivity {

    private TextInputLayout nomeMuseo;
    private TextInputLayout email;
    private TextInputLayout password;
    private TextInputLayout confermaPassword;
    private TextInputLayout telefono;
    private TextInputLayout provincia;
    private TextInputLayout citta;
    private TextInputLayout CAP;
    private TextInputLayout indirizzo;
    private TextInputLayout partitaIVA;
    private ArrayList <TextInputLayout> campo = new ArrayList<>();
    private ArrayList <String> nomeCampo = new ArrayList<>();
    private boolean risultato = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);

        nomeMuseo = (TextInputLayout) findViewById(R.id.NomeUtente);
        email = (TextInputLayout) findViewById(R.id.Email);
        password = (TextInputLayout) findViewById(R.id.password);
        confermaPassword = (TextInputLayout) findViewById(R.id.confermaPassword);
        telefono = (TextInputLayout) findViewById(R.id.telefono);
        provincia = (TextInputLayout) findViewById(R.id.provincia);
        citta = (TextInputLayout) findViewById(R.id.citta);
        CAP = (TextInputLayout) findViewById(R.id.CAP);
        indirizzo = (TextInputLayout) findViewById(R.id.indirizzo);
        partitaIVA = (TextInputLayout) findViewById(R.id.partitaIVA);

        campo.add(nomeMuseo); nomeCampo.add("nomeMuseo");
        campo.add(email);     nomeCampo.add("email");
        campo.add(password);  nomeCampo.add("password");
        campo.add(confermaPassword); nomeCampo.add("confermaPassword");
        campo.add(telefono);  nomeCampo.add("telefono");
        campo.add(provincia); nomeCampo.add("provincia");
        campo.add(citta);     nomeCampo.add("citta");
        campo.add(CAP);       nomeCampo.add("CAP");
        campo.add(indirizzo); nomeCampo.add("indirizzo");

        nomeCampo.add("partitaIVA");



        for(int i=0;i<campo.size();i++)
        {
            int finalI = i;
            campo.get(i).getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    campo.get(finalI).setError(null);
                }
            });
        }

        if(savedInstanceState!=null)//questo serve a ripristinare i valori precedentemente salvati (in caso di landscape o portrait)
        {
            for(int i=0; i< campo.size();i++)
            {
                campo.get(i).getEditText().setText(savedInstanceState.getString(nomeCampo.get(i),""));
            }
            partitaIVA.getEditText().setText(savedInstanceState.getString(nomeCampo.get(9),""));
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

    public boolean emailChecker(String email)
    {
        try {
            InternetAddress indirizzoEmail = new InternetAddress(email);
            indirizzoEmail.validate();
            risultato = true;
        }catch(AddressException e)
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.email_non_validata), Toast.LENGTH_SHORT).show();
            risultato = false;
        }
        return risultato;
    }

    public void emailCheckerDB(String email)
    {
        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output)
            {
                try
                {
                    while (Database.rs.next())
                    {
                        if(!Database.rs.getString("email").isEmpty())
                        {
                            Registrazione.this.risultato=false;
                            break;
                        }
                    }
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_controllo_mail), Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(Database.FLAG_SELECT_RAW, "SELECT email FROM accounts WHERE email = '"+email+"'");

    }

    /**
     *
     * Questo metodo si occupa controllare che tutti i campi (partitaIVA escluso) siano stati riempiti correttamente
     *
     * @return true se le condizioni sono state rispettate | false in caso contrario
     */
    private boolean fieldChecker() {
        boolean flag=true;

        for(int i=0; i< campo.size();i++)
        {
            if(campo.get(i).getEditText().getText().toString().trim().isEmpty())
            {
                campo.get(i).setError(getText(R.string.campoMancante));
                flag=false;
            }
            if(campo.get(i) == email)
            {
                risultato=true;
                emailCheckerDB(campo.get(i).getEditText().getText().toString().trim());
                if(!emailChecker(campo.get(i).getEditText().getText().toString().trim()) || !this.risultato)
                {
                    campo.get(i).setError(getText(R.string.emailInvalida));
                    flag=false;
                }
            }
            if(campo.get(i) == password)
            {
                if(!campo.get(i).getEditText().getText().toString().trim().equals(campo.get(i+1).getEditText().getText().toString().trim()))
                {
                    campo.get(i).setError(getText(R.string.passwordNonCoincidenti));
                    campo.get(i+1).setError(getText(R.string.passwordNonCoincidenti));
                    flag=false;
                }
            }
            if(campo.get(i) == telefono)
            {
                if(campo.get(i).getEditText().getText().length() != 10 || !(campo.get(i).getEditText().getText().toString().matches("[0-9]+")))
                {
                    campo.get(i).setError(getText(R.string.telefonoErrato));
                    flag=false;
                }
            }
        }
        return flag;
    }

    public void termina(View view) {
        onBackPressed();
    }

    /**
     *
     * Si occupa di acquisire da tutti i campi del form i dati e di registrare l'account
     *
     * @param view verrà passato in input l'oggetto dal quale proviene la chiamata, in questo caso un pulsante
     */
    public void registrazione(View view)
    {
        if(fieldChecker())
        {

            String queryMuseo = "INSERT INTO musei(Nome, Telefono, Provincia, Citta, CAP, Indirizzo, P_IVA) VALUES ('"
                    +campo.get(0).getEditText().getText().toString()+"' , "
                    +Long.parseLong(campo.get(4).getEditText().getText().toString())+" , '"
                    +campo.get(5).getEditText().getText().toString()+"' , '"
                    +campo.get(6).getEditText().getText().toString()+"' , '"
                    +campo.get(7).getEditText().getText().toString()+"' , '"
                    +campo.get(8).getEditText().getText().toString()+"' , '"
                    +partitaIVA.getEditText().getText().toString()+"' )";

            //1. fai partire la query

            Database musei = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output)
                {

                }
            }).execute(Database.FLAG_INSERT,queryMuseo);

            Database idMuseo = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output)
                {

                    String[] idMuseo = output.split(",");

                    String queryAccount = "INSERT INTO accounts(idMuseo, Email, Password) VALUES ("
                            +Integer.parseInt(idMuseo[0])+" , '"
                            +campo.get(1).getEditText().getText().toString()+"' , '"
                            +campo.get(2).getEditText().getText().toString()+"' )";

                    Database accounts = (Database) new Database(new Database.AsyncResponse() {

                        @Override
                        public void processFinish(String output)
                        {

                        }
                    }).execute(Database.FLAG_INSERT,queryAccount);
                }
            }).execute(Database.FLAG_SELECT,"SELECT idMuseo FROM musei WHERE Nome = '"+campo.get(0).getEditText().getText().toString()+"'","1");

            //2 ritorna a dove stavi ✓
            onBackPressed();
        }
        else
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_inserimento_museo), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        for(int i=0;i<campo.size();i++)
        {
            if(!campo.get(i).getEditText().getText().toString().isEmpty()) outState.putString(nomeCampo.get(i),this.campo.get(i).getEditText().getText().toString());
        }

        if(!this.partitaIVA.getEditText().getText().toString().isEmpty()) outState.putString(nomeCampo.get(9),this.partitaIVA.getEditText().getText().toString());
    }
}



