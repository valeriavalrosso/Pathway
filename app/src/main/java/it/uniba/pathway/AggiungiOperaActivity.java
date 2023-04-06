package it.uniba.pathway;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


/**
 * L'activity AggiungiOperaActivity permette all'utente di aggiungere una nuova opera al suo museo.
 *
 * Avrà la possibilità di specificare le proprietà dell'opera, quali:
 *  -il titolo dell'opera
 *  -la zona del museo nella quale l'opera è posizionata
 *  -un'immagine o una foto dell'opera, in modo da essere maggiormente riconoscibile
 *  -una descrizione dell'opera, che fornisca maggiori informazioni su di essa
 */
public class AggiungiOperaActivity extends AppCompatActivity {

    private static String ID_MUSEO;
    private int idOpera;

    ImageButton frecciaIndietro;
    ImageButton salvaOpera;

    private LinearLayout salvataggioOpera;
    private boolean TITOLO_VALIDO, ZONA_VALIDA;

    LinkedHashMap<String, String> nomeZonaOpereDB;
    private TextInputLayout titoloOpera;
    private TextInputEditText titoloEditField;
    private String titolo;

    private ArrayList<String> zoneMuseo;
    private String zona;
    private String zonaScelta;
    private TextInputLayout zonaOpera;
    AutoCompleteTextView nomeZonaOpera;
    ArrayAdapter<String> adapterZone;

    private boolean IMMAGINE_ESTESA;
    private CircularRevealCardView frameImmagine;
    private ImageView immagine;
    private FrameLayout frameImmagineFullscreen;
    private ImageView immagineFullscreen;
    private ImageButton galleria, fotocamera;

    private static final int FOTOCAMERA_CODE = 1;
    private static final int GALLERIA_CODE = 2;
    private byte[] immagineSelezionata;
    private Uri uriImmagineSelezionata;
    private Bitmap operaBitmap;

    TextInputLayout descrizioneOpera;
    String descrizione;

    private QRGEncoder qrgEncoder;
    private Bitmap QRcodeBitmap;
    private byte[] QRcodeOpera;

    private int posizione;

