package it.uniba.pathway;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;


public class
Generazione_Percorso extends AppCompatActivity {


    private static int indicePartenza = 0;
    private static int indiceArrivo = 0;
    // variables for our buttons.

    int larghezzaPagina = 700;
    int lunghezzaPagina = 1200;

    private static float maxLarghezzaImg = 300;
    private static float maxAltezzaImg = 300;

    int indexFrase;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp, immagineHeader;
    private static ArrayList<Opera> opera = new ArrayList<>();
    private String ID_MUSEO;
    private static Bitmap imgMuseo = null;
    private static String nomeMuseo = "";
    private static int totOpere=-1;
    private String nomeVista;
    private static int cicliGlob=0;
    private int STORAGE_PERMISSION_CODE = 1;

    private static PdfDocument documentoPDF;

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //leggo id del museo dalla sessione
        opera.clear();

        GestioneDellaSessione sessione = new GestioneDellaSessione(Generazione_Percorso.this);
        ID_MUSEO = sessione.getSessione();

        nomeMuseo = sessione.getNomeMuseo();

        if(sessione.getImmagineMuseo() != null)
        {
            imgMuseo = GestioneImmagine.getCircularBitmapFrom(GestioneImmagine.getImmagineBitmap(sessione.getImmagineMuseo()));
        }
        else
        {
            //se il museo non ha un immagine propria, gli si mette pathway di default
            imgMuseo = BitmapFactory.decodeResource(getResources(), R.drawable.splash_image);
        }



        // initializing our variables.
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.splash_image);                 //Immagine che verrà caricata in alto a sx del documento come header
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        requestStoragePermission();

    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(Generazione_Percorso.this,
                WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(Generazione_Percorso.this)
                    .setTitle(getString(R.string.generazione_percorso))
                    .setMessage(getString(R.string.generazione_percorso_2))
                    .setPositiveButton(getString(R.string.consenti), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Generazione_Percorso.this,
                                    new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton(getString(R.string.nega), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int permission1 = ActivityCompat.checkSelfPermission(Generazione_Percorso.this, WRITE_EXTERNAL_STORAGE);
                int permission2 = ActivityCompat.checkSelfPermission(Generazione_Percorso.this, READ_EXTERNAL_STORAGE);

                contaOpereTot();
                onBackPressed();

            } else {
                Toast.makeText(this, getString(R.string.permesso_negato_scrittura_file), Toast.LENGTH_SHORT).show();

            }
        }

    }

    /**
     * Si occupa di contare le opere presenti nel db in base al ID del museo e della visibilità dell'opera
     * avvalorando la variabile totOpere.
     *
     * Se totOpere fosse maggiore di 0 allora chiamerà il metodo spostaDatiNellaView
     */
    private void contaOpereTot()
    {
        String queryCount = "SELECT COUNT(idOpere) as totOpere FROM opere WHERE idMuseo = "+ID_MUSEO+" AND Visibilita = 1";
        Database contaOpere = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if(!output.isEmpty()) {

                    String[] credenziali = output.split(",");
                    totOpere = Integer.parseInt(credenziali[0]);

                    if (totOpere == -1 || totOpere == 0) {
                        Toast.makeText(Generazione_Percorso.this, getString(R.string.opere_mancanti), Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        spostaDatiNellaView();
                    }
                }

            }
        }).execute(Database.FLAG_SELECT, queryCount,"1");
    }

    /**
     * Crea una vista contenente tutte le opere di un singolo museo in base al id del museo, visibilità ed ordinandole per Zona ed posizione all'interno della zona
     */
    private void spostaDatiNellaView() {
        Random nomeRandom = new Random();
        nomeVista = String.valueOf(nomeRandom.nextInt(999999999));
        String query = "CREATE VIEW rnd"+ nomeVista +" as SELECT ROW_NUMBER() over (order by Sequence, Posizione) idOpere, Nome, Descrizione, Zona, Immagine, Sequence, Posizione FROM (SELECT opere.idOpere, opere.Nome, opere.Descrizione, opere.Zona, opere.Immagine, zone.Sequence, Posizione FROM opere INNER JOIN zone ON opere.Zona = zone.Nome WHERE opere.Visibilita = 1 AND opere.idMuseo = "+ID_MUSEO+" ORDER BY Sequence, Posizione) as t ORDER BY Sequence, Posizione";
        Database contaOpere = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if(!output.isEmpty()) {

                    String[] resoultCode = output.split(",");
                    String ok_Code = resoultCode[0];

                    inizializzazioneMetodoPDF();
                }

            }
        }).execute(Database.FLAG_CREATE_VIEW, query);
    }

    /**
     * Conta il numero di cicli necessari per prendere tutte le opere dalla vista appena creata
     *
     * Terminato il conteggio, richiamta il metodo getOpere()
     */
    private void inizializzazioneMetodoPDF()
    {
        int cicli=0;
        int resto=0;

        cicli = Math.round(totOpere/8);

        if(totOpere%8 != 0)
        {
            cicli++;
            resto = totOpere%8;
        }

        cicliGlob = cicli;
        while(totOpere != 0)
        {

            if(totOpere >= 8)
            {
                indicePartenza = indiceArrivo;
                indiceArrivo += 8;
                totOpere-= 8;
            }
            else
            {
                indicePartenza = indiceArrivo;
                indiceArrivo += resto;
                totOpere -= resto;
            }
            getOpere();
        }

        indicePartenza = 0;
        indiceArrivo = 0;
    }

    /**
     * Richiede ciclicamente 8 opere alla volta dalla vista popolando l'arraylist "opera" della classe Opera.
     *
     * Al termine dell'ultimo ciclo di chiamate, chiama il metodo "creazionePDFPercoroso()"
     */
    private void getOpere()
    {
        String queryView = "SELECT idOpere, Nome, Descrizione, Zona, Immagine FROM rnd"+nomeVista+" WHERE idOpere>"+indicePartenza+" AND idOpere<="+indiceArrivo;

        Database letturaOpere = (Database) new Database(new Database.AsyncResponse()
        {
            @Override
            public void processFinish(String output) {
                try
                {
                    while (Database.rs.next())
                    {

                        opera.add(new Opera(
                                Database.rs.getInt("idOpere"),
                                Database.rs.getString("Nome"),
                                Database.rs.getString("Descrizione"),
                                true,
                                Database.rs.getString("Zona"),
                                GestioneImmagine.getImmagineBitmap(Database.rs.getBytes("Immagine")),
                                Integer.parseInt(ID_MUSEO),
                                -1,
                                false)); //controllare se funziona
                    }

                    //generaPDF();
                }catch(Exception e){
                    System.out.println("Errore acquisizione dati dal DB durante la generazione del PDF: "+e);
                }

                cicliGlob--;
                if(cicliGlob == 0) // quando finiscono tutte le chiamate, allora il pdf verrà generato
                {
                    //elimina la vista dal database per evitare uno spreco di risorse
                    eliminaView();
                    creazionePDFPercorso();
                }

            }

        }).execute(Database.FLAG_SELECT_RAW, queryView);



        //popolamento immagineMuseo e nomeMuseo


    }

    /**
     * Questo metodo si occupa di generare il pdf definendo misure o ricavandole tramite opportuni calcoli matematici. \n\r
     *
     * <p>
     *
     * Tutti i valori numerici corrispondono all'unità di misura px.
     *
     * <p>
     *
     * Principalmente questo metodo gira attorno a 2 FLAG, "flagPrimaOpera" e "flagSecondaOpera"
     * i quali indicano se l'opera che sta per essere scritta sia la prima del documento (parte alta di esso) oppure la seconda (parte bassa)
     *
     * <p>
     *
     * Successivamente ne derivano puri calcoli che permetto ai campi testo (Nome opera, Nome zona, Descrizione) di essere stampati con assoluta precisione
     */
    private void creazionePDFPercorso()
    {



        //Inizializzazione del documento pdf
        documentoPDF = new PdfDocument();
        //Paint permette di stampare o scrivere: testo, geometrie o bitmap; in base a stile e colore attribuibili
        Paint  titolo = new Paint();

        PdfDocument.PageInfo infoPagina = new PdfDocument.PageInfo.Builder(larghezzaPagina,lunghezzaPagina,1).create();



        titolo.setTextAlign(Paint.Align.CENTER);
        //Allineamento titolo
        titolo.setTextSize(40);
        //Dimensione titolo
        titolo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        //Imposta il tipo di testo ( grassetto, italico, normale )


        //HEADER

        //Dichiaro una nuova variabile di tipo paint ma per il testo, dato che utilizza valori diversi rispetto al titolo
        Paint testo = new Paint();

        testo.setTextAlign(Paint.Align.LEFT);
        testo.setTextSize(14);
        testo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        Paint grassetto = new Paint();
        grassetto.setTextAlign(Paint.Align.LEFT);
        grassetto.setTextSize(15);
        grassetto.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));


        float x=0;
        float y=0;
        double moltiplicatoreTesto = 1.3;
        float dimensioneXimmagine = 0;
        float dimensioneYimmagine = 0;
        //cancellami
        int distanzaDalImmagine=11;
        int distanzaDalBordo = 5;

        int caratteri=13, i, indexSpazio, indiceAltezzaDiScrittura=40;
        boolean flagPrimoRigo = true;
        boolean flagPrimaOpera = true;
        boolean flagSecondaOpera = false;
        boolean flagSottoImmage = false;

        PdfDocument.Page pagina = null;
        int counter = 0;
        //Canvas è una superficie di disegno utile per stampare immagini o testo grazie all'aiuto del matodo paint() contenuto al suo interno
        Canvas canvas = null;


        for(i = 0 ; i <opera.size() ; i++)
        {
            //Header

            if(flagPrimaOpera)
            {
                //Inizializza la variabile "pagina" fornendogli alcui dettagli dichiarati nella variaible infoPagina
                pagina = documentoPDF.startPage(infoPagina);
                //Avvaloriamo il nostro "spazio di disegno" ovvero indichiamo che la nostra "tela" sarà la pagina del pdf
                canvas = pagina.getCanvas();


                //Immagine museo ridotta di dimensione e scritta
                immagineHeader = Bitmap.createScaledBitmap(imgMuseo,(int) (imgMuseo.getWidth()*0.15),(int) (imgMuseo.getHeight()*0.15),false);
                canvas.drawBitmap(immagineHeader,-28,0,titolo);

                //Scrittura nome museo nel header della pagina
                titolo.setTextSize(22);
                titolo.setTextAlign(Paint.Align.CENTER);
                int flagTitolo=0;
                float yTitolo = lunghezzaPagina*0.04f;
                String StringaTemp ="",StringaPrecedente="";
                caratteri = 0;

                for(indexFrase = 0 ; indexFrase < nomeMuseo.length() ; indexFrase++)
                {
                    if(nomeMuseo.charAt(indexFrase) != '\n' && caratteri <= 40 )
                    {
                        StringaTemp += nomeMuseo.charAt(indexFrase);
                        caratteri++;
                    }
                    //FIXARE QUI!
                    else
                    {   //Resetto i caratteri contenuti nella riga

                        flagTitolo++;

                        if(flagTitolo <= 2)
                        {
                            canvas.drawText(newLineTitle(indexFrase,StringaTemp),larghezzaPagina/2,yTitolo,titolo);
                            StringaPrecedente = StringaTemp;
                            StringaTemp="";
                            caratteri=0;
                        }
                        else if(indexFrase +1 == nomeMuseo.length() && flagTitolo == 3)
                        {
                            //controllo che il titolo sarà completo nella seconda riga. Se tutto ok, scrivo, altrimenti scrivo la stringa così com'è aggiungengo ... alla fine
                            canvas.drawText(newLineTitle(indexFrase,StringaTemp),larghezzaPagina/2,yTitolo,titolo);
                            break;
                        }
                        else
                        {
                            StringaTemp += "...";
                            canvas.drawText(StringaTemp,larghezzaPagina/2,yTitolo,titolo);
                            break;
                        }
                        yTitolo += 20;
                    }

                    if(indexFrase +1 == nomeMuseo.length() && flagTitolo < 3)
                    {
                        canvas.drawText(StringaTemp,larghezzaPagina/2,yTitolo,titolo);
                        //break;
                    }
                }


                //Si prendere il logo del app Pathway e ne riduce le dimensioni posizionandola a DX della pagina
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.splash_image);
                immagineHeader = Bitmap.createScaledBitmap(bmp,(int) (bmp.getWidth()*0.15),(int) (bmp.getHeight()*0.15),false);
                canvas.drawBitmap(immagineHeader,590,0,titolo);

                //Linea che crea un bordo
                canvas.drawLine(0,immagineHeader.getHeight()+25,700,immagineHeader.getHeight()+25, titolo);

                x = distanzaDalBordo;
                y = immagineHeader.getHeight()+60;

                flagPrimaOpera = false;
            }


            //fine Header

            //inizio Body

            //qualora sia la seconda opera che sta venendo scritta, aggiusta le coordinate di partenza di scrittura
            if(flagSecondaOpera)
            {
                x = distanzaDalBordo;
                y = (int) ((lunghezzaPagina+immagineHeader.getHeight()+25)/2)+35;
            }

            //Ottiene l'immagine dell'opera da stampare
            bmp = opera.get(i).getImmagine();

            //Controlla che le dimensioni dell'immagine siano conformi
            if(bmp.getWidth() > 300 || bmp.getHeight() > 300)
            {

                scaledbmp = GestioneImmagine.controlloImmagine(bmp);
            }
            else
            {
                scaledbmp = bmp;
            }



            //Viene generato un box di dimensioni statiche che conterrà le immagini
            canvas.drawLine(x,y,maxLarghezzaImg+x,y,titolo);
            canvas.drawLine(x,y,x, maxAltezzaImg+y,titolo);
            canvas.drawLine(x,maxAltezzaImg+y,maxLarghezzaImg+x,maxAltezzaImg+y,titolo);
            canvas.drawLine(maxLarghezzaImg+x,y,maxLarghezzaImg+x,maxAltezzaImg+y,titolo);

            String descrizioneOpera = opera.get(i).getDescrizioneOpera();
            String temp = "";

            //Viene calcolato lo spazio vuoto tra il bordo del box e l'immagine, così da stamparla
            //perfettamente al centro di esso
            double spazioX = (maxLarghezzaImg-scaledbmp.getWidth())/2;
            double spazioY = (maxAltezzaImg-scaledbmp.getHeight())/2;

            canvas.drawBitmap(scaledbmp,(int) (x+spazioX), (int) (y+spazioY), titolo);


            dimensioneXimmagine = maxLarghezzaImg+x;
            dimensioneYimmagine = maxAltezzaImg+y;

            x = maxLarghezzaImg+distanzaDalBordo+distanzaDalImmagine;
            y += 11.5;


            //Inizio etichettatura opera
            canvas.drawText(getString(R.string.nome_Opera) ,dimensioneXimmagine+distanzaDalImmagine,y,grassetto);

            canvas.drawText(opera.get(i).getNomeOpera(), (float) (x*moltiplicatoreTesto),y,testo);

            y = y+20;

            canvas.drawText(getString(R.string.zona_opera) ,x,y,grassetto);

            canvas.drawText(opera.get(i).getZona(), (float) (x*moltiplicatoreTesto),y,testo);

            y = y+20;
            //fine etichettattura opera ed inizio descrizione

            canvas.drawText(getString(R.string.descrizione) ,x,y,grassetto);
            caratteri=13;

            boolean daScrivere = true;

            //Questo ciclo gira finchè tutta la descrizione non verrà stampata sul pdf
            for(indexFrase = 0 ; indexFrase < opera.get(i).getDescrizioneOpera().length() ; indexFrase++)
            {

                //La prima volta parto con la variabile caratteri avvalorata a 13 e non a 0, questo fa si che venga lasciata abbastanza spazio
                //dal fine della parola "Descrizione: ". Qualsiasi valore più piccolo in corrispondenza della prima riga, verrà stampato
                //sopra la scritta "Descrizione: " divenendo illeggibile il contenuto della prima frase.

                //Una volta raggiunta una frase di 55 caratteri o una volta raggiunto uno "\n" viene chiamato un metodo che si occupera di fornire la corretta frase da stampare
                //senza che nessuna parola rimanga tagliata
                //Es: frase = "ciao come st"    Diventerebbe ->     "ciao come "
                if(descrizioneOpera.charAt(indexFrase) != '\n' && caratteri <= 55 )
                {
                    temp += descrizioneOpera.charAt(indexFrase);
                    caratteri++;
                    daScrivere = true;
                }

                //Una volta raggiunta una frase di 105 caratteri o una volta raggiunto uno "\n" purchè ci si trovi a scrivere oltre il box immagine,
                //viene chiamato un metodo che si occupera di fornire la corretta frase da stampare
                //senza che nessuna parola rimanga tagliata
                //Es: frase = "ciao come st"    Diventerebbe ->     "ciao come "

                else if(descrizioneOpera.charAt(indexFrase) != '\n' && caratteri <= 105 && flagSottoImmage)
                {
                    temp += descrizioneOpera.charAt(indexFrase);
                    caratteri++;
                    daScrivere = true;
                }
                else
                {
                    //Stampo la frase completa ottenuta. I vari controlli servono a capire se la frase verrà
                    //stampata nella prima riga, in quelle successive e se sotto il box immagine
                    if(descrizioneOpera.charAt(indexFrase) == '\n')
                    {
                        if(flagPrimoRigo)
                        {
                            x = (int) (x*moltiplicatoreTesto);
                            flagPrimoRigo=false;
                        }

                        canvas.drawText(temp, x, y, testo);
                        temp = "";
                        caratteri=0;
                        y = y + 20;
                    }
                    else
                    {
                        if(flagPrimoRigo)
                        {
                            x = (int) (x*moltiplicatoreTesto);
                            flagPrimoRigo=false;
                        }
                        canvas.drawText(newLine(indexFrase,i,temp), x, y, testo);
                        temp = "";
                        caratteri=0;
                        y = y + 20;
                    }
                    indiceAltezzaDiScrittura += 20;                                                 //per ogni rigo avanzo di 20
                    if(maxAltezzaImg < indiceAltezzaDiScrittura)
                    {
                        x = distanzaDalBordo;
                        flagSottoImmage = true;
                        counter++;
                    }
                    //Il counter si occupa di contare le righe della descrizione scritte dopo l'immagine
                    //Se il programma scriverà più di 9 righe, allora verrà stampato alla 10° riga "Descrizione troppo lunga"
                    //Così che il curatore possa accorciarla
                    if(counter == 9)
                    {
                        canvas.drawText(getString(R.string.Descrizione_troppo_lunga), x, y, testo);
                        break;
                    }
                    if(!flagSottoImmage)//molto probabilemente inutile
                    {
                        x = maxLarghezzaImg+distanzaDalBordo+distanzaDalImmagine;
                    }
                    daScrivere = false;
                }
            }

            //Al termine del ciclo precedente l'ultima riga non è detto che venga scritta per intero, ad esempio se finisse prima dei 105 o 55 caratteri
            //non verrebbe stampato nulla, così facendo controllo se stava ancora un'ultima frase da stampare e mi occupo di questo.
            //Oppure qualora il museo inserisse una descrizione di 1 sola frase breve rischierebbe sempre di non stamparla.
            if(daScrivere)
            {
                if(flagPrimoRigo)
                {
                    x = (int) (x*moltiplicatoreTesto);
                    flagPrimoRigo=false;
                    canvas.drawText(temp, x, y, testo);
                }
                else
                {
                    canvas.drawText(temp, x, y, testo);
                }
            }

            //Disegno una linea che separa le 2 opere
            canvas.drawLine(0,(lunghezzaPagina+immagineHeader.getHeight()+25)/2,700,(lunghezzaPagina+immagineHeader.getHeight()+25)/2, titolo);

            //Imposto dei FLAG in base all'ordine di scrittura dell'ultima opera
            if(flagSecondaOpera)
            {
                documentoPDF.finishPage(pagina);
                flagPrimaOpera = true;
                flagSecondaOpera = false;
            }
            else if(!flagSecondaOpera  && (i+1 == opera.size()))
            {
                documentoPDF.finishPage(pagina);
                break;
            }
            else
            {
                flagSecondaOpera = true;
            }
            indiceAltezzaDiScrittura = 40;
            flagSottoImmage = false;
            counter = 0;
            flagPrimoRigo=true;
        }

        //Una volta terminata la stampa di tutte le opere presenti nell'array list "opera" di classe Opera
        //provveto a generare il file pdf nella cartella download di facile accesso anche per l'utente
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getString(R.string.Percorso_pathway_pdf));

        if(file.exists())
        {
            file.delete();
        }

        try{
            documentoPDF.writeTo(new FileOutputStream(file));
            Toast.makeText(Generazione_Percorso.this, getString(R.string.percorso_creato), Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            Toast.makeText(Generazione_Percorso.this, getString(R.string.percorso_non_creato), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //chiude il documento pdf
        documentoPDF.close();


    }

    /**
     * Presa una frase come parametro di input, controlla che quest'ultima non termini con una parola troncata
     * restituidendo la frase corretta da stampare.
     *
     * Inoltre avvalora la variabile "indexDescrizione" nel punto in cui il ciclo di lettura della descrizione
     * deve riprendere a leggere
     *
     * @param indexFrase Indica il punto preciso all'interno della descrizione nel momento in cui questo metodo è stato chiamato
     * @param indexOpera Indica l'indice dell'opera di cui sta leggendo la descrizione
     * @param temp Indica la frase creata dalla lettura della descrizione
     * @return Restituisce la frase corretta senza parole finali incomplete
     */
    private String newLine(int indexFrase, int indexOpera, String temp) {

        //contatore indica di quante posizioni bisognerà tornare indietro nella stringa (o array di caratteri)
        int index , contatore = 0;
        String frase="";

        for (index = indexFrase ; index >= 0 ; index--)
        {
            if(opera.get(indexOpera).getDescrizioneOpera().charAt(index) == ' ')
            {
                //Corregge il punto da dove bisognerà continuare a leggere la frase
                this.indexFrase = index;

                //creo la nuova stringa copiando dalla prima lettera all'ultimo spazio presente nella frase
                //eliminando del numero esatto le posizioni contenenti le parole incomplete
                frase = temp.substring(0, temp.length()-contatore);
                break;
            }
            else
            {
                contatore++;
            }
            //il caso per '\n' non serve perchè viene gestito a monte
        }

        return frase;
    }

    /**
     * Presa una frase come parametro di input, controlla che quest'ultima non termini con una parola troncata
     * restituidendo la frase corretta da stampare.
     *
     * Inoltre avvalora la variabile "indexDescrizione" nel punto in cui il ciclo di lettura della descrizione
     * deve riprendere a leggere
     *
     * @param indexFrase Indica il punto preciso all'interno del titolo nel momento in cui questo metodo è stato chiamato
     * @param temp Indica la frase creata dalla lettura della descrizione
     * @return Restituisce la frase corretta senza parole finali incomplete
     */
    private String newLineTitle(int indexFrase, String temp) {

        int index , contatore = 0;
        String frase="";
        for (index = indexFrase ; index >= 0 ; index--)
        {
            if(nomeMuseo.charAt(index) == ' ')
            {
                //Corregge il punto da dove bisognerà continuare a leggere la frase
                this.indexFrase = index;

                //creo la nuova stringa copiando dalla prima lettera all'ultimo spazio presente nella frase
                //eliminando del numero esatto le posizioni contenenti le parole incomplete
                frase = temp.substring(0, temp.length()-contatore);
                break;
            }
            else
            {
                contatore++;
            }
            //il caso per '\n' non serve perchè viene gestito a monte
        }
        return frase;
    }

    /**
     * Una volta terminata l'acquisizione di tutte le opere dalla vista, questo metodo si occuperà di eliminarla
     */
    private void eliminaView()
    {
        String query = "DROP VIEW rnd"+nomeVista;
        Database contaOpere = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                if(!output.isEmpty()) {

                    String[] resoultCode = output.split(",");
                    String ok_Code = resoultCode[0];

                }

            }
        }).execute(Database.FLAG_DELETE, query);
    }

}