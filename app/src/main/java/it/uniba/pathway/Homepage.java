package it.uniba.pathway;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.uniba.pathway.Helper.MyItemTouchHelperCallback;

public class Homepage extends AppCompatActivity {

    private FloatingActionButton fabMain;
    private FloatingActionButton fabEdit;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabSave;
    private FloatingActionButton fabExport;


    private EditText zoneName;
    private Button confirm, annulla;
    Dialog dialogg;

    public static String ID_MUSEO;    // SOSTITUIRE ID MUSEO CON SHARED PREFERENCES
    private String nomeMuseo;
    private byte[] immagineMuseo;

    //public static String ID_MUSEO;



    Map<String, String> map;

    GridLayout grid;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    Adapter adapter;

    ItemTouchHelper itemTouchHelper;

    Animation fromBottomAdd, fromBottomEdit, toBottomAdd, toBottomEdit, rotateForward, rotateBackward;
    boolean isOpen = false; //default: false
    ProgressDialog progressDialog;

    LinearLayout layoutAggiuntaZone, layoutModificaZone, layoutEsportaPercorso;
    BottomNavigationView bottomNavigationView;

    private SearchView search_bar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GestioneDellaSessione sessione = new GestioneDellaSessione(Homepage.this);
        ID_MUSEO = sessione.getSessione();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        search_bar = (SearchView) findViewById(R.id.search_bar);



        if(savedInstanceState!=null)
        {
            adapter.titles = savedInstanceState.getStringArrayList("nomiZoneCercate");
        }

