package it.uniba.pathway;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class OperaDettagliata extends AppCompatActivity {

    private TextView Zone;
    private TextView Title;
    private TextView Description;
    private static String Descrizione;
    private ImageView Photo;
    public static Bitmap BitPhoto = null;
    private ImageButton Edit;
    private ImageButton Back;
    private ImageButton Save;
    private ImageButton Delete;
    private ImageView Camera;
    private TextView Descriptio;
    private ImageButton Nav;
    private String nome_zona;
    private boolean FLAG_MODIFICHE = false;
    ScrollView layout;
    int num;
    int act;
    private ProgressBar Progress;
    Uri immagineSelezionata;

    BottomNavigationView bottomNavigationView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opera);


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar);

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


        Title = findViewById(R.id.title);
        Zone = findViewById(R.id.zone);
        Description = findViewById(R.id.description);
        Descriptio = findViewById(R.id.Descriptio);
        Photo = findViewById(R.id.photo);
        Edit = findViewById(R.id.edit);
        Back = findViewById(R.id.back);
        Save = findViewById(R.id.save);
        Delete = findViewById(R.id.delete);
        Camera = findViewById(R.id.camera);
        layout = findViewById(R.id.scrollview);
        Progress = findViewById(R.id.progress);
        Descriptio = findViewById(R.id.Descriptio);
        Nav = findViewById(R.id.nav);

        Intent intent = getIntent();
        num = intent.getIntExtra("NUM",0);
        act = intent.getIntExtra("ACT",0);
        nome_zona = intent.getStringExtra("nome_zona");

        layout.setOnTouchListener(new OnSwipeTouchListener(OperaDettagliata.this) {
            @Override
            public void onSwipeLeft() {
                openNewActivity();

            }
            public void onSwipeRight() {
                onBackPressed();
            }
        });

        Nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });

        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                try {
                    while (Database.rs.next()) {
                        Descriptio.setVisibility(View.VISIBLE);
                        Edit.setVisibility(View.VISIBLE);
                        Back.setVisibility(View.VISIBLE);
                        Nav.setVisibility(View.VISIBLE);
                        Progress.setVisibility(View.GONE);
                        layout.setAlpha(1F);
                        Title.setText(Database.rs.getString("Nome"));
                        Zone.setText(Database.rs.getString("Zona"));
                        Description.setText(Database.rs.getString("Descrizione"));

                        byte[] immagineByte = Database.rs.getBytes("Immagine");
                        if (immagineByte == null) {
                        } else {
                            BitPhoto = BitmapFactory.decodeByteArray(immagineByte, 0, immagineByte.length);
                            Photo.setImageBitmap(BitPhoto);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }).execute(Database.FLAG_SELECT_RAW, "SELECT * FROM opere where idOpere=" + num, "8");

        AlertDialog Dialogt = new AlertDialog.Builder(this).create();
        EditText EditTitle = new EditText(this);
        AlertDialog Dialogz = new AlertDialog.Builder(this).create();
        EditText EditZone = new EditText(this);
        AlertDialog Dialogd = new AlertDialog.Builder(this).create();
        EditText EditDescription = new EditText(this);
        AlertDialog Dialogs = new AlertDialog.Builder(this).create();
        AlertDialog Dialogdel = new AlertDialog.Builder(this).create();


        Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back.setVisibility(View.GONE);
                Edit.setVisibility(View.GONE);
                Nav.setVisibility(View.GONE);
                Save.setVisibility(View.VISIBLE);
                Delete.setVisibility(View.VISIBLE);
                Camera.setVisibility(View.VISIBLE);
                Photo.setAlpha(150);
                Title.setAlpha(0.7F);
                Zone.setAlpha(0.7F);
                Description.setAlpha(0.7F);

                layout.setOnTouchListener(null);

                Toast.makeText(OperaDettagliata.this, getResources().getString(R.string.apportare_modifiche), Toast.LENGTH_SHORT).show();

                //Modifica Immagine
                Photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        startActivityForResult(intent,2);
                    }
                });

                //Modifica del Titolo
                Dialogt.setTitle(R.string.modifica_titolo);
                Dialogt.setView(EditTitle);
                Dialogt.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.salva), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Title.setText(EditTitle.getText());
                    }
                });
                Dialogt.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.annulla), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditTitle.setText(Title.getText());
                        Dialogt.show();
                    }
                });

                //Modifica della Zona

                Dialogz.setTitle(R.string.modifica_zona);
                Dialogz.setView(EditZone);
                Dialogz.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.salva), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Zone.setText(EditZone.getText());
                    }
                });
                Dialogz.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.annulla), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Zone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditZone.setText(Zone.getText());
                        Dialogz.show();
                    }
                });

                //Modifica della Descrizione

                Dialogd.setTitle(R.string.modifica_descrizione);
                Dialogd.setView(EditDescription);
                Dialogd.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.salva), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Description.setText(EditDescription.getText());
                    }
                });
                Dialogd.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.annulla), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Descriptio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditDescription.setText(Description.getText());
                        Dialogd.show();
                    }
                });
                Description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditDescription.setText(Description.getText());
                        Dialogd.show();
                    }
                });
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogs.setTitle(getString(R.string.salvare_modifiche));
                Dialogs.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.salva), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Nav.setVisibility(View.VISIBLE);
                        Save.setVisibility(View.GONE);
                        Delete.setVisibility(View.GONE);
                        Camera.setVisibility(View.GONE);
                        Edit.setVisibility(View.VISIBLE);
                        Back.setVisibility(View.VISIBLE);
                        Photo.setAlpha(255);
                        Title.setAlpha(1F);
                        Zone.setAlpha(1F);
                        Description.setAlpha(1F);

                        layout.setOnTouchListener(new OnSwipeTouchListener(OperaDettagliata.this) {
                            @Override
                            public void onSwipeLeft() {
                                openNewActivity();

                            }
                            public void onSwipeRight() {
                                onBackPressed();
                            }
                        });

                        Photo.setOnClickListener(null);
                        Title.setOnClickListener(null);
                        Description.setOnClickListener(null);
                        Descriptio.setOnClickListener(null);
                        Zone.setOnClickListener(null);


                        String name = Title.getText().toString();
                        String zone = Zone.getText().toString();
                        String description = Description.getText().toString();
                        Descrizione = Description.getText().toString();
                        System.out.println("Description"+description);

                        FLAG_MODIFICHE = true;

                        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                            @Override
                            public void processFinish(String output) {
                                boolean result=Boolean.parseBoolean(output);

                                if(result){
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.modifiche_opera_successo),Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_modifiche_opera),Toast.LENGTH_SHORT).show();
                                }


                            }
                        }).execute(Database.FLAG_IMAGE_UPLOAD, "UPDATE opere SET Immagine = ?,Nome='"+name+"' ,Zona='"+zone+"' ,Descrizione= '"+ OperaDettagliata.getDescrizione()+"' WHERE idOpere="+num);

                    }
                });

                Dialogs.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.annulla), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Dialogs.show();
            }
        });

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialogdel.setTitle(getString(R.string.delete_edit));

                Dialogdel.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.elimina), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Nav.setVisibility(View.VISIBLE);
                        Save.setVisibility(View.GONE);
                        Delete.setVisibility(View.GONE);
                        Camera.setVisibility(View.GONE);
                        Back.setVisibility(View.VISIBLE);
                        Edit.setVisibility(View.VISIBLE);
                        Photo.setAlpha(255);
                        Title.setAlpha(1F);
                        Zone.setAlpha(1F);
                        Description.setAlpha(1F);
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.modifiche_annullate),Toast.LENGTH_SHORT).show();

                        layout.setOnTouchListener(new OnSwipeTouchListener(OperaDettagliata.this) {
                            @Override
                            public void onSwipeLeft() {
                                openNewActivity();
                            }
                            public void onSwipeRight() {
                                //openPreviousActivity();
                                onBackPressed();
                            }
                        });

                        Photo.setOnClickListener(null);
                        Title.setOnClickListener(null);
                        Description.setOnClickListener(null);
                        Descriptio.setOnClickListener(null);
                        Zone.setOnClickListener(null);

                        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                try {
                                    while (Database.rs.next()) {
                                        Title.setText(Database.rs.getString("Nome"));
                                        Zone.setText(Database.rs.getString("Zona"));
                                        Description.setText(Database.rs.getString("Descrizione"));
                                        byte[] immagineByte = Database.rs.getBytes("Immagine");
                                        if (immagineByte == null) {
                                        } else {
                                            BitPhoto = BitmapFactory.decodeByteArray(immagineByte, 0, immagineByte.length);
                                            Photo.setImageBitmap(BitPhoto);
                                        }
                                    }
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }

                            }
                        }).execute(Database.FLAG_SELECT_RAW, "SELECT * FROM opere where idOpere=" +num, "8");
                    }
                });

                Dialogdel.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.annulla), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Dialogdel.show();
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreviousActivity();
            }
        });

        if(savedInstanceState != null)
        {
            immagineSelezionata = savedInstanceState.getParcelable("imageUri");
        }
        else
        {
            immagineSelezionata = Uri.parse("android:resource://it.uniba.pathway/drawable/ic_baseline_insert_photo_24.xml");
        }
        Photo.setImageURI(immagineSelezionata);
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
        outState.putParcelable("imageUri",this.immagineSelezionata);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.BitPhoto=savedInstanceState.getParcelable("imageBitmap");
        Toast.makeText(getApplicationContext(),getResources().getString(R.string.refresh_di_tutto),Toast.LENGTH_SHORT).show();
        Photo.setImageBitmap(BitPhoto);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null)
        {
            immagineSelezionata = data.getData();
            Photo.setImageURI(immagineSelezionata);
            setImmagineBitmap();
}
    }

    @Override
    public void onBackPressed() {
        openPreviousActivity();
    }

    public void setImmagineBitmap()
    {
        Bitmap bitmap=null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),this.immagineSelezionata);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GestioneImmagine.immagineBitmap = bitmap;
        this.BitPhoto = bitmap;
    }

    public void openNewActivity(){
        Intent intent = new Intent(this, Opera2.class);
        intent.putExtra("NUM",num);
        intent.putExtra("ACT",act);
        this.startActivity(intent);
    }

    public void openPreviousActivity(){
        if(act==1) {
            Intent intent = new Intent(this, Qr_scan.class);
            this.startActivity(intent);
        }
        else{
            if (FLAG_MODIFICHE) {
                Intent intent = new Intent(OperaDettagliata.this, OpereActivity.class);
                intent.putExtra("nome_zona", nome_zona);
                startActivity(intent);
                FLAG_MODIFICHE = false;
            }
            else {
                super.onBackPressed();
            }
        }
    }

    public static String getDescrizione()
    {
        return Descrizione;
    }
}