package it.uniba.pathway;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


/**
 * L'activity ListSortingActivity permette di visualizzare e modificare l'ordine delle opere
 * all'interno della zona, effettuando azioni di drag&drop sulla lista.
 */
public class ListSortingActivity extends AppCompatActivity {

    public static String ID_MUSEO;
    private String nomeZona = "Zona Salone";

    private RelativeLayout listaDragDropLayout;

    private Opera opera;
    private ArrayList<Opera> opere;
    private OpereViewModel opereViewModel;

    private boolean CARICAMENTO_OPERE_AVVIATO;
    private boolean OPERE_CARICATE;

    private CustomListView listaOpereDD;
    private ListDragDropAdapter listDragDropAdapter;

    private ImageButton frecciaIndietroLista;
    private FloatingActionButton fabSalvaOrdinamento;
    private LinearLayout caricamentoOpereDD;

    LinearLayout layoutAggiornamentoPosizioni;
    private int indexUpdates;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sorting);


        GestioneDellaSessione sessione = new GestioneDellaSessione(ListSortingActivity.this);
        ID_MUSEO = sessione.getSessione();


        lockDeviceRotation(true);


        OPERE_CARICATE = false;  // le opere non sono ancora state prelevate tutte dal db
        CARICAMENTO_OPERE_AVVIATO = false; // la chiamata al db per le opere non è stata ancora avviata

        if(savedInstanceState != null) {
            CARICAMENTO_OPERE_AVVIATO = savedInstanceState.getBoolean("caricamentoAvviato");
        }


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeZona = extras.getString("zona");
        }


        // inizializzazione degli elementi del layout
        frecciaIndietroLista = (ImageButton) findViewById(R.id.frecciaIndietroLista);
        frecciaIndietroLista.setVisibility(View.GONE);

        fabSalvaOrdinamento = (FloatingActionButton) findViewById(R.id.fabSalvaOrdinamento);
        fabSalvaOrdinamento.setVisibility(View.GONE);

        caricamentoOpereDD = (LinearLayout) findViewById(R.id.layout_caricamento_opere_DD);
        listaOpereDD = (CustomListView) findViewById(R.id.listaOpereDragDrop);

        layoutAggiornamentoPosizioni = (LinearLayout) findViewById(R.id.layout_aggiornamento_posizioni);
        layoutAggiornamentoPosizioni.setVisibility(View.GONE);


        opere = new ArrayList<Opera>();
        opereViewModel = new ViewModelProvider(this).get(OpereViewModel.class);
        // '-> ViewModel viene solo ri-inizializzato al refresh dell'activity, non ri-creato


        if (!opereViewModel.opere.isEmpty()) {  // se l'arraylist di opere è stata già popolata
            setListDragDropAdapter();
        }
        else if (!CARICAMENTO_OPERE_AVVIATO) {
            CARICAMENTO_OPERE_AVVIATO = true;
            getOpereFromDB();
        }


        // gestione del click della freccia per tornare indietro
        manageClickIndietroLista();

        // gestione del click al Floating Action Button che permette di salvare l'ordinamento
        manageClickSalvaOrdinamento();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("caricamentoAvviato", CARICAMENTO_OPERE_AVVIATO);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CARICAMENTO_OPERE_AVVIATO = savedInstanceState.getBoolean("caricamentoAvviato");
    }



    @Override
    public void onBackPressed() {
        createExitDialog();
    }


    /**
     * Attiva o disattiva (mantenendo la configurazione corrente) la rotazione dello schermo.
     * <p>
     *     La seconda condizione si verifica solo in fase di caricamento e di salvataggio delle opere.
     *     Infatti, è stato implementato perchè, se si ruota lo schermo durante queste azioni,
     *     l'overlay che ne indica il caricamento, non assume visibilità <i>GONE</i>
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }


    /**
     * Disattiva la visibilità di alcuni elementi dell'interfaccia e la attiva per altri, in modo da
     * poter permettere all'utente di interagirvi al completamento del caricamento delle opere.
     */
    public void showViews() {
        caricamentoOpereDD.setVisibility(View.GONE);
        lockDeviceRotation(false);  // rotazione schermo abilita

        frecciaIndietroLista.setVisibility(View.VISIBLE);
        fabSalvaOrdinamento.setVisibility(View.VISIBLE);

        listaOpereDD.setVisibility(View.VISIBLE);
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
                System.out.println("Output: " + output);

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

                    opereViewModel.addAllOpere(opere);
                    setListDragDropAdapter();

                } catch (Exception e) {

                }
            }

        }).execute(Database.FLAG_SELECT_RAW, "SELECT * FROM opere WHERE idMuseo = " + ID_MUSEO + " AND Zona = '" + nomeZona + "' AND Visibilita = 1 ORDER BY Posizione");
    }



    /**
     * Crea una nuova istanza di ListDragDropAdapter ed effettua la sua inizializzazione.
     * <p>
     *     In particolare, al costruttore di ListDragDropAdapter viene fornito il contesto, l'ArrayList
     *     di opere con cui si deve popolare la lista e l'interfaccia Listener dichiarata nella
     *     classe ListDragDropAdapter, con l'implementazione del suo metodo onGrab.
     * </p>
     * <p>
     *     Inoltre, viene implementato il metodo swapElements dell'interfaccia Listener della
     *     classe CustomListView, che viene chiamato a ogni movimento delle opere, infatti le opere
     *     vengono scambiate a due a due tramite questo metodo direttamente nell'ArrayList delle opere
     *     della classe OpereViewModel fino a raggiungere la posizione finale decisa dall'utente.<br>
     *     In questo modo, l'attributo posizione dell'opera sarà aggiornato assegnando [indice attuale
     *     nell'ArrayList opere della classe OpereViewModel + 1].
     * </p>
     */
    public void setListDragDropAdapter() {
        listDragDropAdapter = new ListDragDropAdapter(ListSortingActivity.this, opereViewModel.opere, new ListDragDropAdapter.Listener() {
            @Override
            public void onGrab(int position, ConstraintLayout row) {
                listaOpereDD.onGrab(position, row);
            }
        });

        listaOpereDD.setAdapter(listDragDropAdapter);
        listaOpereDD.setListener(new CustomListView.Listener() {
            @Override
            public void swapElements(int indexOne, int indexTwo) {

                Opera temp = opereViewModel.opere.get(indexOne);
                opereViewModel.opere.set(indexOne, opereViewModel.opere.get(indexTwo));
                opereViewModel.opere.set(indexTwo, temp);

                opereViewModel.opere.get(indexOne).setPosizione(indexOne+1);
                opereViewModel.opere.get(indexTwo).setPosizione(indexTwo+1);
            }
        });

        showViews();
    }


    /**
     * Gestisce il click della freccia presente in alto a sinistra.
     */
    public void manageClickIndietroLista() {
        frecciaIndietroLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createExitDialog();
            }
        });
    }

    /**
     * Crea e visualizza il dialog di uscita dalla modifica del percorso.
     * <p>
     *     Il titolo e il messaggio del dialog comunicano all'utente che, se conferma l'azione,
     *     le modifiche apportate non saranno salvate. Infatti, le opere verranno visualizzate
     *     secondo l'ordine precedente alle modifiche.
     * </p>
     */
    public void createExitDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ListSortingActivity.this);
        builder.setTitle(R.string.uscita_title);
        builder.setMessage(R.string.uscita_message);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListSortingActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }


    /**
     * Gestisce il click del Floating Action Button che permette di salvare il nuovo ordinamento.
     */
    public void manageClickSalvaOrdinamento() {
        fabSalvaOrdinamento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSaveDialog();
            }
        });
    }

    /**
     * Crea e visualizza il dialog di salvataggio delle modifiche.<br>
     * <p>
     *     Il titolo e il messaggio del dialog comunicano all'utente che, se conferma l'azione, le
     *     modifiche apportate all'ordine delle opere saranno salvate.
     * </p>
     * <p>
     *     In particolare, viene visualizzato l'overlay che comunica all'utente che si stanno
     *     salvando le nuove opere nel database, disattivando la rotazione dello schermo, altrimenti
     *     l'overlay non assumerebbe visibilità <i>GONE</i> appena termina l'azione di caricamento.
     * </p>
     * <p>
     *     NB : l'ArrayList locale delle opere viene modificato in tempo reale, infatti le opere
     *     vengono scambiate volta per volta direttamente nell'ArrayList, dunque qui viene gestita
     *     solo la modifica sul database.
     * </p>
     */
    public void createSaveDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ListSortingActivity.this);
        builder.setTitle(R.string.salva_percorso_title);
        builder.setMessage(R.string.salva_percorso_message);
        builder.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                layoutAggiornamentoPosizioni.setVisibility(View.VISIBLE);

                indexUpdates = opereViewModel.opere.size(); // controllo il numero di query terminate

                for (int j = 0; j < opereViewModel.opere.size(); j++) {
                    updatePositions(opereViewModel.opere.get(j).getIdOpera(), opereViewModel.opere.get(j).getPosizione());
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_negative_button, null);
        builder.show();
    }

    /**
     * Salva le posizioni aggiornate delle opere sul database, una per volta.
     * <p>
     *     In particolare, ogni volta che termina ogni chiamata al database, l'indice indexUpdated
     *     viene decrementato, in modo tale che solo quando raggiunge lo zero, ovvero quando tutte
     *     le query di aggiornamento sono state completate, l'utente viene riportato alla lista
     *     aggiornata delle opere in modalità di sola visualizzazione.
     * </p>
     * <br>
     * @param id ID dell'opera di cui si sta aggiornando la posizione
     * @param newPosition posizione nuova dell'opera corrente all'interno della zona
     */
    public void updatePositions(int id, int newPosition) {
        Database aggiornamentoPosizioni = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if (output != "true") {
                    Toast.makeText(getApplicationContext(), getString(R.string.errore_aggiornamento_lista), Toast.LENGTH_SHORT);
                }

                indexUpdates--;
                if (indexUpdates == 0) {
                    layoutAggiornamentoPosizioni.setVisibility(View.GONE);

                    Intent intent = new Intent(ListSortingActivity.this, OpereActivity.class);
                    intent.putExtra("nome_zona", nomeZona);
                    intent.putExtra("previousActivity", "ListSortingActivity");
                    startActivity(intent);
                }
            }

        }).execute(Database.FLAG_UPDATE, "UPDATE opere SET Posizione = " + newPosition + " WHERE idOpere = " + id);
    }
}