        if(sessione.getFlagDatiMuseo())
        {

            Database nomeImmagineMuseo = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    System.out.println("Output: " + output);


                    try {
                        while (Database.rs.next()) {
                            nomeMuseo = Database.rs.getString("Nome");
                            immagineMuseo = Database.rs.getBytes("FotoMuseo");
                        }

                    } catch (Exception e) {
                        Toast.makeText(Homepage.this, getResources().getString(R.string.errore_server_catch), Toast.LENGTH_SHORT).show();
                    }

                    if (immagineMuseo != null) {
                        sessione.setImmagineMuseo(immagineMuseo);
                    }

                    sessione.setNomeMuseo(nomeMuseo);
                    sessione.setFlagDatiMuseo(false);

                }

            }).execute(Database.FLAG_SELECT_RAW, "SELECT Nome,FotoMuseo FROM musei WHERE idMuseo = " + ID_MUSEO);
        }





        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar);

        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.scanner:
                        startActivity(new Intent(getApplicationContext(), Request_Camera.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.impostazioni:
                        Intent i = new Intent(Homepage.this, SettingsActivity.class);
                        //i.putExtra("nome_museo", nomeMuseo);
                        //i.putExtra("immagine_museo", immagineMuseo);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                        return true;
                }

                return false;
            }
        });

        map = new HashMap<String, String>();
        ArrayList<Button> zones = new ArrayList<Button>();

        recyclerView = findViewById(R.id.recyclerview);
        //recyclerView.setHasFixedSize(true);


        fabMain = (FloatingActionButton) findViewById(R.id.floatingActionButton4);
        fabEdit = (FloatingActionButton) findViewById(R.id.floatingActionButton5);
        fabAdd = (FloatingActionButton) findViewById(R.id.floatingActionButton6);
        fabSave = (FloatingActionButton) findViewById(R.id.floatingActionButtonSalva);
        fabExport = (FloatingActionButton) findViewById(R.id.floatingActionButtonExport);

        fromBottomAdd = AnimationUtils.loadAnimation(this, R.anim.from_bottom_add_button);
        fromBottomEdit = AnimationUtils.loadAnimation(this, R.anim.from_bottom_edit_button);
        toBottomAdd = AnimationUtils.loadAnimation(this, R.anim.to_bottom_add_button);
        toBottomEdit = AnimationUtils.loadAnimation(this, R.anim.to_bottom_edit_button);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);


        layoutAggiuntaZone = (LinearLayout) findViewById(R.id.layoutAggiuntaZone);
        layoutModificaZone = (LinearLayout) findViewById(R.id.layoutModificaZone);
        layoutEsportaPercorso = (LinearLayout) findViewById(R.id.layoutEsportaPercorso);



        startLoader();


        populateZone();




        // Fab Main
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAggiuntaZone.getVisibility()== View.VISIBLE && layoutModificaZone.getVisibility()== View.VISIBLE && layoutEsportaPercorso.getVisibility()== View.VISIBLE) {
                    fabMain.startAnimation(rotateBackward);
                    fabMain.setImageResource(R.drawable.ic_view_dashboard_edit);
                    fabMain.startAnimation(rotateBackward);

                    closeFabMenu(); disableButtons();

                } else {
                    fabMain.startAnimation(rotateForward);
                    fabMain.setImageResource(R.drawable.ic_close_cross);
                    fabMain.startAnimation(rotateForward);

                    openFabMenu();  enableButtons();

                }
            }
        });


        // Add zone
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDialg();

                fabMain.startAnimation(rotateBackward);
                fabMain.setImageResource(R.drawable.ic_view_dashboard_edit);
                fabMain.startAnimation(rotateBackward);
                closeFabMenu(); disableButtons();
            }
        });


        //Edit zone
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(Homepage.this, "Modifica Zona", Toast.LENGTH_SHORT).show();

                startLoader();

                fabMain.startAnimation(rotateForward);
                fabMain.setVisibility(View.INVISIBLE);
                fabSave.setVisibility(View.VISIBLE);
                fabSave.startAnimation(rotateForward);
                closeFabMenu(); disableButtons();

                populateZoneAll();

            }
        });


        //Esporta l'intero percorso
        fabExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Homepage.this, "Esportazione del percorso in corso...", Toast.LENGTH_LONG).show();
                // toast visibile per 5 secondi circa (con LENGTH_SHORT sono circa 3 secondi)

                fabMain.startAnimation(rotateBackward);
                fabMain.setImageResource(R.drawable.ic_view_dashboard_edit);
                fabMain.startAnimation(rotateBackward);
                closeFabMenu(); disableButtons();

                    Intent intent = new Intent(Homepage.this, Generazione_Percorso.class);
                    // eventuali extra
                    startActivity(intent);

            }
        });

        //Save zone
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startLoader();

                fabSave.startAnimation(rotateBackward);
                fabSave.setVisibility(View.GONE);
                fabMain.setImageResource(R.drawable.ic_view_dashboard_edit);
                fabMain.setVisibility(View.VISIBLE);
                fabMain.startAnimation(rotateBackward);

                System.out.println(adapter.ids.toString());
                System.out.println(adapter.ids.size()+1);

                for (int i=0; i<adapter.ids.size(); i++){
                    adapter.ids.get(i);

                    sortZone(adapter.ids.get(i), i+1);
                }

                populateZone();

            }
        });


    }

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

    private void disableButtons() {
        fabAdd.setClickable(false);
        fabEdit.setClickable(false);
        fabExport.setClickable(false);
    }

    private void enableButtons() {
        fabAdd.setClickable(true);
        fabEdit.setClickable(true);
        fabExport.setClickable(true);
    }


    //Ordinamento delle zone nel database, dopo che è stato effettuato, e salvato, un loro spostamento tramite il drag & drop
    public void sortZone(Integer integer, int i) {
        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {


            }
        }).execute(Database.FLAG_UPDATE, "UPDATE zone SET sequence="+ i +" where idMuseo="+ ID_MUSEO +" && id="+ integer, "5");

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("nomiZoneCercate", (ArrayList<String>) Adapter.titles);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        adapter.titles = savedInstanceState.getStringArrayList("nomiZoneCercate");
    }

    @Override
    protected void onResume() {
        bottomNavigationView.setSelectedItemId(R.id.home);
        super.onResume();
    }

    //Inserimento di una nuova zona nel database (il suo nome nel popup che appare se si clicca sul fabAdd)
    public void createNewDialg() {
        //POPUP
        dialogg = new Dialog(Homepage.this);
        dialogg.setContentView(R.layout.custom_dialog);
        dialogg.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        dialogg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogg.setCancelable(false);

        zoneName = (EditText) dialogg.findViewById(R.id.conferma);
        Button confirm = dialogg.findViewById(R.id.btn_confirm);
        Button annulla = dialogg.findViewById(R.id.btn_cancel);

        dialogg.show();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = zoneName.getText().toString();

                if (!name.isEmpty()) {
                    //caricamento..insert DB
                    startLoader();

                    Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                        @Override
                        public void processFinish(String output) {

                            boolean result = Boolean.parseBoolean(output);

                            if (result) {
                                populateZone();
                                dialogg.dismiss();
                                progressDialog.dismiss();
                                Toast.makeText(Homepage.this, getString(R.string.zona)+": " + name + getString(R.string.inserita_con_successo), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Homepage.this, getString(R.string.errore_inserimento), Toast.LENGTH_SHORT).show();
                                dialogg.dismiss();
                            }


                        }
                    }).execute(Database.FLAG_INSERT, "insert into zone(Nome,idMuseo,sequence) values (\"" + name + "\"," + ID_MUSEO + "," + (adapter.ids.size()+1) + ");");


                } else {
                    Toast.makeText(Homepage.this, getString(R.string.nome_invalido), Toast.LENGTH_SHORT).show();
                }

            }
        });

        annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogg.dismiss();
            }
        });


    }

    //Mostra tutte le zone, visibili e non visibili, e nell'ordine salvato tramite connessione al database, nella sezione di modifica
    public void populateZoneAll() {
        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

            List<Integer> visibility = new ArrayList<Integer>();
            List<String> titles = new ArrayList<String>();
            List<Integer> ids = new ArrayList<Integer>();
            ArrayList<ItemClass> list = new ArrayList<>();


            @Override
            public void processFinish(String output) {

                recyclerView.removeAllViews();

                progressDialog.dismiss();

                System.out.println(output);
                String[] zone = output.split("\n");


                for (String infoZona : zone) {
                    String[] iz = infoZona.split(",");
                    //map.put(iz[0],iz[1]);

                    try {
                        ids.add(Integer.parseInt(iz[0]));
                        titles.add(iz[1]);
                        visibility.add(Integer.parseInt(iz[3]));
                    }catch(NumberFormatException e){

                    }

                }

                ButterKnife.bind(Homepage.this); //
                recyclerView.setHasFixedSize(true);
                GridLayoutManager layoutManager = new GridLayoutManager(Homepage.this, 2);
                recyclerView.setLayoutManager(layoutManager);

                //Set Adapter
                Adapter adapter = new Adapter(Homepage.this, titles, visibility, ids, true, viewHolder -> {
                    itemTouchHelper.startDrag(viewHolder);
                });
                recyclerView.setAdapter(adapter);
                ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(adapter);
                itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(recyclerView);

                //barra di ricerca per le zone presenti
                search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {

                        adapter.getFilter().filter(query);

                        return false;
                    }
                });

            }
        }).execute(Database.FLAG_SELECT, "SELECT * FROM zone where idMuseo="+ ID_MUSEO+" ORDER BY sequence", "5");

    }

    //caricamento
    public void startLoader(){
        progressDialog = new ProgressDialog(Homepage.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    //Mostra le zone visibili e nell'ordine stabilito tramite il drag & drop, tramite connessione al database
    public void populateZone() {
        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

            List<Integer> visibility = new ArrayList<Integer>();
            List<String> titles = new ArrayList<String>();
            List<Integer> ids = new ArrayList<Integer>();
            ArrayList<ItemClass> list = new ArrayList<>();


            @Override
            public void processFinish(String output) {

                recyclerView.removeAllViews();

                progressDialog.dismiss();

                String[] zone = output.split("\n");

                for (String infoZona : zone) {
                    String[] iz = infoZona.split(",");
                    try {
                        ids.add(Integer.parseInt(iz[0]));
                        titles.add(iz[1]);
                        visibility.add(Integer.parseInt(String.valueOf(1)));

                    }catch(NumberFormatException e){

                    }

                }

                adapter = new Adapter(Homepage.this, titles,visibility, ids,false, viewHolder -> {

                });
                GridLayoutManager gridLayoutManager = new GridLayoutManager(Homepage.this,2,GridLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setAdapter(adapter);


                        //barra di ricerca zone
                        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String s) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String query) {

                                adapter.getFilter().filter(query);

                                return false;
                            }
                        });

            }
        }).execute(Database.FLAG_SELECT, "SELECT * FROM zone where idMuseo="+ ID_MUSEO +" && visibility=1 ORDER BY sequence", "5");

    }


    private void closeFabMenu() {
        recyclerView.setAlpha(1.0F);
        layoutAggiuntaZone.startAnimation(toBottomAdd);
        layoutAggiuntaZone.setVisibility(View.GONE);
        layoutModificaZone.startAnimation(toBottomEdit);
        layoutModificaZone.setVisibility(View.GONE);
        layoutEsportaPercorso.startAnimation(toBottomEdit);
        layoutEsportaPercorso.setVisibility(View.GONE);
    }

    private void openFabMenu() {
        recyclerView.setAlpha(0.3F);
        layoutAggiuntaZone.setVisibility(View.VISIBLE);
        layoutAggiuntaZone.startAnimation(fromBottomAdd);
        layoutModificaZone.setVisibility(View.VISIBLE);
        layoutModificaZone.startAnimation(fromBottomEdit);
        layoutEsportaPercorso.setVisibility(View.VISIBLE);
        layoutEsportaPercorso.startAnimation(fromBottomEdit);
    }

}