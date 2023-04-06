package it.uniba.pathway;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Logout_temporaneo_cancellare extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private Button logout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout_temporaneo_cancellare);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        logout = (Button) findViewById(R.id.LogoutBtn);

        logoutApp();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutApp();
            }
        });

    }

    public void logoutApp()
    {
        GestioneDellaSessione gestioneDellaSessione = new GestioneDellaSessione(Logout_temporaneo_cancellare.this);
        gestioneDellaSessione.rimozioneSessione();

        if(mGoogleSignInClient!=null)
        {
            mGoogleSignInClient.signOut();
        }

        reindirizzaPaginaLogin();
    }

    //effettua al disconnessione da google
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        reindirizzaPaginaLogin();
                    }
                });
        reindirizzaPaginaLogin();
    }

    private void reindirizzaPaginaLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}