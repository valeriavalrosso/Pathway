package it.uniba.pathway;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{

    private static final String TITLE_TAG = "settingsActivityTitle";
    private static int ID_MUSEO; //idMuseo preso dalla sessione
    private static String MUSEUM_NAME;
    private static String TELEPHONE;
    private static String PROVINCE;
    private static String CITY;
    private static String POSTAL_CODE;
    private static String ADDRESS;
    private static String VAT_NUMBER;
    private static byte[] MUSEUM_IMAGE;
    private static Bitmap MUSEUM_IMAGE_BITMAP;
    private static Uri IMAGE_PATH;
    private static String VT_LINK;

    private static BottomNavigationView bottomNavigationView;
    private static ImageButton buttonScanner, buttonHome, buttonSettings;

    //logout chry
    private GoogleSignInClient mGoogleSignInClient;
    private static Context contesto;

    private static String PASSWORD;
    private static boolean pswUpdated = true;
    //logout chry


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        //logout chry
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        contesto = this;
        //logout chry

        GestioneDellaSessione sessione = new GestioneDellaSessione(SettingsActivity.this);
        ID_MUSEO = Integer.parseInt(sessione.getSessione());
        MUSEUM_NAME = sessione.getNomeMuseo();
        MUSEUM_IMAGE = sessione.getImmagineMuseo();


        getData();

        //sets language
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        String language = sharedPreferences.getString("language", "");
        setLocale(this,language);


        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.profileSettings, new HeaderFragment())
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new LanguageFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                        }
                    }
                });



        LinearLayout layoutScanner = (LinearLayout) findViewById(R.id.layoutScanner);
        ImageButton buttonScanner = (ImageButton) findViewById(R.id.buttonScanner);

        LinearLayout layoutHome = (LinearLayout) findViewById(R.id.layoutHome);
        ImageButton buttonHome = (ImageButton) findViewById(R.id.buttonHome);

        layoutScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contesto, Request_Camera.class);
                startActivity(intent);
            }
        });

        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contesto, Request_Camera.class);
                startActivity(intent);
            }
        });


        layoutHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contesto, Homepage.class);
                startActivity(intent);
            }
        });

        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contesto, Homepage.class);
                startActivity(intent);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        if (pref.getKey().equals("VirtualTour")) {
            DialogFragment dialogFragment = VTDialogFragment.newInstance();
            dialogFragment.setTargetFragment(caller, 0);
            dialogFragment.show(getSupportFragmentManager(), null);

        } else {
            final Bundle args = pref.getExtras();
            final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                    getClassLoader(),
                    pref.getFragment());
            fragment.setArguments(args);
            fragment.setTargetFragment(caller, 0);
            // Replace the existing Fragment with the new Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings, fragment)
                    .addToBackStack(null)
                    .commit();
            setTitle(pref.getTitle());
        }
        return true;
    }



    public static class HeaderFragment extends Fragment {

        public HeaderFragment() {
            // Required empty public constructor
        }

        public HeaderFragment newInstance() {
            HeaderFragment fragment = new HeaderFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.settings_activity, container, false);

            TextView nomeMuseo = (TextView) rootView.findViewById(R.id.nomeMuseo);
            nomeMuseo.setText(MUSEUM_NAME);

            if (SettingsActivity.MUSEUM_IMAGE != null) {
                ImageView immagineProfilo = (ImageView) rootView.findViewById(R.id.immagineProfilo);
                immagineProfilo.setImageBitmap(GestioneImmagine.getImmagineBitmap(SettingsActivity.MUSEUM_IMAGE));
            }


            LinearLayout bottomBarImpostazioni = (LinearLayout) rootView.findViewById(R.id.bottombar_impostazioni);
            bottomBarImpostazioni.setClickable(false);
            bottomBarImpostazioni.setVisibility(View.GONE);


            return rootView;
        }
    }

    public void showProfileDetails(View view){
        ConstraintLayout profileSettings = (ConstraintLayout) findViewById(R.id.profileSettings);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.fragment_profile, null);

        profileSettings.removeAllViews();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profileSettings, new ProfileFragment())
                .commit();
    }




    public static class LanguageFragment extends PreferenceFragmentCompat {

        private static final String LANGUAGE_KEY= "language";


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            PreferenceScreen pScreen = getPreferenceManager().createPreferenceScreen(getContext());
            setPreferenceScreen(pScreen);
            addPreferencesFromResource(R.xml.header_preferences);
            //handles language preference
            ListPreference language = getPreferenceScreen().findPreference(LANGUAGE_KEY);

            if(language != null){

                language.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
                language.setEntries(R.array.language_entries);
                language.setEntryValues(R.array.language_values);
                language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        // Get selected language
                        String selectedLanguage = newValue.toString();
                        setLocale(getActivity(),selectedLanguage);
                        getActivity().recreate();
                        return true;
                    }
                });
            }


        }

        @Override
        public void onResume() {
            super.onResume();

        }
        @Override
        public void onPause() {
            super.onPause();
        }
    }



    public static class ProfileFragment extends Fragment {
        boolean EDIT_MODE = false;
        public ProfileFragment() {
            // Required empty public constructor
        }

        public static ProfileFragment newInstance() {
            ProfileFragment fragment = new ProfileFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);

            return fragment;
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            ImageButton editBtn = rootView.findViewById(R.id.editProfileBtn);
            ImageButton backBtn = rootView.findViewById(R.id.backBtn);


            EditText museumName = rootView.findViewById(R.id.name);
            EditText telephone = rootView.findViewById(R.id.telephoneNumber);
            EditText province = rootView.findViewById(R.id.province);
            EditText city = rootView.findViewById(R.id.city);
            EditText postalCode = rootView.findViewById(R.id.postalCode);
            EditText address = rootView.findViewById(R.id.address);
            EditText vatNumber = rootView.findViewById(R.id.vatNumber);
            ImageView museumImageView = rootView.findViewById(R.id.profileImage);

            museumName.setText(MUSEUM_NAME);
            telephone.setText(TELEPHONE);
            province.setText(PROVINCE);
            city.setText(CITY);
            postalCode.setText(POSTAL_CODE);
            address.setText(ADDRESS);
            vatNumber.setText(VAT_NUMBER);

            Drawable defaultImageDrawable = getResources().getDrawable(R.drawable.ic_baseline_account_circle_24);
            Bitmap defaultImage = drawableToBitmap(defaultImageDrawable);
            if (MUSEUM_IMAGE != null){
                Log.d("check","museum image not null");
                MUSEUM_IMAGE_BITMAP = BitmapFactory.decodeByteArray(MUSEUM_IMAGE,0,MUSEUM_IMAGE.length);
                museumImageView.setImageBitmap(MUSEUM_IMAGE_BITMAP);
            }else{
                museumImageView.setImageBitmap(defaultImage);

            }

            MaterialButton eliminaAccount = (MaterialButton) rootView.findViewById(R.id.eliminaAccount);


            museumName.setKeyListener(null);
            telephone.setKeyListener(null);
            province.setKeyListener(null);
            city.setKeyListener(null);
            postalCode.setKeyListener(null);
            address.setKeyListener(null);
            vatNumber.setKeyListener(null);


            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EDIT_MODE=true;
                    rootView.findViewById(R.id.editProfileBtn).setVisibility(View.GONE);
                    rootView.findViewById(R.id.backBtn).setVisibility(View.GONE);
                    ImageButton saveChangesBtn = rootView.findViewById(R.id.saveChangesBtn);
                    ImageButton discardChangesBtn = rootView.findViewById(R.id.discardChangesBtn);
                    ImageView editProfileIcon = rootView.findViewById(R.id.edit_image);
                    saveChangesBtn.setVisibility(View.VISIBLE);
                    discardChangesBtn.setVisibility(View.VISIBLE);
                    editProfileIcon.setVisibility(View.VISIBLE);
                    museumImageView.setAlpha(0.50F);
                    eliminaAccount.setVisibility(View.GONE);


                    String oldMuseumName = museumName.getText().toString();
                    String oldTelephone = telephone.getText().toString();
                    String oldProvince = province.getText().toString();
                    String oldCity = city.getText().toString();
                    String oldPostalCode = postalCode.getText().toString();
                    String oldAddress = address.getText().toString();
                    String oldVatNumber = vatNumber.getText().toString();
                    Bitmap oldMuseumImage = ((BitmapDrawable) museumImageView.getDrawable()).getBitmap();


                    KeyListener defaultKeyListener = (KeyListener) new EditText(getContext()).getKeyListener();
                    museumName.setKeyListener(defaultKeyListener);
                    telephone.setKeyListener(defaultKeyListener);
                    province.setKeyListener(defaultKeyListener);
                    city.setKeyListener(defaultKeyListener);
                    postalCode.setKeyListener(defaultKeyListener);
                    address.setKeyListener(defaultKeyListener);
                    vatNumber.setKeyListener(defaultKeyListener);


                    museumImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder editProfileImageDialog = new AlertDialog.Builder(getContext());
                            editProfileImageDialog.setTitle(R.string.edit_profile_image_title)
                                    .setPositiveButton(R.string.select_image, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                                            intent.setType("image/*");
                                            startActivityForResult(intent,2);
                                            museumImageView.setImageBitmap(MUSEUM_IMAGE_BITMAP);

                                        }
                                    }).setNegativeButton(R.string.remove_image, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    MUSEUM_IMAGE_BITMAP = null;
                                    museumImageView.setImageBitmap(defaultImage);
                                }
                            }).create();
                            editProfileImageDialog.show();
                        }
                    });



                    discardChangesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder discardChangesDialog = new AlertDialog.Builder(getContext());
                            discardChangesDialog.setTitle(R.string.discard_changes_dialog_title)
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            museumName.setText(oldMuseumName);
                                            telephone.setText(oldTelephone);
                                            province.setText(oldProvince);
                                            city.setText(oldCity);
                                            postalCode.setText(oldPostalCode);
                                            address.setText(oldAddress);
                                            vatNumber.setText(oldVatNumber);
                                            museumImageView.setImageBitmap(oldMuseumImage);

                                            museumName.setKeyListener(null);
                                            telephone.setKeyListener(null);
                                            province.setKeyListener(null);
                                            city.setKeyListener(null);
                                            postalCode.setKeyListener(null);
                                            address.setKeyListener(null);
                                            vatNumber.setKeyListener(null);
                                            museumImageView.setOnClickListener(null);
                                            MUSEUM_IMAGE_BITMAP=oldMuseumImage;

                                            editBtn.setVisibility(View.VISIBLE);
                                            backBtn.setVisibility(View.VISIBLE);
                                            discardChangesBtn.setVisibility(View.GONE);
                                            saveChangesBtn.setVisibility(View.GONE);
                                            museumImageView.setAlpha(1F);
                                            editProfileIcon.setVisibility(View.GONE);
                                            EDIT_MODE=false;

                                            eliminaAccount.setVisibility(View.VISIBLE);

                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create();
                            discardChangesDialog.show();
                        }
                    });

                    saveChangesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(getContext());
                            saveChangesDialog.setTitle(R.string.save_changes_dialog_title)
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            museumName.setText(oldMuseumName);
                                            telephone.setText(oldTelephone);
                                            province.setText(oldProvince);
                                            city.setText(oldCity);
                                            postalCode.setText(oldPostalCode);
                                            address.setText(oldAddress);
                                            vatNumber.setText(oldVatNumber);
                                            museumImageView.setImageBitmap(oldMuseumImage);

                                            museumName.setKeyListener(null);
                                            telephone.setKeyListener(null);
                                            province.setKeyListener(null);
                                            city.setKeyListener(null);
                                            postalCode.setKeyListener(null);
                                            address.setKeyListener(null);
                                            vatNumber.setKeyListener(null);
                                            museumImageView.setOnClickListener(null);
                                            MUSEUM_IMAGE_BITMAP=oldMuseumImage;

                                            editBtn.setVisibility(View.VISIBLE);
                                            backBtn.setVisibility(View.VISIBLE);
                                            discardChangesBtn.setVisibility(View.GONE);
                                            saveChangesBtn.setVisibility(View.GONE);
                                            museumImageView.setAlpha(1F);
                                            editProfileIcon.setVisibility(View.GONE);
                                            EDIT_MODE=false;

                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            String newMuseumName = museumName.getText().toString();
                                            String newTelephone = telephone.getText().toString();
                                            String newProvince = province.getText().toString();
                                            String newCity = city.getText().toString();
                                            String newPostalCode = postalCode.getText().toString();
                                            String newAddress = address.getText().toString();
                                            String newVatNumber = vatNumber.getText().toString();

                                            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                                                @Override
                                                public void processFinish(String output) {}
                                            }).execute(Database.FLAG_INSERT, "UPDATE musei SET Nome = '"+newMuseumName+"',"+
                                                    "Telefono ='"+newTelephone+"',"+
                                                    "Provincia ='"+newProvince+"',"+
                                                    "Citta ='"+newCity+"',"+
                                                    "CAP ='"+newPostalCode+"',"+
                                                    "Indirizzo ='"+newAddress+"',"+
                                                    "P_IVA ='"+newVatNumber+"' WHERE idMuseo ="+ID_MUSEO+";");

                                            MUSEUM_NAME=newMuseumName;
                                            TELEPHONE=newTelephone;
                                            PROVINCE=newProvince;
                                            CITY=newCity;
                                            POSTAL_CODE=newPostalCode;
                                            ADDRESS=newAddress;
                                            VAT_NUMBER=newVatNumber;


                                            if(MUSEUM_IMAGE_BITMAP!=null) {
                                                GestioneImmagine.immagineBitmap = MUSEUM_IMAGE_BITMAP;
                                                Database asyncTask1 = (Database) new Database(new Database.AsyncResponse() {
                                                    @Override
                                                    public void processFinish(String output) {
                                                    }
                                                }).execute(Database.FLAG_IMAGE_UPLOAD, "UPDATE musei SET FotoMuseo = ? WHERE idMuseo =" + ID_MUSEO + ";");

                                            }else{
                                                museumImageView.setImageBitmap(defaultImage);
                                                GestioneImmagine.immagineBitmap = null;
                                                Database asyncTask1 = (Database) new Database(new Database.AsyncResponse() {
                                                    @Override
                                                    public void processFinish(String output) {}
                                                }).execute(Database.FLAG_INSERT,"UPDATE musei SET FotoMuseo = null WHERE idMuseo ="+ID_MUSEO+";");
                                                MUSEUM_IMAGE=null;
                                            }


                                            museumName.setKeyListener(null);
                                            telephone.setKeyListener(null);
                                            province.setKeyListener(null);
                                            city.setKeyListener(null);
                                            postalCode.setKeyListener(null);
                                            address.setKeyListener(null);
                                            vatNumber.setKeyListener(null);
                                            museumImageView.setOnClickListener(null);


                                            editBtn.setVisibility(View.VISIBLE);
                                            backBtn.setVisibility(View.VISIBLE);
                                            discardChangesBtn.setVisibility(View.GONE);
                                            saveChangesBtn.setVisibility(View.GONE);
                                            museumImageView.setAlpha(1F);
                                            editProfileIcon.setVisibility(View.GONE);
                                            EDIT_MODE=false;

                                            eliminaAccount.setVisibility(View.VISIBLE);

                                            dialogInterface.dismiss();

                                        }
                                    }).create();
                            saveChangesDialog.show();
                        }
                    });
                }
            });


            return rootView;
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == RESULT_OK && data!=null)
            {
                IMAGE_PATH = data.getData();
                try{
                    MUSEUM_IMAGE_BITMAP = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),IMAGE_PATH);

                }catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void onResume() {
            super.onResume();
            if(MUSEUM_IMAGE_BITMAP!=null){
                ((ImageView) getView().findViewById(R.id.profileImage)).setImageBitmap(MUSEUM_IMAGE_BITMAP);
            }else {
                Drawable defaultImageDrawable = getResources().getDrawable(R.drawable.ic_baseline_account_circle_24);
                Bitmap defaultImage = drawableToBitmap(defaultImageDrawable);
                ((ImageView) getView().findViewById(R.id.profileImage)).setImageBitmap(defaultImage);
            }


        }


        @Override
        public void onPause() {
            super.onPause();
        }



        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            OnBackPressedCallback callback = new OnBackPressedCallback(
                    true // default to enabled
            ) {
                @Override
                public void handleOnBackPressed() {
                    if(EDIT_MODE==false){
                        getParentFragmentManager().popBackStackImmediate();
                    }else{
                        Toast.makeText(getContext(), getString(R.string.cannot_exit_edit_mode), Toast.LENGTH_LONG).show();
                    }
                }
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(
                    this, // LifecycleOwner
                    callback);
        }


    }

    public void backBtnClick(View view) {
        RelativeLayout profileDetails = (RelativeLayout) findViewById(R.id.profileDetails);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        profileDetails.removeAllViews();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.profileDetails, new HeaderFragment())
                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new LanguageFragment())
                .commit();
    }


    public void deleteAccount(View view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SettingsActivity.this);
        builder.setTitle(R.string.eliminazione_account_title);
        builder.setMessage(R.string.eliminazione_account_message);
        builder.setPositiveButton(getResources().getString(R.string.elimina), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                eliminaAccount();

                RelativeLayout profileDetails = (RelativeLayout) findViewById(R.id.profileDetails);

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                profileDetails.removeAllViews();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.profileDetails, new HeaderFragment())
                        .commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings, new LanguageFragment())
                        .commit();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.annulla), null);
        builder.show();
    }

    public void eliminaAccount()
    {
        Database eliminaOpere = (Database) new Database(new Database.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Database eliminaZone = (Database) new Database(new Database.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                        Database eliminaAccounts = (Database) new Database(new Database.AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                Database eliminaMuseo = (Database) new Database(new Database.AsyncResponse() {
                                    @Override
                                    public void processFinish(String output) {
                                        Intent intent = new Intent(contesto, Logout_temporaneo_cancellare.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }).execute(Database.FLAG_DELETE, "DELETE FROM musei WHERE idMuseo = " + ID_MUSEO);
                            }
                        }).execute(Database.FLAG_DELETE, "DELETE FROM accounts WHERE idMuseo = " + ID_MUSEO);
                    }
                }).execute(Database.FLAG_DELETE, "DELETE FROM zone WHERE idMuseo = " + ID_MUSEO);
            }
        }).execute(Database.FLAG_DELETE, "DELETE FROM opere WHERE idMuseo = " + ID_MUSEO);
    }

    public static class ChangePasswordFragment extends Fragment {
        public ChangePasswordFragment(){
            // Required empty public constructor
        }
        public static  ChangePasswordFragment newInstance() {
            ChangePasswordFragment fragment = new ChangePasswordFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);
            EditText oldPassword = rootView.findViewById(R.id.oldPassword);
            EditText newPassword = rootView.findViewById(R.id.newPassword);
            EditText repeatNewPassword = rootView.findViewById(R.id.repeatNewPassword);
            Button saveBtn = rootView.findViewById(R.id.savePasswordButton);
            TextView error = rootView.findViewById(R.id.change_password_error);
            getPassword();


            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    //1 controllare che la vecchia psw sia uguale a quella attuale
                    //2 controllare che la nuova password non sia vuota, 3 stessa cosa per il "repeate" field
                    //4 controllare che coincinado i campi
                    //5 aggiornare la password



                    if (newPassword.getText().toString().length()<4 ){
                        newPassword.setError(getString(R.string.password_format_error));
                    }else if(PASSWORD.equals(oldPassword.getText().toString()))
                    {
                        if(newPassword.getText().toString().trim().isEmpty()) //controllo se è vuota
                        {
                            newPassword.setError(getString(R.string.campoMancante));
                        }
                        else if (repeatNewPassword.getText().toString().trim().isEmpty()) //controllo se è vuota
                        {
                            repeatNewPassword.setError(getString(R.string.campoMancante));
                        }
                        else if(newPassword.getText().toString().equals(repeatNewPassword.getText().toString()))
                        {
                            Database cambioPassword = (Database) new Database(new Database.AsyncResponse() {

                                @Override
                                public void processFinish(String output) {
                                    if(output=="true")
                                    {
                                        startActivity(new Intent(contesto, SettingsActivity.class));
                                    }
                                }
                            }).execute(Database.FLAG_UPDATE, "UPDATE accounts SET Password='"+newPassword.getText().toString()+"' where idMuseo = "+ID_MUSEO);
                            pswUpdated = false;
                        }
                        else
                        {
                            Toast.makeText(contesto, R.string.passwordNonCoincidenti, Toast.LENGTH_LONG).show();
                            newPassword.setError(getString(R.string.passwordNonCoincidenti));
                            repeatNewPassword.setError(getString(R.string.passwordNonCoincidenti));
                        }
                    }
                    else
                    {
                        oldPassword.setError(getString(R.string.oldPassword_error));
                    }

                }
            });


            return rootView;

        }

        private void getPassword() {

            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {



                @Override
                public void processFinish(String output) {

                    if (!output.isEmpty()) {
                        String[] credenziali = output.split(",");
                        PASSWORD =  credenziali[0];
                    } else {
                        Toast.makeText(contesto, R.string.Password_non_presente_nel_Database, Toast.LENGTH_SHORT).show();
                    }

                }

            }).execute(Database.FLAG_SELECT, "SELECT Password FROM accounts where idMuseo = " + ID_MUSEO + ";", "1");

        }
    }

    public static class VTDialogFragment extends DialogFragment{
        EditText mInput;
        boolean isValid;

        public VTDialogFragment(){
            // Required empty public constructor
        }
        public static  VTDialogFragment newInstance() {
            VTDialogFragment fragment = new VTDialogFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder vtDialogBuilder = new AlertDialog.Builder(getContext());
            vtDialogBuilder.setTitle(getString(R.string.settings_virtual_tour));
            vtDialogBuilder.setMessage(getString(R.string.virtual_tour_dialog_message));

            LinearLayout layout = new LinearLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(56,0,56,56);
            mInput = new EditText(getContext());
            mInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

            layout.addView(mInput,params);
            vtDialogBuilder.setView(layout);


            vtDialogBuilder.setNeutralButton(R.string.virtual_tour_neutral_button_text, (dialogInterface, i) -> {
                //overridden in "on resume"
            });
            vtDialogBuilder.setPositiveButton(R.string.save,(dialogInterface, i) -> {
                //overridden in "on resume"
            });
            vtDialogBuilder.setNegativeButton(R.string.cancel,(dialogInterface, i) -> {
                //overridden in "on resume"
            });

            return vtDialogBuilder.create();
        }

        @Override
        public void onResume() {
            super.onResume();
            final AlertDialog d = (AlertDialog) getDialog();
            if (d != null) {
                //correct the edit text margins
                if (VT_LINK != null){
                    mInput.requestFocus();
                    mInput.setText(VT_LINK);
                    mInput.setSelection(mInput.getText().length());
                }
                //set the behavior when positive button is clicked
                Button positiveButton = d.getButton(BUTTON_POSITIVE);
                positiveButton.setOnClickListener(v -> {
                    String newLink = String.valueOf(mInput.getText());
                    isValid = URLUtil.isValidUrl(newLink);

                    if (isValid) {
                        d.dismiss();
                        if (!(newLink.equals(VT_LINK)))
                            Toast.makeText(getContext(), getString(R.string.virtual_tour_toast_message_added), Toast.LENGTH_SHORT).show();

                    } else if (newLink.isEmpty()) {
                        d.dismiss();
                        if (!(newLink.equals(VT_LINK)))
                            Toast.makeText(getContext(), getString(R.string.virtual_tour_toast_message_removed), Toast.LENGTH_SHORT).show();
                    } else
                        mInput.setError(getString(R.string.virtual_tour_dialog_error));
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                });


                //set the behavior when neutral button is clicked
                Button neutralButton = d.getButton(BUTTON_NEUTRAL);
                neutralButton.setOnClickListener(view -> {
                    String newLink = String.valueOf(mInput.getText());
                    isValid = URLUtil.isValidUrl(newLink);
                    if (isValid) {

                        Uri webpage = Uri.parse(newLink);

                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

                        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    } else if (newLink.isEmpty())
                        mInput.setError(getString(R.string.virtual_tour_cannot_test_empty_link));
                    else
                        mInput.setError(getString(R.string.virtual_tour_dialog_error));
                });
            }
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            //saves the changes when the dialog is dismissed
            String newLink = String.valueOf(mInput.getText());
            if (isValid || newLink.isEmpty()){
                Database asyncTask = (Database) new Database(new Database.AsyncResponse() {
                    @Override
                    public void processFinish(String output) {
                    }
                }).execute(Database.FLAG_INSERT, "UPDATE musei SET linkVT = '"+ newLink +"' WHERE idMuseo =" + ID_MUSEO + ";");
                VT_LINK=newLink;
            }
        }
    }


    public static class InfoFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.info_preferences, rootKey);
            Preference appVersion = getPreferenceScreen().findPreference("appVersion");
            String version = BuildConfig.VERSION_NAME;
            appVersion.setTitle(getString(R.string.app_version)+": " + version);
        }


        public static class TermsFragment extends Fragment {
            public TermsFragment() {
                // Required empty public constructor
            }
            public static TermsFragment newInstance() {
                TermsFragment fragment = new TermsFragment();
                Bundle args = new Bundle();
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

                View rootView = inflater.inflate(R.layout.fragment_info_subscreens, container, false);
                WebView view = (WebView) rootView.findViewById(R.id.webview);
                view.loadUrl("file:///android_asset/terms.html");
                return rootView;
            }
        }


        public static class PrivacyPolicyFragment extends Fragment {
            public PrivacyPolicyFragment() {
                // Required empty public constructor
            }
            public static PrivacyPolicyFragment newInstance() {
                PrivacyPolicyFragment fragment = new PrivacyPolicyFragment();
                Bundle args = new Bundle();
                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

                View rootView = inflater.inflate(R.layout.fragment_info_subscreens, container, false);
                WebView view = (WebView) rootView.findViewById(R.id.webview);
                view.loadUrl("file:///android_asset/privacy.html");
                return rootView;
            }
        }

    }


    public static class LogoutDialogFragment extends DialogFragment{
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder logoutDialog = new AlertDialog.Builder(getContext());
            logoutDialog.setTitle(R.string.Logout).
                    setMessage(R.string.LogoutConfirmation).
                    setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //redirect to
                            Intent intent = new Intent(contesto, Logout_temporaneo_cancellare.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).setNegativeButton(R.string.cancel,null).create();
            return logoutDialog.create();
        }
    }


    private void getData(){
        try {
            String Data =  new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    try {
                        Database.rs.beforeFirst();
                        while(Database.rs.next())
                        {
                            MUSEUM_NAME = Database.rs.getString("Nome");
                            TELEPHONE = Database.rs.getString("Telefono");
                            PROVINCE = Database.rs.getString("Provincia");
                            CITY = Database.rs.getString("Citta");
                            POSTAL_CODE = Database.rs.getString("CAP");
                            ADDRESS = Database.rs.getString("Indirizzo");
                            VAT_NUMBER = Database.rs.getString("P_IVA");
                            MUSEUM_IMAGE = Database.rs.getBytes("FotoMuseo");
                            VT_LINK = Database.rs.getString("linkVT");
                        }
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.errore_recupero_dati_museo), Toast.LENGTH_SHORT).show();
                    }
                }
            }).execute(Database.FLAG_SELECT, "SELECT * FROM musei WHERE idMuseo ="+ID_MUSEO, "10").get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }



    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

    }

    public static SharedPreferences getSharedPreferences (Context ctxt) {
        return ctxt.getSharedPreferences("FILE", 0);
    }


    public void logout(View view){
        new LogoutDialogFragment().show(getSupportFragmentManager(),null);
    }

    protected static Bitmap drawableToBitmap(Drawable drawable){
        try {
            Bitmap bitmap;
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }


    }

}






