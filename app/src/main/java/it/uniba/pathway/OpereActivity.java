package it.uniba.pathway;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


/**
 * L'activity OpereActivity permette di visualizzare e modificare l'elenco delle
 * opere, divise in base alle zone presenti nel museo. Inoltre, è presente anche
 * la funzionalità di ricerca delle opere tramite il titolo.
 */
public class OpereActivity extends AppCompatActivity {

    private String previousActivity;
    public String nomeZona;
    public static String ID_MUSEO;

    private boolean CARICAMENTO_OPERE_AVVIATO;

    private ImageButton frecciaIndietro;

    private SearchView searchbar;
    private TextView nessunaOpera;
    private boolean ZONA_VUOTA;
    private LinearLayout caricamentoOpere;

    private boolean OPERE_CARICATE;
    private Opera opera;
    private ListAdapter listAdapter;
    private ListView listaOpere;

    private ArrayList<Integer> opereVisibilitaModificata;
    private ArrayList<Integer> opereDaEliminare;

    private ArrayList<Opera> opere;
    private OpereViewModel opereViewModel;

    private FloatingActionButton fabOpere, fabAggiuntaOpere, fabModificaOpere, fabModificaPercorso;
    private LinearLayout layoutAggiuntaOpere, layoutModificaOpere, layoutModificaPercorso;
    private TextView labelModificaOpere, labelModificaPercorso;
    private Animation fromBottomAdd, fromBottomEdit, toBottomAdd, toBottomEdit, rotateForward, rotateBackward;

    private boolean MENU_SELECTED = true;
    private boolean EDIT_MODE = false;
    private int idOperaCliccata;

