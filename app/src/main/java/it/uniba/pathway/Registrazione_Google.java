package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class Registrazione_Google extends AppCompatActivity {

    private TextInputLayout nomeMuseo;
    private TextInputLayout telefono;
    private TextInputLayout provincia;
    private TextInputLayout citta;
    private TextInputLayout CAP;
    private TextInputLayout indirizzo;
    private TextInputLayout partitaIVA;
    private String idGoogle;
    private ArrayList<TextInputLayout> campo = new ArrayList<>();
    private ArrayList <String> nomeCampo = new ArrayList<>();
    private boolean risultato = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_google);

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            this.idGoogle = extras.getString("idGoogle");
        }

        nomeMuseo = (TextInputLayout) findViewById(R.id.NomeMuseo);

        telefono = (TextInputLayout) findViewById(R.id.telefono);
        provincia = (TextInputLayout) findViewById(R.id.provincia);
        citta = (TextInputLayout) findViewById(R.id.citta);
        CAP = (TextInputLayout) findViewById(R.id.CAP);
        indirizzo = (TextInputLayout) findViewById(R.id.indirizzo);
        partitaIVA = (TextInputLayout) findViewById(R.id.partitaIVA);

        campo.add(nomeMuseo);  nomeCampo.add("nomeMuseo");
        campo.add(telefono);   nomeCampo.add("telefono");
        campo.add(provincia);  nomeCampo.add("provincia");
        campo.add(citta);      nomeCampo.add("citta");
        campo.add(CAP);        nomeCampo.add("CAP");
        campo.add(indirizzo);  nomeCampo.add("indirizzo");
        campo.add(partitaIVA); nomeCampo.add("partitaIVA");

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
            if(campo.get(i).getEditText().getText().toString().trim().isEmpty() && !nomeCampo.get(i).equals("partitaIVA"))
            {
                campo.get(i).setError(getText(R.string.campoMancante));
                flag=false;
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

    @Override
    public void onBackPressed() { //In caso si abbia sbagliato a scegliere un account Google, tornando in dietro sarà possibile cambiare account

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        super.onBackPressed();

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

            String queryMuseo = "INSERT INTO musei(Nome, Telefono, Provincia, Citta, CAP, Indirizzo, P_IVA, idGoogle) VALUES ('"
                    +campo.get(0).getEditText().getText().toString()+"' , "
                    +Long.parseLong(campo.get(1).getEditText().getText().toString())+" , '"
                    +campo.get(2).getEditText().getText().toString()+"' , '"
                    +campo.get(3).getEditText().getText().toString()+"' , '"
                    +campo.get(4).getEditText().getText().toString()+"' , '"
                    +campo.get(5).getEditText().getText().toString()+"' , '"
                    +campo.get(6).getEditText().getText().toString()+"' , '"
                    +idGoogle+"' )";

            //1. fai partire la query

            Database musei = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output)
                {
                    Intent intent = new Intent(Registrazione_Google.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("flagSessione",true);
                    intent.putExtra("idGoogle",idGoogle);
                    startActivity(intent);
                }
            }).execute(Database.FLAG_INSERT,queryMuseo);
            //2 ritorna a dove stavi ✓

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
    }

}