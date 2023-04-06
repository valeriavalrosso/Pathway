package it.uniba.pathway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * La classe ListDragDropAdapter estende la classe ArrayAdapter&lt;Opera&gt;, quindi permette di
 * popolare e visualizzare la lista delle opere, gestendone anche le modifiche all'ordinamento,
 * che avvengono mediante azioni di drag&drop.
 */
public class ListDragDropAdapter extends ArrayAdapter<Opera> {

    final int INVALID_ID = -1;

    /**
     * Dichiarazione dell'interfaccia Listener e del suo metodo onGrab, implementato nell'activity
     * ListSortingActivity, nel momento in cui viene istanziato un oggetto di classe ListDragDropAdapter.
     */
    public interface Listener {
        void onGrab(int position, ConstraintLayout row);
    }


    Context context;
    ArrayList<Opera> opere;
    final Listener listener;
    final Map<Opera, Integer> mIdMap = new HashMap<>();


    /**
     * Costruttore della classe ListDragDropAdapter, che permette di istanziarne degli oggetti,
     * fornendone dei parametri ben precisi, utili alla corretta gestione della lista.
     * <p>
     *     Al suo interno, viene popolata una mappa che mette in relazione ogni oggetto di classe
     *     Opera, presente nell'ArrayList opere, con il suo indice.
     * </p>
     * <br>
     * @param context contesto attuale dell'applicazione
     * @param opere ArrayList di opere con cui si deve popolare la lista
     * @param listener interfaccia che gestisce la "presa" di un'opera per spostarla e le azioni successive
     */
    public ListDragDropAdapter(Context context, ArrayList<Opera> opere, Listener listener) {
        super(context,0,opere);

        this.context = context;
        this.opere = opere;
        this.listener = listener;

        for (int i = 0; i < opere.size(); ++i) {
            mIdMap.put(opere.get(i), i);
        }
    }


    /**
     * Costruisce e restituisce la View delle righe della lista per ogni opera, seguendo il layout
     * in opere_item.xml.<br>
     * In particolare, a ogni chiamata, viene costruita la View per l'opera che si trova all'indice
     * position (uno dei parametri di input al metodo) nell'ArrayList opere.
     * <p>
     *     In questo modo, ogni riga della lista viene costruita in base a diversi parametri.<br>
     *     Infatti, per ogni riga della lista viene impostata l'immagine, il nome e la desrizione
     *     dell'opera che si trovano all'indice position nell'ArrayList opere.<br>
     * </p>
     * <p>
     *     All'interno del metodo, è presente anche l'implementazione del metodo onTouch()
     *     dell'interfaccia OnTouchListener. Al suo interno, viene chiamato il metodo onGrab
     *     dell'interfaccia Listener, implementato nell'activity ListSortingActivity.<br>
     * </p>
     * <br>
     * @param position indice dell'opera nell'ArrayList opere
     * @param view la View da utilizzare per la visualizzazione della lista
     * @param parent View "genitore" di cui la View che sarà restiutuita sarà "figlio"
     * @return riga della lista costruita secondo i vari parametri
     */
    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Context context = getContext();

        if(null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.opere_item, null);
        }

        ConstraintLayout operaSingolaLayout = (ConstraintLayout) view.findViewById(R.id.opera_singola_layout);

        TextView idOpera = (TextView) view.findViewById(R.id.idOpera);   // non visibile nel layout
        ImageView immagineOpera = (ImageView) view.findViewById(R.id.immagineOpera);
        TextView nomeOpera = (TextView) view.findViewById(R.id.nomeOpera);
        TextView descrizioneOpera = (TextView) view.findViewById(R.id.descrizioneOpera);

        LinearLayout dragOpera = (LinearLayout) view.findViewById(R.id.dragOpera);
        dragOpera.setVisibility(View.VISIBLE);
        dragOpera.setTag(opere.get(position).getIdOpera());


        idOpera.setText(String.valueOf(opere.get(position).getIdOpera()));
        immagineOpera.setImageBitmap(opere.get(position).getImmagine());
        nomeOpera.setText(opere.get(position).getNomeOpera());
        descrizioneOpera.setText(opere.get(position).getDescrizioneOpera());


        view.findViewById(R.id.dragOpera)
                .setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        listener.onGrab(position, operaSingolaLayout);
                        return false;
                    }
                });

        return view;
    }


    /**
     * Restituisce il valore presente nella mappa, corrispondente alla chiave passata.
     * <p>
     *     In particolare, se position è valido, ovvero se è compreso tra 0 e il numero di opere
     *     che popolano la lista, recupera l'opera che si trova all'indice position nell'ArrayList
     *     opere e restituisce il valore associato nella mappa all'opera recuperata come chiave.
     * </p>
     * <br>
     * @param position indice dell'opera nella lista di tutte le opere
     * @return valore presente nella mappa in corrispondenza dell'opera come chiave
     */
    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }

        Opera item = getItem(position);

        return mIdMap.get(item);
    }


    /**
     * Indica se gli ID degli elementi rimangono stabili quando si verificano cambiamenti alla lista.
     * <br><br>
     * @return <i>true</i> se un ID si riferisce sempre allo stesso oggetto, <i>false</i> altrimenti
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }
}
