package it.uniba.pathway;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;

public class GestioneImmagine {

    public static Bitmap immagineBitmap=null;

    /**
     * Restituisce un immagine bitmap da un array di byte[], solitamente utilizzata per tradurre le immagini dal database
     * @param immagineByte Array di byte[] il quale deve essere popolato da un immagine Bitmap precedentemente tradotta in un array di byte[]
     * @return un immagine Bitmap
     */
    public static Bitmap getImmagineBitmap(byte[] immagineByte) {

        if(immagineByte != null) immagineBitmap = BitmapFactory.decodeByteArray(immagineByte,0,immagineByte.length);
        else{
            immagineBitmap = null;
        }
        return immagineBitmap;
    }

    /**
     * Converte la variabile globale immagineBitmap in un array di byte[]
     *
     * @return un array di byte[] contenente un immagine Bitmap
     */
    @NonNull
    public static byte[] byteArrayConversion() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        immagineBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] ByteArrayImmagine = bos.toByteArray();

        return ByteArrayImmagine;
    }

    /**
     *
     * Questo metodo carica il codice QR all'interno dell'opera specificata nel id
     *
     * @param id questo parametro viene utilizzato per indicare in quale opera verrà caricata l'immagine del QR
     */
    public static void aggiornaImmagineQr(int id) {

        Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

            @Override
            public void processFinish(String output) {

            }
        }).execute(Database.FLAG_IMAGE_UPLOAD, "UPDATE opere SET QR = ? WHERE idOpere = " + id + ";");

    }

    /**
     *
     * Essendo nel documento PDF generato un limite di spazio per il caricamento delle immagini,
     * questo metodo si occupa di controllare che questo limite di spazio venga rispettato, altrimenti
     * calcolerà quali misure dovranno avere i lati di un immagine per rispettare i limiti di dimensione e
     * mantenere il rapporto dell'immagine (per rapporto si intende 16:9, 16:10, 4:3, 1:1, 21:9 ecc)
     *
     * @param bmp Immagine di cui si vogliono controllare le dimensioni
     * @return immagine Bitmap ridimensionata
     */
    public static Bitmap controlloImmagine(Bitmap bmp)
    {
        Bitmap scaledbmp;

        boolean scaleFlag = true ;
        double scale = 1, bmpWidth = bmp.getWidth(), bmpHeight = bmp.getHeight();
        double maxDimens;
        //Controlla qual è il lato più grande dell'immagine o eventualmente se i 2 lati sono uguali
        if(bmpHeight > bmpWidth || bmpHeight == bmpWidth)
        {
            maxDimens = bmpHeight;
        }
        else
        {
            maxDimens = bmpWidth;
        }

        while(scaleFlag)
        {
            if(scaleFlag)
            {
                if(maxDimens * scale <= 300)
                {
                    scaleFlag = false;
                }
                scale = scale - 0.01;
            }
        }

        scaledbmp = Bitmap.createScaledBitmap(bmp,(int) (bmp.getWidth()*scale),(int) (bmp.getHeight()*scale),true);

        return scaledbmp;
    }

    /**
     * Passata un'immagine di tipo Bitmap, questo metodo disegnerà un cerchio all'interno del cui posizionare e ritagliare l'immagine passatagli.
     *
     * @param bitmap Immagine che verrà "incastonata" nel cerchio
     * @return L'immagine arrotondata tipo logo
     */
    public static Bitmap getCircularBitmapFrom(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        float radius;
        //Calcolo il raggio dal lato più piccolo, così da riempire interamente il cerchio/cornice

        if(bitmap.getWidth() > bitmap.getHeight())
        {
            radius = ((float) bitmap.getHeight()) / 2f;
        }
        else
        {
            radius = ((float) bitmap.getWidth()) / 2f;
        }

        //Dichiaro una variabile Bitmap con le misure dell'immagine e specifico la densità di colore

        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Dichiaro la variaible BitmapShader che permette di ripete o far riflettere la bitmap in base alla modalità.
        //In questo caso scegliendo la modalità CLAMP indico al programma che qualora dovesse avanzare spazio extra all'interno dell'area di disegno
        //(nel mio caso del cerchio di raggio radius), il programma dovrà riempirlo con i colori dello strato più esterno
        //Ad esempio caso in cui l'immagine da inserire nella tela sia più piccola:
        //Immagine singola: https://img-blog.csdn.net/20161215160747537
        //Immagine all'interno della tela con la modalità CLAMP: https://img-blog.csdn.net/20161215164944584
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();

        //Alias aiuta a smussare i bordi rendendoli più smussati
        paint.setAntiAlias(true);
        paint.setShader(shader);

        Canvas canvas = new Canvas(canvasBitmap);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2.3f, radius*0.8f, paint);

        return canvasBitmap;
    }
}