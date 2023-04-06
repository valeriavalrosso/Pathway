package it.uniba.pathway;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class Database extends AsyncTask<String, Void, String> {

    private static final String url = "jdbc:mysql://185.25.205.141:3306/sms?useSSL=false&requireSSL=false";
    private static final String user = "SMS2021";
    private static final String pass = "Sms2021!";

    public static final String FLAG_INSERT = "i";
    public static final String FLAG_SELECT = "s";
    public static final String FLAG_UPDATE = "u";
    public static final String FLAG_DELETE = "d";
    public static final String FLAG_SELECT_RAW = "sr";
    public static final String FLAG_IMAGE_UPLOAD = "iu";
    public static final String FLAG_CREATE_VIEW = "v";
    public static ResultSet rs = null;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public Database(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    /**
     * Il metodo doInBackground permette di connettersi al database ed effettuare delle operazioni
     * senza o in parte, intaccare l'esperienza dell'utente.
     *
     * <p>
     *
     * Una volta terminata la sua esecuzione i risultati vengono elaborati e passati tramite il metodo onPostExecute
     *
     * In caso di problemi di connessione, la variaible "result" riporterà l'errore
     */
    protected String doInBackground(String... params) {
        Statement st=null;
        String result = "";
        Connection con=null;

        //Viene stabilita una connessione al database
        try {

            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);

            st = con.createStatement();


        }catch(Exception e){
            result = "error connection";
        }

        /**
         * Questo if permette di effettuare delle RICHIESTE di tipo SELECT al database
         * formattando l'output. Tra un campo e l'altro viene posizionato un carattere separatore ovvero : ","
         *
         * L'output formattato verrà inserito nella variabile "result"
         */
        if(params[0].equals(FLAG_SELECT)){
            try {
                rs = st.executeQuery(params[1]);
                ResultSetMetaData rsmd = rs.getMetaData();

                while (rs.next()) {

                    for (int i=1; i<=Integer.parseInt(params[2]); i++) {
                        result += rs.getString(i)+",";
                    }
                    result+="\n";
                }

            } catch (Exception e) {
                result="query Error -> "+params[1]+" "+e;
            }
        }

        /**
         * Questo if permette di effettuare l'inserimento, l'aggiornamento e l'eliminazione nel database
         *
         * Prima di far ciò, a causa di alcuni caratteri speciali che potrebbero intaccare una di queste precedenti operazioni,
         * viene controllata la query.
         *
         * Popola la variabile "result" true se la lettura di dati dal database è andata a buon fine. False viceversa.
         */
        if(params[0].equals(FLAG_INSERT) || params[0].equals(FLAG_UPDATE) || params[0].equals(FLAG_DELETE)){

            int questionMarkCounter = 0;
            String query = params[1];

            for(int i=0 ; i < query.length() ; i++)
            {
                if(query.charAt(i) == '?') questionMarkCounter++;
            }

            //se i punti di domanda "?" sono diversi da 0, il primo indicherà al preparedStatement di caricare la descrizione dell'opera
            //Contenente appunto dei caratteri speciali come " o ', mentre il secondo ? caricherà l'immagine
            //Questo particolare controllo viene utilizzato per caricare una nuova opera senza effettuare diverse chiamate per 3 singoli attributi quali
            // dati dell'opera, descrizione (per i caratteri speciali) e immagine
            if(questionMarkCounter != 0)
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GestioneImmagine.immagineBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] ByteArrayImmagine = bos.toByteArray();

                try{
                    PreparedStatement ps = con.prepareStatement(params[1]);
                    ps.setString(1,params[2]);                                                    //params 3 sarà la descrizione che si aggiunge come variabile alla QUery
                    ps.setBytes(2,ByteArrayImmagine);                                             //Il 2° '?' sarà dell'immagine da caricare
                    ps.executeUpdate();
                    ps.close();
                    result="true";
                }
                catch (Exception e){
                    result="false";
                }
            }
            else
            {
                try{
                    st.executeUpdate(params[1]);
                    result="true";
                }catch (Exception e){
                    result="query Error -> "+params[1]+" "+e;
                }
            }
        }

        /**
         * Questo if permette di effettuare delle RICHIESTE di tipo SELECT al database senza formattare l'output
         *
         * Popolando la variabile "result" true se la lettura di dati dal database è andata a buon fine. False viceversa.
         */
        if(params[0].equals(FLAG_SELECT_RAW)){

            try{
                this.rs = st.executeQuery(params[1]);
                if (!this.rs.isBeforeFirst() ) {
                    System.out.println("Non sono presenti dati");
                    result = "false";
                } else {
                    result = "true";
                }

            }catch (Exception e){
                result="query Error -> "+params[1]+" "+e;
            }
        }

        /**
         * Una volta popolata la viariabile "immagineBitmap" nella classe GestioneImmagine, sarà possibile caricare
         * questa Bitmap nel database ad un'opera corrispondente che verrà indicata nella richiesta (query)
         */

        if(params[0].equals(FLAG_IMAGE_UPLOAD)){

            try{
                PreparedStatement ps = con.prepareStatement(params[1]);
                ps.setBytes(1,GestioneImmagine.byteArrayConversion());
                ps.executeUpdate();
                ps.close();
                result="true";
            }
            catch (Exception e){
            }
        }

        /**
         * Questo if si occupa di creare ed eliminare una vista.
         *
         * Metodo utilizzato per la creazione del percorso di un museo
         */
        if(params[0].equals(FLAG_CREATE_VIEW)){

            try{
                st.execute(params[1]);
                result="true";
            }catch (Exception e){
                result="Errore nella query di creazione della view -> "+params[1]+" "+e;
            }
        }

        return result;
    }



    @Override
    /**
     * Terminata l'esecuzione del metodo doInBackground, questo metodo prende come dato di input la variabile "result",
     * avvalorata nel metodo precedente.
     *
     * In fine il metodo si occupera di passare il contenuto di result come parametro di ingresso del metodo "processFinish()"
     */
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }



}


