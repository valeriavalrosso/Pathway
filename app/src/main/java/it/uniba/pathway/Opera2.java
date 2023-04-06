package it.uniba.pathway;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class Opera2 extends AppCompatActivity {

    private int STORAGE_PERMISSION_CODE = 1;
    private TextView Title;
    private TextView Zone;
    private String zona;
    private ImageView Qr;
    private Button Download;
    private Button Delete;
    public static Bitmap BitPhoto = null;
    int num;
    int act;
    private ProgressBar Progress;
    ConstraintLayout layout;
    private ImageButton Nav;
    private static final String image_url = "https://uniqueandrocode.000webhostapp.com/hiren/androidtutorial/demo.png";

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        num = intent.getIntExtra("NUM", 0);
        act = intent.getIntExtra("ACT", 0);

        setContentView(R.layout.activity_opera2);
        Title = findViewById(R.id.title);
        Zone = findViewById(R.id.zone);
        Qr = findViewById(R.id.qr);
        Download = findViewById(R.id.download);
        Delete = findViewById(R.id.delete);
        layout = findViewById(R.id.constraint);
        Progress = findViewById(R.id.progress);
        Nav = findViewById(R.id.nav);

        layout.setOnTouchListener(new OnSwipeTouchListener(Opera2.this) {
            @Override
            public void onSwipeRight() {
                //openNewActivity();
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

                        Download.setVisibility(View.VISIBLE);
                        Delete.setVisibility(View.VISIBLE);
                        Title.setVisibility(View.VISIBLE);
                        Zone.setVisibility(View.VISIBLE);
                        Nav.setVisibility(View.VISIBLE);
                        Progress.setVisibility(View.GONE);

                        Title.setText(Database.rs.getString("Nome"));
                        zona = Database.rs.getString("Zona");
                        Zone.setText(zona);

                        byte[] immagineByte = Database.rs.getBytes("QR"); //Cambiare campo con Qr
                        if (immagineByte == null) {
                        } else {
                            BitPhoto = BitmapFactory.decodeByteArray(immagineByte, 0, immagineByte.length);
                            Qr.setImageBitmap(BitPhoto);
                        }
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }).execute(Database.FLAG_SELECT_RAW, "SELECT * FROM opere where idOpere=" + num, "8");


        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Opera2.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    download();
                } else {
                    requestStoragePermission();
                }
            }

        });

        AlertDialog Dialogdel = new AlertDialog.Builder(this).create();

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialogdel.setTitle(R.string.eliminazione_opera_title);

                Dialogdel.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.eliminazione_opera_postive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                            }
                        }).execute(Database.FLAG_DELETE, "DELETE FROM opere where idOpere=" + num);

                        Intent intent = new Intent(Opera2.this, OpereActivity.class);
                        intent.putExtra("nome_zona", zona);
                        intent.putExtra("previousActivity", "Opera2");
                        startActivity(intent);
                    }
                });

                Dialogdel.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.eliminazione_opera_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                Dialogdel.show();
            }
        });


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottombar);

        bottomNavigationView.getMenu().findItem(R.id.scanner).setCheckable(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scanner:
                        startActivity(new Intent(getApplicationContext(), Request_Camera.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), Homepage.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.impostazioni:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class)); // SOSTITUIRE MainActivity con activity delle impostazioni (Omer) | Temporaneamente sostituito con il logout di chry
                        overridePendingTransition(0, 0);
                        return true;
                }

                return false;
            }
        });


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

    private static Bitmap viewToBitmap(View view, int widh, int hight) {
        Bitmap bitmap = Bitmap.createBitmap(widh, hight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void refreshGallary(File file) {
        Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        i.setData(Uri.fromFile(file));
        sendBroadcast(i);
    }

    private File getdisc() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(file, "My Image");
    }

    public void download() {


        FileOutputStream fileOutputStream = null;
        File file = getdisc();

        if (!file.exists() && !file.mkdirs()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_accesso_archivio), Toast.LENGTH_LONG).show();
            return;
        }

        String name = "Opera" + num + ".jpeg";
        String file_name = file.getAbsolutePath() + "/" + name;
        File new_file = new File(file_name);

        try {
            fileOutputStream = new FileOutputStream(new_file);
            Bitmap bitmap = viewToBitmap(Qr, Qr.getWidth(), Qr.getHeight());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.immagine_salvata_successo), Toast.LENGTH_LONG).show();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch
        (FileNotFoundException e) {

        } catch (IOException e) {

        }
        refreshGallary(file);
    }
    public void openNewActivity() {
        Intent intent = new Intent(this, OperaDettagliata.class);
        intent.putExtra("NUM", num);
        intent.putExtra("ACT", act);
        startActivity(intent);
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.richiesta_archivio_title)
                    .setMessage(R.string.richiesta_archivio_message)
                    .setPositiveButton(getResources().getString(R.string.richiesta_archivio_positive_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Opera2.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.richiesta_archivio_negative_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download();
            } else {
                Toast.makeText(this, getResources().getString(R.string.permesso_negato_scrittura_file), Toast.LENGTH_SHORT).show();

            }
        }

    }

}