    private BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_opera);


        GestioneDellaSessione sessione = new GestioneDellaSessione(AggiungiOperaActivity.this);
        ID_MUSEO = sessione.getSessione();


        // inizializzazione degli elementi del layout
        frecciaIndietro = (ImageButton) findViewById(R.id.frecciaIndietro);
        salvaOpera = (ImageButton) findViewById(R.id.salvaOpera);

        salvataggioOpera = (LinearLayout) findViewById(R.id.layout_salvataggio_opera);
        TITOLO_VALIDO = true; ZONA_VALIDA = true;

        titoloOpera = (TextInputLayout) findViewById(R.id.titoloOpera);
        titoloEditField = (TextInputEditText) findViewById(R.id.titoloEditField);

        zonaOpera = (TextInputLayout) findViewById(R.id.zonaOpera);
        nomeZonaOpera = (AutoCompleteTextView) findViewById(R.id.nomeZonaOpera);

        frameImmagine = (CircularRevealCardView) findViewById(R.id.frameImmagine);
        immagine = (ImageView) findViewById(R.id.immagine);
        frameImmagineFullscreen = (FrameLayout) findViewById(R.id.frameImmagineFullscreen);
        immagineFullscreen = (ImageView) findViewById(R.id.immagineFullscreen);
        galleria = (ImageButton) findViewById(R.id.galleria);
        fotocamera = (ImageButton) findViewById(R.id.fotocamera);
        IMMAGINE_ESTESA = false;

        descrizioneOpera = (TextInputLayout) findViewById(R.id.descrizione);


        if (savedInstanceState != null) {
            titolo = savedInstanceState.getString("titolo_opera");
            zonaScelta = savedInstanceState.getString("zona_opera");
            IMMAGINE_ESTESA = savedInstanceState.getBoolean("immagine_estesa");
            immagineSelezionata = savedInstanceState.getByteArray("immagine_scelta");
            descrizione = savedInstanceState.getString("descrizione_opera");
            QRcodeOpera = savedInstanceState.getByteArray("qr_code_opera");
        }


        showBottomNavigationBar();


        zoneMuseo = new ArrayList<String>();
        getZoneMuseoDB();
        setAdapterZone();



        nomeZonaOpereDB = new LinkedHashMap<>();
        getNomeZonaOpereDB();



        // gestione degli errori sul titolo
        manageTitleError();

        // gestione dell'errore sulla zona
        manageAreaError();

        // gestione del click sulla freccia per tornare indietro
        manageClickIndietroLista();

        ArrayList<Integer> idOperaAggiunta = new ArrayList<Integer>();
        // gestione del click per il salvataggio dell'opera
        manageClickSalvaOpera();

        // gestione dei click sulle icone che riguardano l'immagine dell'opera
        expandImage();
        minimizeImage();
        clickCamera();
        clickGalleria();
    }



    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("titolo_opera", titoloOpera.getEditText().getText().toString());
        outState.putString("zona_opera", zonaScelta);
        outState.putBoolean("immagine_estesa", IMMAGINE_ESTESA);
        outState.putByteArray("immagine_scelta", immagineSelezionata);
        outState.putString("descrizione_opera", descrizioneOpera.getEditText().getText().toString());
        outState.putByteArray("qr_code_opera", QRcodeOpera);

        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        titolo = savedInstanceState.getString("titolo_opera");
        zonaScelta = savedInstanceState.getString("zona_opera");
        IMMAGINE_ESTESA = savedInstanceState.getBoolean("immagine_estesa");
        immagineSelezionata = savedInstanceState.getByteArray("immagine_scelta");
        descrizione = savedInstanceState.getString("descrizione_opera");
        QRcodeOpera = savedInstanceState.getByteArray("qr_code_opera");
    }



    /**
     * Attiva o disattiva (mantenendo la configurazione corrente) la rotazione dello schermo.
     * <p>
     *     La seconda condizione si verifica solo in fase di salvataggio dell'opera. Infatti,
     *     è stato implementato perchè, se si ruota lo schermo durante il salvataggio dell'opera,
     *     l'overlay che ne indica l'azione, non assume visibilità <i>GONE</i> nel momento in cui
     *     l'azione è terminata, ma solamente al successivo refresh dell'activity.
     * </p>
     * <br>
     * @param locked indica se la rotazione deve essere bloccata (<i>true</i>) o sbloccata (<i>false</i>)
     */
    public void lockDeviceRotation(boolean locked) {
        if (locked) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }
    }


    /**
     * Visualizza e gestisce la bottom navigation bar tramite l'implementazione di un listener.
     * <p>
     *     Infatti, tramite il metodo di callback onNavigationItemSelected() dell'interfaccia
     *     OnNavigationItemSelectedListener si accede a tre diverse activity, in base a quale item
     *     clicca l'utente. Trovandosi nell'aggiunta dell'opera, nessun item è selezionato.
     * </p>
     */
    public void showBottomNavigationBar() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar_aggiungi_opera);
        bottomNavigationView.getMenu().findItem(R.id.scanner).setCheckable(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.scanner:
                        startActivity(new Intent(getApplicationContext(), Request_Camera.class));
                        overridePendingTransition(0, 0);
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
    }


    /**
     * Effettua una richiesta al database per ottenere tutte le opere presenti all'interno del
     * museo e poterle mostrare nel menu a discesa.
     */
    public void getZoneMuseoDB() {
        Database opereMuseo = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                try {
                    while (Database.rs.next()) {
                        zona = Database.rs.getString("Nome");
                        zoneMuseo.add(zona);
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_recupero_opere), Toast.LENGTH_SHORT).show();
                }
            }

        }).execute(Database.FLAG_SELECT_RAW, "SELECT Nome FROM zone WHERE idMuseo = " + ID_MUSEO);
    }

    /**
     * Crea la lista delle zone presenti all'interno del museo, per poterla visualizzare come
     * menu a discesa nel secondo campo di testo.
     */
    public void setAdapterZone() {
        adapterZone = new ArrayAdapter<String>(this, R.layout.zone_lista_aggiunta_opere, zoneMuseo);
        nomeZonaOpera.setAdapter(adapterZone);

        nomeZonaOpera.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                zonaScelta = parent.getItemAtPosition(position).toString();
            }
        });
    }


    /**
     * Effettua una richiesta al database per poter popolare una mappa che metta in corrispondenza
     * ogni opera con la relativa zona, in modo tale che l'opera che si sta aggiungendo non abbia
     * lo stesso nome di un'altra delle opere presenti all'interno del museo e, inoltre, in modo
     * che si possa ricavare la posizione della nuova opera all'interno della zona.
     * <p>
     *     NB : di defualt, l'opera appena aggiunta sarà l'ultima della zona.
     * </p>
     */
    public void getNomeZonaOpereDB() {
        Database asyncTask_getNomiOpere = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {
                System.out.println("Output opere esistenti: " + output);

                if (Boolean.parseBoolean(output)) {
                    try {
                        while (Database.rs.next()) {
                            nomeZonaOpereDB.put(Database.rs.getString("Nome").toUpperCase(), Database.rs.getString("Zona"));
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_recupero_opere), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }).execute(Database.FLAG_SELECT_RAW, "SELECT Nome, Zona FROM opere WHERE idMuseo = " + ID_MUSEO);
    }


    /**
     * Gestisce la visualizzazione degli errori sul titolo dell'opera, che, oltre ad essere un
     * campo obbligatorio, dunque non può contenere una stringa vuota, non deve neanche contenere
     * una stringa che corrisponda al nome di un'altra delle opere presenti nel museo.
     */
    public void manageTitleError() {
        titoloEditField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                titoloOpera.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkTitolo();
            }
        });
    }

    /**
     * Gestisce la visualizzazione dell'errore sulla zona, che è un campo obbligatorio, quindi
     * l'utente deve necessariamente sceglierne una.
     * <p>
     *     NB : l'errore sarà visibile solo dopo che l'utente ha cliccato almeno una volta
     *     sull'icona di salvataggio.
     * </p>
     */
    public void manageAreaError() {
        zonaOpera.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    zonaOpera.setError(null);
                }
                else if (!ZONA_VALIDA) {
                    checkZona();
                }
            }
        });
    }


    /**
     * Gestisce il click sulla freccia verso sinistra, posta in alto a sinistra.
     * <p>
     *     In particolare, mostra un dialog che chiede all'utente la conferma dell'azione, perchè,
     *     in tal caso, le modifiche non saranno salvate.
     * </p>
     */
    public void manageClickIndietroLista() {
        frecciaIndietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AggiungiOperaActivity.this);
                builder.setTitle(R.string.uscita_title);
                builder.setMessage(R.string.uscita_message);
                builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AggiungiOperaActivity.super.onBackPressed();
                    }
                });
                builder.setNegativeButton(R.string.dialog_negative_button, null);
                builder.show();
            }
        });
    }


    /**
     * Override del metodo onBackPressed(), che permette di mostrare all'utente un dialog, in cui
     * si chiede conferma dell'azione, perchè, in tal caso, le modifiche non saranno salvate.
     */
    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AggiungiOperaActivity.this);
        builder.setTitle(R.string.uscita_title);
        builder.setMessage(R.string.uscita_message);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AggiungiOperaActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }


    /**
     * Gestisce il click sull'icona di salvataggio dell'opera.
     * <p>
     *     In particolare, controlla che i campi del titolo e della zona non presentino errori e,
     *     solo se entrambi i campi sono validi, avviene il reale salvataggio dell'opera:<br>
     *     1. si popola la variabile GestioneImmagine.immagineBitmap, dato che la classe
     *     Database.java carica come immagine il conteuto di quesat variabile;<br>
     *     2. viene chiamato il metodo salvaOperaDB().
     * </p>
     */
    public void manageClickSalvaOpera() {
        salvaOpera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titolo = titoloOpera.getEditText().getText().toString();

                descrizione = descrizioneOpera.getEditText().getText().toString();

                checkTitolo();
                checkZona();


                // se titolo e zona sono validi, allora l'opera viene salvata nel db
                if (TITOLO_VALIDO && ZONA_VALIDA) {
                    titoloOpera.clearFocus();
                    zonaOpera.clearFocus();
                    salvataggioOpera.setVisibility(View.VISIBLE);
                    lockDeviceRotation(true);


                    // ricavo la posizione della nuova opera all'interno della zona (default = ultima)
                    posizione = 1;
                    for (Map.Entry<String, String> operaCurr : nomeZonaOpereDB.entrySet()) {
                        if (operaCurr.getValue().equals(zonaScelta)) {
                            posizione++;
                        }
                    }


                    // nascondo la tastiera mentre viene eseguito il salvataggio
                    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(salvaOpera.getWindowToken(), 0);



                    if (operaBitmap == null) {
                        GestioneImmagine.immagineBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.image_icon);
                    }

                    salvaOperaDB();
                }
            }
        });
    }

    /**
     * Controlla se sono presenti errori sul titolo.
     */
    public void checkTitolo() {
        if (titoloOpera.getEditText().getText().toString().isEmpty()) {
            TITOLO_VALIDO = false;
            titoloOpera.setError(getString(R.string.titolo_opera_non_inserito));
        }
        else if (nomeZonaOpereDB.containsKey(titoloOpera.getEditText().getText().toString().toUpperCase())) {
            TITOLO_VALIDO = false;
            titoloOpera.setError(getString(R.string.titolo_opera_esistente));
        }
        else {
            titoloOpera.setError(null);
            TITOLO_VALIDO = true;
        }
    }

    /**
     * Controlla se sono presenti errori sulla zona.
     */
    public void checkZona() {
        if (zonaScelta == null) {
            ZONA_VALIDA = false;
            zonaOpera.setError(getString(R.string.zona_non_selezionata));
        }
        else {
            zonaOpera.setError(null);
            ZONA_VALIDA = true;
        }
    }

    /**
     * Salva l'opera sul database.
     * <p>
     *     Per effettuare il salvataggio, si effettuano i seguenti passaggi:<br>
     *     1. si salvano nome, descrizione, visibilita, zona, immagine e idMuseo sul database;<br>
     *     2. si recupera dal database l'ID dell'opera appena aggiunta, tramite il titolo e l'ID
     *     del museo, in quanto il titolo deve essere unico al suo interno;<br>
     *     3. si genera e viene caricato sul database il QRcode contenente l'ID dell'opera appena
     *     aggiunta.
     * </p>
     */
    public void salvaOperaDB() {
        Database inserimentoPrimiDati = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                try {
                    Database getIdOperaAggiunta = (Database) new Database(new Database.AsyncResponse() {

                        @Override
                        public void processFinish(String output) {

                            try {
                                while(Database.rs.next()) {
                                    idOpera = Database.rs.getInt("idOpere");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                salvataggioOpera.setVisibility(View.GONE);
                                lockDeviceRotation(false);
                                Toast.makeText(getApplicationContext(), R.string.errore_salvataggio_opera, Toast.LENGTH_SHORT).show();
                            }
                            generazioneQRcode(idOpera); // metodo che genera e carica il QRcode
                            salvataggioOpera.setVisibility(View.GONE);
                            lockDeviceRotation(false);

                            // quando finisce di caricare l'opera nel db, visualizza la lista delle opere
                            Intent i = new Intent(AggiungiOperaActivity.this, OpereActivity.class);
                            i.putExtra("previousActivity", "AggiungiOperaActivity");
                            i.putExtra("nome_zona", zonaScelta);
                            startActivity(i);

                            Toast.makeText(getApplicationContext(), R.string.opera_salvata, Toast.LENGTH_SHORT).show();
                        }

                    }).execute(Database.FLAG_SELECT_RAW, "SELECT idOpere FROM opere WHERE Nome = '" + titolo + "' AND idMuseo = " + ID_MUSEO);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    salvataggioOpera.setVisibility(View.GONE);
                    lockDeviceRotation(false);
                    Toast.makeText(getApplicationContext(), R.string.errore_salvataggio_opera, Toast.LENGTH_SHORT).show();
                }
            }

        }).execute(Database.FLAG_INSERT, "INSERT INTO opere (Nome, Descrizione, Visibilita, Zona, Immagine, idMuseo, Posizione) VALUES ('" + titolo + "', ?, 1, '" + zonaScelta + "', ?, '" + ID_MUSEO + "', + " + posizione + ")", descrizione);
    }


    /**
     * Permette di visualizzare l'immagine dell'opera a tutto schermo, quando si clicca su di essa.
     */
    public void expandImage() {
        frameImmagine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IMMAGINE_ESTESA = true;

                frameImmagine.setVisibility(View.INVISIBLE);
                frameImmagineFullscreen.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Permette di visualizzare l'immagine all'interno del frame, quando si clicca sull'immagine
     * a schermo intero.
     */
    public void minimizeImage() {
        frameImmagineFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IMMAGINE_ESTESA = false;

                frameImmagineFullscreen.setVisibility(View.GONE);
                frameImmagine.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Gestisce il click sull'icona della galleria.
     * <p>
     *     In particolare, tramite un intent, permette di accedere alla galleria del dispositivo,
     *     in modo tale che l’utente possa scegliere una delle immagini presenti per rappresentare
     *     l’opera che sta aggiungendo.
     * </p>
     */
    public void clickGalleria() {
        galleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERIA_CODE);
            }
        });
    }

    /**
     * Gestisce il click sull'icona della fotocamera.
     * <p>
     *     In particolare, controlla se è stato concesso il permesso per poter accedere alla
     *     fotocamera del dispositivo e, in caso di esito positivo, tramite un intent, permette di
     *     scattare una foto che rappresenti l’opera che si sta aggiungendo.
     * </p>
     */
    public void clickCamera() {
        fotocamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, FOTOCAMERA_CODE);
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, FOTOCAMERA_CODE);
                }
            }
        });
    }

    /**
     * Override del metodo di callback onRequestPermissionsResult(). Viene invocato a ogni chiamata
     * del metodo requestPermissions(), presente nel metodo clickCamera(), e gestisce le azioni
     * dopo che all'utente è stato chiesto il permesso necessario.
     * <br><br>
     * @param requestCode codice passato al metodo requestPermissions()
     * @param permissions indica il permesso richiesto all'utente
     * @param grantResults indica se il permesso è stato concesso o meno
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FOTOCAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, FOTOCAMERA_CODE);
            }
            else {
                Toast.makeText(this, R.string.permesso_negato_fotocamera, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Override del metodo di callback onActivityResult(). Questo metodo, viene invocato dopo che
     * viene chiamato il metodo startActivityForResult(), chiamato nei metodi clickCamera() e
     * clickGalleria().
     * <p>
     *     In particolare, se è stata scattata una foto dalla fotocamera o è stata selezionata
     *     un'immagine dalla galleria, questa immagine viene salvata e impostata nell'ImageView
     *     corrispondente.
     * </p>
     * @param requestCode indica quale startActivityForResult() ha invocato onActivityResult()
     * @param resultCode indica il risultato dell'intent
     * @param data contiene i dati passati dall'intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            switch (requestCode) {

                case FOTOCAMERA_CODE:
                    operaBitmap = (Bitmap) data.getExtras().get("data");

                    immagine.setImageBitmap(operaBitmap);
                    immagineFullscreen.setImageBitmap(operaBitmap);

                    Toast.makeText(getApplicationContext(), R.string.immagine_inserita, Toast.LENGTH_SHORT).show();
                    break;

                case GALLERIA_CODE:
                    uriImmagineSelezionata = data.getData();

                    try {
                        operaBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImmagineSelezionata);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    immagine.setAlpha(1.0F);
                    immagine.setImageBitmap(operaBitmap);
                    immagineFullscreen.setImageBitmap(operaBitmap);

                    break;
            }


            GestioneImmagine.immagineBitmap = operaBitmap;
            immagineSelezionata = GestioneImmagine.byteArrayConversion();
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.errore_inserimento_immagine, Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Metodo che genera il QR code dell'opera corrispondente all'ID passato in input, e lo carica
     * sul database.
     * <p>
     *     In particolare, utilizza la libreria qrgenerator per creare il QR code, in base ai
     *     parametri passati, ovvero il testo che il QR code deve contenere, il formato del testo
     *     e le dimensioni che il QR code deve assumere.
     * </p>
     * <p>
     *     Infine, il QRcode viene salvato nella variabile immagineBitmap della classe
     *     GestioneImmagine e viene caricato sul database tramite il metodo aggiornaImmagineQr()
     *     della classe GestioneImmagine.
     * </p>
     * @param idOpera ID dell'opera di cui si deve generare e caricare il QR code
     */

    public void generazioneQRcode(int idOpera) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;
        int finalDimen;

        if (width < height)
            finalDimen = width;
        else finalDimen = height;
        finalDimen = finalDimen * 3/4;

        qrgEncoder = new QRGEncoder(idOpera+"", null, QRGContents.Type.TEXT, finalDimen);
        try {
            QRcodeBitmap = qrgEncoder.getBitmap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        GestioneImmagine.immagineBitmap = QRcodeBitmap;
        GestioneImmagine.aggiornaImmagineQr(idOpera);
        QRcodeOpera = GestioneImmagine.byteArrayConversion(); // conversione per salvarlo con onSaveInstanceState
    }
}