    private BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GestioneDellaSessione sessione = new GestioneDellaSessione(OpereActivity.this);
        ID_MUSEO = sessione.getSessione();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opere);


        lockDeviceRotation(true);


        OPERE_CARICATE = false;  // false = le opere non sono ancora state prelevate tutte dal db
        CARICAMENTO_OPERE_AVVIATO = false;  // false = la chiamata al db per le opere non è stata ancora avviata
        ZONA_VUOTA = false;


        if(savedInstanceState != null) {
            CARICAMENTO_OPERE_AVVIATO = savedInstanceState.getBoolean("caricamentoAvviato");
            OPERE_CARICATE = savedInstanceState.getBoolean("opereCaricate");
            ZONA_VUOTA = savedInstanceState.getBoolean("zonaVuota");
            EDIT_MODE = savedInstanceState.getBoolean("editMode");
            opereVisibilitaModificata = savedInstanceState.getIntegerArrayList("opereModificaVisibilita");
            opereDaEliminare = savedInstanceState.getIntegerArrayList("opereEliminazione");
        }


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeZona = extras.getString("nome_zona");
            previousActivity = extras.getString("previousActivity");
        }

        if (previousActivity == null) {
            previousActivity = "";
        }


        // inizializzazione degli elementi del layout
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar_opere);
        showBottomNavigationBar();

        frecciaIndietro = (ImageButton) findViewById(R.id.frecciaIndietro);
        frecciaIndietro.setVisibility(View.GONE);

        fabOpere = (FloatingActionButton) findViewById(R.id.fabOpere);
        fabOpere.setVisibility(View.GONE);

        fabAggiuntaOpere = (FloatingActionButton) findViewById(R.id.fabAggiuntaOpere);
        layoutAggiuntaOpere = (LinearLayout) findViewById(R.id.layoutAggiuntaOpere);
        fabModificaOpere = (FloatingActionButton) findViewById(R.id.fabModificaOpere);
        labelModificaOpere = (TextView) findViewById(R.id.labelModificaOpere);
        layoutModificaOpere = (LinearLayout) findViewById(R.id.layoutModificaOpere);
        fabModificaPercorso = (FloatingActionButton) findViewById(R.id.fabModificaPercorso);
        labelModificaPercorso = (TextView) findViewById(R.id.labelModificaPercorso);
        layoutModificaPercorso = (LinearLayout) findViewById(R.id.layoutModificaPercorso);
        disableAddEditButtons();

        caricamentoOpere = (LinearLayout) findViewById(R.id.layout_caricamento_opere);
        nessunaOpera = (TextView) findViewById(R.id.nessunaOpera);
        listaOpere = (ListView) findViewById(R.id.listaOpere);
        searchbar = (SearchView) findViewById(R.id.searchbar);


        // controllo se la zona è vuota
        if (ZONA_VUOTA) {
            nessunaOpera.setVisibility(View.VISIBLE);
            ZONA_VUOTA = true;
            showViews();
            hideEditButtons();  // button per modifica di opere e percorso non accessibili
        }


        // controllo se si è in fase di modifica
        if (EDIT_MODE) {
            fabOpere.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.cactus_light));
            fabOpere.setImageResource(R.drawable.ic_baseline_check_24);
        }


        opereVisibilitaModificata = new ArrayList<Integer>();
        opereDaEliminare = new ArrayList<Integer>();


        opere = new ArrayList<Opera>();
        opereViewModel = new ViewModelProvider(this).get(OpereViewModel.class);
        // '-> ViewModel viene solo ri-inizializzato al refresh dell'activity, non ri-creato


        if (!opereViewModel.opere.isEmpty()) {  // se l'arraylist di opere è stata già popolata
            setListAdapter();
        }
        else if (!CARICAMENTO_OPERE_AVVIATO) {  // se la richiesta al db non è stata ancora inviata
            CARICAMENTO_OPERE_AVVIATO = true;
            getOpereFromDB();
        }


        // gestione del click di ciascun elemento della lista -> implementazione interfaccia del listener
        manageArtworkClickListener();

        // gestione del click della freccia per tornare indietro
        manageClickIndietro();

        // gestione dell'inserimento di testo all'interno della searchbar
        manageSearchBarTextListener();

        // gestione del click dei floating action button del menu
        manageClickBtnOpere();
        manageClickModificaOpere();
        manageClickModificaPercorso();
        manageClickAggiungi();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("caricamentoAvviato", CARICAMENTO_OPERE_AVVIATO);
        outState.putBoolean("opereCaricate", OPERE_CARICATE);
        outState.putBoolean("zonaVuota", ZONA_VUOTA);
        outState.putBoolean("editMode", EDIT_MODE);
        outState.putIntegerArrayList("opereModificaVisibilita", opereVisibilitaModificata);
        outState.putIntegerArrayList("opereEliminazione", opereDaEliminare);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CARICAMENTO_OPERE_AVVIATO = savedInstanceState.getBoolean("caricamentoAvviato");
        OPERE_CARICATE = savedInstanceState.getBoolean("opereCaricate");
        ZONA_VUOTA = savedInstanceState.getBoolean("zonaVuota");
        EDIT_MODE = savedInstanceState.getBoolean("editMode");
        opereVisibilitaModificata = savedInstanceState.getIntegerArrayList("opereModificaVisibilita");
        opereDaEliminare = savedInstanceState.getIntegerArrayList("opereEliminazione");
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
     * Attiva o disattiva (mantenendo la configurazione corrente) la rotazione dello schermo.
     * <p>
     *     La seconda condizione si verifica solo in fase di caricamento delle opere. Infatti,
     *     è stato implementato perchè, se si ruota lo schermo durante la creazione della lista
     *     delle opere, l'overlay che ne indica il caricamento, non assume visibilità <i>GONE</i>
     *     nel momento in cui l'azione è terminata, ma solamente al successivo refresh dell'activity.
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
     *     clicca l'utente. Trovandosi nella lista delle opere, nessun item risulta selezionato.
     * </p>
     */
    public void showBottomNavigationBar() {
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
     * Disattiva la visibilità di alcuni elementi dell'interfaccia e la attiva per altri, in modo da
     * poter permettere all'utente di interagirvi al completamento del caricamento delle opere.
     */
    public void showViews() {
        caricamentoOpere.setVisibility(View.GONE);
        lockDeviceRotation(false);  // rotazione schermo abilitata

        frecciaIndietro.setVisibility(View.VISIBLE);
        searchbar.setVisibility(View.VISIBLE);
        fabOpere.setVisibility(View.VISIBLE);

        listaOpere.setVisibility(View.VISIBLE);
    }


    /**
     * Effettua una richiesta al database, con cui recupera tutte le informazioni sulle opere presenti.
     * <p>
     *     Popola le variabili ArrayList&lt;Opera&gt; globali, quindi non necessita di input o output.
     * </p>
     */
    public void getOpereFromDB() {
        Database opereZonaCliccata = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if (!Boolean.parseBoolean(output)) {
                    OPERE_CARICATE = true;

                    nessunaOpera.setVisibility(View.VISIBLE);
                    ZONA_VUOTA = true;
                    showViews();
                    hideEditButtons();  // button per modifica di opere e percorso non accessibili
                }
                else {
                    try {
                        while (Database.rs.next()) {
                            opera = new Opera(
                                    Database.rs.getInt("idOpere"),
                                    Database.rs.getString("Nome"),
                                    Database.rs.getString("Descrizione"),
                                    Database.rs.getBoolean("visibilita"),
                                    Database.rs.getString("Zona"),
                                    GestioneImmagine.getImmagineBitmap(Database.rs.getBytes("Immagine")),
                                    Database.rs.getInt("idMuseo"),
                                    Database.rs.getInt("Posizione"),
                                    false
                            );

                            opere.add(opera);
                        }
                        OPERE_CARICATE = true;

                        // aggiunta di un'opera vuota come ultimo elemento della lista delle opere
                        //   '-> altrimenti in edit mode l'ultimo elemento sarebbe in parte coperto dal fab
                        Opera operaVuota = new Opera(-1, "", "", false, nomeZona, null, Integer.parseInt(ID_MUSEO), 0, false);
                        opere.add(operaVuota);

                        opereViewModel.addAllOpere(opere);
                        setListAdapter();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_recupero_opere), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }).execute(Database.FLAG_SELECT_RAW, "SELECT * FROM opere WHERE idMuseo = " + ID_MUSEO + " AND Zona = '" + nomeZona + "' ORDER BY Posizione");
    }


    /**
     * Crea una nuova istanza di ListAdapter ed effettua la sua inizializzazione.
     * <p>
     *     In particolare, al costruttore di ListAdapter viene fornito il contesto, l'ArrayList
     *     di opere con cui si deve popolare la lista e il flag EDIT_MODE, che indica se ci si
     *     trova nella modalità di modifica delle opere o meno.
     * </p>
     */
    public void setListAdapter() {
        listAdapter = new ListAdapter(OpereActivity.this, opereViewModel.opere, EDIT_MODE);
        listaOpere.setAdapter(listAdapter);

        showViews();
    }


    /**
     * Gestisce il click su ognuna delle opere della lista.
     * <p>
     *     E' stato implementato il metodo onItemClick dell'interfaccia OnItemClickListener,
     *     che, tramite un intent, permette all'utente di visualizzare la scheda dettagliata
     *     dell'opera selezionata dalla lista. Inoltre, è stato implementato il controllo
     *     sull'id dell'opera, che deve essere diverso da -1, dato che quest'ultimo
     *     identifica l'opera vuota (utile alla sola User Experience).
     * </p>
     */
    public void manageArtworkClickListener() {
        listaOpere.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                idOperaCliccata = Integer.parseInt(((TextView) view.findViewById(R.id.idOpera)).getText().toString());

                if(idOperaCliccata != -1) {    // se l'opera cliccata non è l'opera vuota
                    Intent i = new Intent(OpereActivity.this, OperaDettagliata.class);
                    i.putExtra("nome_zona", nomeZona);
                    i.putExtra("NUM", idOperaCliccata); //NUM = id_opera
                    startActivity(i);
                }
            }
        });
    }


    /**
     * Gestisce la barra di ricerca, filtrando la lista delle opere in base al testo inserito
     * dall'utente.
     * <p>
     *     Infatti, è stato dichiarato un listener, ovvero l'interfaccia OnQueryTextListener,
     *     e sono stati implementati i suoi due metodi di callback setOnQueryTextListener
     *     e onQueryTextChange.
     * </p>
     */
    public void manageSearchBarTextListener() {
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(listAdapter != null)
                {
                    listAdapter.getFilter().filter(query);
                }
                return false;
            }
        });
    }




    /**
     * Gestisce il click della freccia presente in alto a sinistra, in base alla modalità in cui
     * ci si trova (sola visualizzazione o modifica delle opere).
     */
    public void manageClickIndietro() {
        frecciaIndietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchbar.clearFocus();

                if (!EDIT_MODE) {
                    onBackPressed();
                }
                else {
                    createExitDialog();
                }
            }
        });
    }


    /**
     * Implementazione del metodo onBackPressed della classe ComponentActivity, che gestisce il
     * tasto indietro del dispositivo.
     * <p>
     *     Se ci si trova in Edit Mode, viene visualizzato un dialog per confermare l'eventuale
     *     annullamento delle modifiche, altrimenti si possono raggiungere activity diverse in base
     *     all'activity da cui si è raggiunta quella corrente.
     * </p>
     */
    @Override
    public void onBackPressed() {
        if (EDIT_MODE) {
            createExitDialog();
        }
        else if (previousActivity.equals("AggiungiOperaActivity") || previousActivity.equals("ListSortingActivity")) {
            Intent intent = new Intent(OpereActivity.this, Homepage.class);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }

    /**
     * Crea e visualizza il dialog di uscita dalla modalità di modifica.
     * <p>
     *     Il titolo e il messaggio del dialog comunicano all'utente che, se conferma l'azione,
     *     le modifiche apportate non saranno salvate. Infatti, tutte le opere che aveva deciso di
     *     eliminare non saranno effettivamente rimosse dal database, ma risulteranno ancora accessibili,
     *     e le opere di cui si era modificata la visibilità saranno riportate allo stato iniziale.
     * </p>
     * <p>
     *     NB : la modifica della visibilità viene riportata direttamente sul database, quindi
     *     viene nuovamente modificata in questo metodo, a differenza dell'eliminazione che non
     *     viene gestita qui, ma viene riportata sul database solo in fase di salvataggio delle
     *     modifiche, diventando irreversibile.
     * </p>
     */
    public void createExitDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(OpereActivity.this);
        builder.setTitle(R.string.uscita_title);
        builder.setMessage(R.string.uscita_message);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // setto a 'false' il flag operaEliminata per tutte le opere della zona (arraylist locale)
                for(int j = 0; j < opereViewModel.opere.size(); j++) {
                    opereViewModel.opere.get(j).setOperaEliminata(false);
                }
                opereDaEliminare.clear();  // svuoto l'arraylist locale delle opere non più eliminate


                // riporto allo stato di partenza la visibilità delle opere sul db
                undoVisibilityChanges();
                opereVisibilitaModificata.clear();

                EDIT_MODE = false;

                setListAdapter();
                fabOpere.setImageResource(R.drawable.ic_view_dashboard_edit);
                fabOpere.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.amber));
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }

    /**
     * Annulla le modifiche effettuate alle visibilità delle opere,
     * in caso di annullamento delle modifiche apportate alle opere.
     * <p>
     *     1. prendo gli id delle opere modificate dall'ArrayList opereModificaVisibilita<br>
     *     2. cerco le opere modificate nell'ArrayList di tutte le opere della zona<br>
     *     3. modifico la visibilita nell'ArrayList (locale)<br>
     *     4. carico la modifica sul database, chiamando il metodo changeVisibilityDB()
     * </p>
     */
    public void undoVisibilityChanges() {
        boolean visibilita;

        for (Integer id : opereVisibilitaModificata) {

            for (int j = 0; j < opereViewModel.opere.size(); j++) {
                if (opereViewModel.opere.get(j).getIdOpera() == id) {
                    visibilita = !opereViewModel.opere.get(j).getVisibilitaOpera();
                    opereViewModel.opere.get(j).setVisibilitaOpera(visibilita);

                    changeVisibilityDB(id, visibilita);

                    break;
                }
            }
        }
    }

    /**
     * Annulla sul database le modifiche effettuate alle visibilità delle opere,
     * in caso di annullamento delle modifiche apportate alle opere.
     * <br><br>
     * @param id numero identificativo dell'opera di cui si sta modificando la visibilità
     * @param visibilita nuova visibilità da salvare nel database per l'opera corrente
     */
    public void changeVisibilityDB(int id, boolean visibilita) {
        Database modificaVisibilitaOpera = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if (output != "true") {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_modifica_visibilita), Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }).execute(Database.FLAG_UPDATE, "UPDATE opere SET Visibilita = " + visibilita + " WHERE idOpere = " + id);
    }


    /**
     * Crea e visualizza il dialog di salvataggio delle modifiche.
     * <p>
     *     Il titolo e il messaggio del dialog comunicano all'utente che, se conferma l'azione, le
     *     modifiche apportate saranno salvate. Tutte le opere che aveva deciso di eliminare adesso
     *     saranno effettivamente rimosse dal database, quindi l'azione è irreversibile e le opere
     *     non sarano più accessibili. Invece, la modifica della visibilità non viene gestita in
     *     questo metodo, in quanto viene modificata direttamente sul db.
     * </p>
     */
    public void createSaveDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(OpereActivity.this);
        builder.setTitle(R.string.salva_opere_title);

        if (opereDaEliminare.isEmpty()) {
            builder.setMessage(R.string.salva_opere_message);
        }
        else {
            builder.setMessage(R.string.salva_opere_message_eliminate);
        }

        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (!opereDaEliminare.isEmpty()) {
                    deleteOpere();
                }

                EDIT_MODE = false;
                exitEditMode();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }


    /**
     * Elimina le opere selezionate, in caso di salvataggio delle modifiche apportate alle opere.
     * <p>
     *     1. prendo gli id delle opere che si è deciso di eliminare dall'ArrayList opereEliminazione<br>
     *     2. cerco le opere suddette nell'ArrayList di tutte le opere della zona<br>
     *     3. rimuovo le opere dall'ArrayList di tutte le opere della zona (locale)<br>
     *     4. rimuovo le opere dal database, chiamando il metodo deleteOperaDB()
     * </p>
     */
    public void deleteOpere() {

        for (Integer id : opereDaEliminare) {

            for (int i = 0; i < opereViewModel.opere.size(); i++) {
                if (opereViewModel.opere.get(i).getIdOpera() == id) {
                    opereViewModel.opere.remove(i);
                    deleteOperaDB(id);
                }
            }
        }
    }

    /**
     * Elimina dal database le opere selezionate, in caso di salvataggio delle modifiche apportate
     * alle opere.
     * <br><br>
     * @param id numero identificativo dell'opera che si sta eliminando
     */
    public void deleteOperaDB(int id) {
        Database eliminaOperaDefinitivamente = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if (output != "true") {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_eliminazione_opera), Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }).execute(Database.FLAG_DELETE, "DELETE FROM opere WHERE idOpere = " + id);
    }


    /**
     * Gestisce l'azione da compiere quando si clicca sul floating action button presente in basso
     * a destra sull'interfaccia, tramite il metodo di callback onClick dell'interfaccia OnClickListener.
     * <p>
     *     Questo button ha due funzionalità diverse in base alla modalità in cui ci si trova:<br>
     *         1. apre/chiude il menu delle possibili funzionalità in fase di visualizzazione delle opere<br>
     *         2. salva le modifiche apportate in fase di modifica delle opere
     * </p>
     */
    public void manageClickBtnOpere() {
        fromBottomAdd = AnimationUtils.loadAnimation(this, R.anim.from_bottom_add_button);
        fromBottomEdit = AnimationUtils.loadAnimation(this, R.anim.from_bottom_edit_button);
        toBottomAdd = AnimationUtils.loadAnimation(this, R.anim.to_bottom_add_button);
        toBottomEdit = AnimationUtils.loadAnimation(this, R.anim.to_bottom_edit_button);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);


        fabOpere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (EDIT_MODE) { // è il button che salva le modifiche apportate alle opere
                    searchbar.clearFocus();
                    createSaveDialog();
                }
                else {  // è il button che apre il menu per modificare il percorso o aggiungere un'opera
                    if (MENU_SELECTED) {
                        fabOpere.startAnimation(rotateForward);
                        fabOpere.setImageResource(R.drawable.ic_close_cross);
                        fabOpere.startAnimation(rotateForward);
                        openFabMenu();  enableAddEditButtons();
                    }
                    else {
                        closeFabMenu(); disableAddEditButtons();
                    }
                }
            }
        });
    }


    /**
     * Apre il menu delle funzionalità, impostando la visibilità <i>VISIBLE</i> ai floating action
     * button del menu, in modo tale che l'utente possa selezionare l'azione da compiere.
     */
    private void openFabMenu() {
        listaOpere.setAlpha(0.3F);
        layoutAggiuntaOpere.setVisibility(View.VISIBLE);
        layoutAggiuntaOpere.startAnimation(fromBottomAdd);
        layoutModificaOpere.setVisibility(View.VISIBLE);
        layoutModificaOpere.startAnimation(fromBottomEdit);
        layoutModificaPercorso.setVisibility(View.VISIBLE);
        layoutModificaPercorso.startAnimation(fromBottomEdit);
        MENU_SELECTED = false;
    }

    /**
     * Chiude il menu delle funzionalità, impostando la visibilità <i>GONE</i> ai floating action
     * button del menu.
     */
    private void closeFabMenu() {
        fabOpere.startAnimation(rotateBackward);
        fabOpere.setImageResource(R.drawable.ic_view_dashboard_edit);
        fabOpere.startAnimation(rotateBackward);

        listaOpere.setAlpha(1.0F);
        layoutAggiuntaOpere.startAnimation(toBottomAdd);
        layoutAggiuntaOpere.setVisibility(View.GONE);
        layoutModificaOpere.startAnimation(toBottomEdit);
        layoutModificaOpere.setVisibility(View.GONE);
        layoutModificaPercorso.startAnimation(toBottomEdit);
        layoutModificaPercorso.setVisibility(View.GONE);
        MENU_SELECTED = true;
    }

    /**
     * Disattiva la possibilità di cliccare i floating action button del menu, quando il menu è chiuso.
     */
    private void disableAddEditButtons() {
        fabAggiuntaOpere.setClickable(false);
        layoutAggiuntaOpere.setClickable(false);
        fabModificaOpere.setClickable(false);
        layoutModificaOpere.setClickable(false);
        fabModificaPercorso.setClickable(false);
        layoutModificaPercorso.setClickable(false);
    }

    /**
     * Attiva la possibilità di cliccare i floating action button del menu, quando il menu è aperto.
     */
    private void enableAddEditButtons() {
        fabAggiuntaOpere.setClickable(true);
        layoutAggiuntaOpere.setClickable(true);
        fabModificaOpere.setClickable(true);
        layoutModificaOpere.setClickable(true);
        fabModificaPercorso.setClickable(true);
        layoutModificaPercorso.setClickable(true);
    }

    /**
     * Nasconde i floating action button del menu che permettono la modifica delle opere o del
     * percorso, quando la zona selezionata in homepage è vuota, dunque non contiene opere.
     */
    private void hideEditButtons() {
        fabModificaOpere.setVisibility(View.GONE);
        labelModificaOpere.setVisibility(View.GONE);
        layoutModificaOpere.setVisibility(View.GONE);
        fabModificaPercorso.setVisibility(View.GONE);
        labelModificaPercorso.setVisibility(View.GONE);
        layoutModificaPercorso.setVisibility(View.GONE);
    }


    /**
     * Gestisce il click dell'utente sul floating action button del menu che permette la modifica
     * delle opere (cambio visibilità ed eliminazione), tramite il metodo di callback onClick
     * dell'interfaccia OnClickListener.
     * <p>
     *     La modalità di modifica è adesso attiva.
     * </p>
     */
    public void manageClickModificaOpere() {
        fabModificaOpere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EDIT_MODE = true;
                searchbar.clearFocus();

                setListAdapter();

                closeFabMenu(); disableAddEditButtons();
                enterEditMode();
            }
        });
    }

    /**
     * Preparazione alle azioni che si possono compiere in Edit Mode:<br>
     * 1. modifica del button in basso a destra, per renderlo il button di salvataggio delle modifiche<br>
     * 2. rimozione degli elementi dalle ArrayList in cui verranno salvati gli ID delle opere modificate
     */
    private void enterEditMode() {
        fabOpere.startAnimation(rotateForward);
        fabOpere.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.cactus_light));
        fabOpere.setImageResource(R.drawable.ic_baseline_check_24);
        fabOpere.startAnimation(rotateForward);

        opereVisibilitaModificata.clear();
        opereDaEliminare.clear();
    }

    /**
     * Uscita dalla modalità di modifica e preparazione alla modalità di sola visualizzazione.
     * <p>
     *     Il button in basso a destra viene modificato, per renderlo il button con cui si apre il
     *     menu delle funzionalità applicabili alle opere.
     * </p>
     */
    private void exitEditMode() {
        disableAddEditButtons();

        setListAdapter();

        fabOpere.startAnimation(rotateBackward);
        fabOpere.setImageResource(R.drawable.ic_view_dashboard_edit);
        fabOpere.startAnimation(rotateBackward);
        fabOpere.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.amber));
    }


    /**
     * Gestisce le azioni da compiere quando viene modificata la visibilita di un'opera e salva
     * la modifica effettuata sul database.
     * <p>
     *     Quando la visibilità di un'opera viene modificata, l'ID dell'opera viene inserito
     *     nell'ArrayList opereVisibilitaModificata.
     * </p>
     * <p>
     *     NB : se l'ID dell'opera è già presente nell'ArrayList, vuol dire che la visibilità
     *     dell'opera era stata già modificata, quindi viene rimossa dall'ArrayList (essendo un
     *     tipo di dato boolean, se viene modificata due volte, è tornata allo stato di partenza).
     * </p>
     * <br>
     * @param view opera cliccata, da cui si può ricavare l'ID
     */
    public void clickVisibilita(View view) {
        int indexOperaCurr = -1;
        int idOperaCliccata = (int) view.getTag();
        boolean visibile = true;


        indexOperaCurr = getIndexByID(idOperaCliccata);
        if (indexOperaCurr == -1) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_modifica_visibilita), Toast.LENGTH_SHORT).show();
        }
        visibile = !opereViewModel.opere.get(indexOperaCurr).getVisibilitaOpera();



        if (!opereVisibilitaModificata.contains(idOperaCliccata)) {
            opereVisibilitaModificata.add(idOperaCliccata);
        }
        else {
            opereVisibilitaModificata.remove(Integer.valueOf(idOperaCliccata));
        }

        opereViewModel.opere.get(indexOperaCurr).setVisibilitaOpera(visibile);
        changeVisibilityDB(idOperaCliccata, visibile);

        listAdapter.notifyDataSetChanged();
    }


    /**
     * Restituisce l'indice dell'opera nell'ArrayList di tutte le opere della zona, cercandola
     * tramite il suo ID.
     * <br><br>
     * @param id ID dell'opera di cui si vuole recuperare l'indice
     * @return indice dell'opera nell'ArrayList delle opere della zona corrente
     */
    public int getIndexByID(int id) {
        int index = -1;

        for(int i = 0; i < opereViewModel.opere.size(); i++) {
            if(opereViewModel.opere.get(i).getIdOpera() == id) {
                // l'opera è stata trovata
                index = i;
                break;
            }
        }

        return index;
    }


    /**
     * Gestisce le azioni da compiere quando si elimina un'opera in fase di modifica.
     * <p>
     *     Mostra un dialog per chiedere conferma all'utente dell'azione che  si sta compiendo.
     * </p>
     * <br>
     * @param view opera cliccata, da cui si può ricavare l'ID
     */
    public void clickEliminazione(View view) {
        int idOperaCliccata = (int) view.getTag();

        createDeletionDialog(idOperaCliccata);
    }

    /**
     * Crea e visualizza il dialog di conferma dell'eliminazione dell'opera selezionata.
     * <p>
     *     Il titolo e il messaggio del dialog comunicano all'utente che, se conferma l'azione,
     *     l'opera verrà eliminata nel momento del salvataggio delle modifiche.
     * </p>
     * <p>
     *     In particolare, se l'utente conferma la scelta, l'ID dell'opera viene inserita
     *     nell'ArrayList opereDaEliminare e nell'ArrayList di tutte le opere della zona
     *     viene impostato al valore <i>true</i> il flag operaEliminata.
     * </p>
     * <br>
     * @param id ID dell'opera che si è deciso di eliminare
     */
    public void createDeletionDialog(int id) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(OpereActivity.this);
        builder.setTitle(R.string.eliminazione_title);
        builder.setMessage(R.string.eliminazione_message);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                opereDaEliminare.add(id);

                int indexOperaCurr = getIndexByID(id);
                opereViewModel.opere.get(indexOperaCurr).setOperaEliminata(true);

                listAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }

    /**
     * Gestisce le azioni da compiere quando si decide di annullare l'eliminazione di un'opera.
     * <p>
     *     Infatti, l'ID dell'opera viene rimosso dall'ArrayList opereDaEliminare e nell'ArrayList
     *     di tutte le opere della zona viene impostato al valore <i>false</i> il flag oepraEliminata.
     * </p>
     * <br>
     * @param view opera cliccata, da cui si può ricavare l'ID
     */
    public void annullaEliminazione(View view) {
        int idOperaCliccata = (int) view.getTag();

        opereDaEliminare.remove(Integer.valueOf(idOperaCliccata));

        int indexOperaCurr = getIndexByID(idOperaCliccata);
        opereViewModel.opere.get(indexOperaCurr).setOperaEliminata(false);

        listAdapter.notifyDataSetChanged();
    }


    /**
     * Gestisce il click dell'utente sul floating action button del menu che permette la modifica
     * del percorso (ordine delle opere), tramite il metodo di callback onClick dell'interfaccia
     * OnClickListener.
     */
    public void manageClickModificaPercorso() {
        fabModificaPercorso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu(); disableAddEditButtons();

                Intent i = new Intent(OpereActivity.this, ListSortingActivity.class);
                i.putExtra("zona", nomeZona);
                startActivity(i);
            }
        });
    }


    /**
     * Gestisce il click dell'utente sul floating action button del menu che permette l'aggiunta
     * di una nuova opera al museo, tramite il metodo di callback onClick dell'interfaccia
     * OnClickListener.
     */
    public void manageClickAggiungi() {
        fabAggiuntaOpere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu(); disableAddEditButtons();

                Intent i = new Intent(OpereActivity.this, AggiungiOperaActivity.class);
                startActivity(i);
            }
        });
    }
}
