package it.uniba.pathway;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Base64;

public class GestioneDellaSessione {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREFERENCES_NAME = "sessione";
    String SESSION_KEY = "sessione_utente";
    String NOME_MUSEO = "nome_museo";
    String IMMAGINE_MUSEO = "immagine_museo";
    String FLAG_DATI_MUSEO = "datiMuseo";

    public GestioneDellaSessione(Context contesto)
    {
        sharedPreferences = contesto.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void salvataggioSessione(String id)
    {
        //salvare la sessione dell'utente quando l'utente ha effettuato l'accesso
        editor.putString(SESSION_KEY+"",id).commit();
    }

    public String getSessione()
    {
        //restituirà id dell'utente se salvata, altrimenti restituirà -1
        return sharedPreferences.getString(SESSION_KEY, "-1");
    }

    public void setNomeMuseo(String nomeMuseo)
    {
        editor.putString(NOME_MUSEO, nomeMuseo).commit();
    }

    public String getNomeMuseo()
    {
        return sharedPreferences.getString(NOME_MUSEO, String.valueOf(R.string.nome_museo_mancante));
    }

    public void setImmagineMuseo(byte[] immagineMuseo)
    {
        editor.putString(IMMAGINE_MUSEO, Base64.getEncoder().encodeToString(immagineMuseo)).commit();
    }

    public byte[] getImmagineMuseo()
    {
        if(sharedPreferences.getString(IMMAGINE_MUSEO,null) != null)
        {
            return Base64.getDecoder().decode(sharedPreferences.getString(IMMAGINE_MUSEO,null));
        }
        else
        {
            return null;
        }
    }

    public void setFlagDatiMuseo(boolean flag)
    {
        editor.putBoolean(FLAG_DATI_MUSEO, flag).commit();
    }

    public boolean getFlagDatiMuseo()
    {
        return sharedPreferences.getBoolean(FLAG_DATI_MUSEO,true);
    }

    public void rimozioneSessione()
    {
        editor.putString(SESSION_KEY+"","-1").commit();
        editor.putString(IMMAGINE_MUSEO, null).commit();
        editor.putString(NOME_MUSEO, "").commit();
        editor.putBoolean(FLAG_DATI_MUSEO, true).commit();
    }
}
