package it.uniba.pathway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * La classe ListAdapter estende la classe BaseAdapter, quindi permette di popolare e gestire la
 * visualizzazione della lista delle opere sia in modalità di sola visualizzazione sia in modalità
 * di modifica delle opere.<br>
 * Inoltre, implementa l'interfaccia Filterable, in modo da offrire all'utente la funzionalità
 * di ricerca delle opere tramite il titolo delle stesse, adattandone la lista di conseguenza.
 */
public class ListAdapter extends BaseAdapter implements Filterable {

    Context context;
    ArrayList<Opera> opere;
    boolean EDIT_MODE = false;
    ArrayList<Opera> opereCercate;
    CustomFilter customFilter;


    /**
     * Costruttore della classe ListAdapter, che permette di istanziarne degli oggetti,
     * fornendo dei parametri ben precisi, utili alla corretta visualizzazione della lista.
     * <p>
     *     Al suo interno, viene inizializzata anche l'ArrayList opereCercate con tutte le opere
     *     della zona in cui ci si trova, in modo tale che in fase di ricerca delle opere,
     *     l'ArrayList opereCercate possa aggiornarsi, contenendo le sole opere che rispettano
     *     il parametro di ricerca.
     * </p>
     * <br>
     * @param context contesto attuale dell'applicazione
     * @param opere ArrayList delle opere con cui si deve popolare la lista
     * @param EDIT_MODE flag che indica se ci si trova in Edit Mode (<i>true</i>) o sola visualizzazione (<i>false</i>)
     */
    public ListAdapter(Context context, ArrayList<Opera> opere, boolean EDIT_MODE) {
        this.context = context;
        this.opere = opere;
        this.opereCercate = opere;
        this.EDIT_MODE = EDIT_MODE;
    }


    /**
     * Restituisce il numero delle opere della zona.
     * <br><br>
     * @return numero delle opere presenti nell'ArrayList opere
     */
    @Override
    public int getCount() {
        return opere.size();
    }


    /**
     * Restituisce l'opera che si trova nell'ArrayList opere all'indice passato in input.
     * <br><br>
     * @param position indice dell'opera nell'ArrayList
     * @return oggetto di classe Opera presente all'indice position nell'ArrayList opere
     */
    @Override
    public Object getItem(int position) {
        return opere.get(position);
    }


    /**
     * Restituisce l'identificativo della riga dell'opera che si trova nell'ArrayList opere all'indice position.
     * <br><br>
     * @param position indice nell'ArrayList opere dell'opera di cui si cerca l'id della riga
     * @return identificativo della riga corrispondente all'opera di riferimento
     */
    @Override
    public long getItemId(int position) {
        return opere.indexOf(getItem(position));
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
     *     Oppure, per quanto riguarda la visibilità o l'eliminazione, se l'opera non è visibile o
     *     è in procinto di essere eliminata, in modalità di modifica l'intera riga assume opacità
     *     pari al 50%, invece in modalità di sola visualizzazione non viene mostrata.
     * </p>
     * <br>
     * <p>
     *     NB : viene effettuato un controllo sulla posizione diversa da [numero totale delle opere
     *     - 1], in quanto in OpereActivity.getOpereFromDB() è stata aggiunta un'opera vuota visibile solo
     *     in Edit Mode, perchè altrimenti, nella riga dell'ultima opera, gli ImageButton per la
     *     modifica della visibilità dell'opera e per l'eliminazione dell'opera sarebbero coperti
     *     dal Floating Action Button.
     * </p>
     * <br>
     * @param position indice dell'opera nell'ArrayList opere
     * @param convertView la View da utilizzare per la visualizzazione della lista
     * @param parent View "genitore" di cui la View che sarà restiutuita sarà "figlio"
     * @return riga della lista costruita secondo i vari parametri
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.opere_item, null);
        }


        // in edit mode, l'intera riga dell'opera non visibile ha opacità ridotta al 50%
        LinearLayout operaSingola = (LinearLayout) convertView.findViewById(R.id.opera_singola);
        if(EDIT_MODE) {
            if(opere.get(position).getVisibilitaOpera())
                operaSingola.setAlpha(1.0F);
            else operaSingola.setAlpha(0.5F);
        }
        else {
            if(opere.get(position).getVisibilitaOpera())
                operaSingola.setVisibility(View.VISIBLE);
            else operaSingola.setVisibility(View.GONE);
        }



        TextView idOpera = (TextView) convertView.findViewById(R.id.idOpera);   // non visibile nel layout
        ImageView immagineOpera = (ImageView) convertView.findViewById(R.id.immagineOpera);
        TextView nomeOpera = (TextView) convertView.findViewById(R.id.nomeOpera);
        TextView descrizioneOpera = (TextView) convertView.findViewById(R.id.descrizioneOpera);

        ImageButton eliminaOpera = (ImageButton) convertView.findViewById(R.id.eliminaOpera);
        eliminaOpera.setTag(opere.get(position).getIdOpera());

        ImageButton visibilitaOpera = (ImageButton) convertView.findViewById(R.id.visibilitaOpera);
        visibilitaOpera.setTag(opere.get(position).getIdOpera());

        Button annullaEliminazione = (Button) convertView.findViewById(R.id.annullaEliminazione);
        annullaEliminazione.setTag(opere.get(position).getIdOpera());

        // setto i valori dei campi per ogni opera della lista
        idOpera.setText(String.valueOf(opere.get(position).getIdOpera()));
        immagineOpera.setImageBitmap(opere.get(position).getImmagine());
        nomeOpera.setText(opere.get(position).getNomeOpera());
        descrizioneOpera.setText(opere.get(position).getDescrizioneOpera());



        if (EDIT_MODE && position != getCount()-1) {

            // se si è deciso di eliminare l'opera, viene visualizzato il tasto per annullare l'eliminazione
            if (opere.get(position).getOperaEliminata()) {
                operaSingola.setAlpha(0.5F);
                eliminaOpera.setVisibility(View.GONE);
                visibilitaOpera.setVisibility(View.GONE);
                annullaEliminazione.setVisibility(View.VISIBLE);
            }
            else {
                if(opere.get(position).getVisibilitaOpera()) {
                    visibilitaOpera.setImageResource(R.drawable.ic_baseline_visibility_24);
                }
                else {
                    visibilitaOpera.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                }
                annullaEliminazione.setVisibility(View.GONE);
                eliminaOpera.setVisibility(View.VISIBLE);
                visibilitaOpera.setVisibility(View.VISIBLE);
            }
        }
        else {  // se non si è in edit mode o è l'opera vuota (ultima) -> eliminaOpera, visibilitaOpera e annullaEliminazione assumono visibilità GONE
            annullaEliminazione.setVisibility(View.GONE);
            eliminaOpera.setVisibility(View.GONE);
            visibilitaOpera.setVisibility(View.GONE);
        }

        return convertView;
    }


    /**
     * Metodo che restituisce un'istanza della classe CustomFilter, esistente o appena creata.
     * <br><br>
     * @return customFilter, oggetto di classe CustomFilter
     */
    @Override
    public Filter getFilter() {
        if (customFilter == null) {
            customFilter = new CustomFilter();
        }
        return customFilter;
    }


    /**
     * Inner class che gestisce la ricerca, permettendo il filtraggio della lista delle opere secondo
     * la stringa che l'utente inserisce nella barra di ricerca.
     */
    class CustomFilter extends Filter {

        /**
         * Filtra l'ArrayList delle opere, in base alla stringa che l'utente inserisce nella barra
         * di ricerca; infatti, in caso la stringa sia vuota, viene restituito l'intero ArrayList
         * di opere presenti nella zona corrente, altrimenti vengono selezionate e visualizzate le
         * sole opere il cui nome contiene la stringa inserita.
         * <br><br>
         * @param charSequence stringa inserita dall'utente nella barra di ricerca
         * @return oggetto di classe FilterResults, caratterizzato dalla coppia
         *         (numero di corrispondenze, elementi corrispondenti)
         */
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();

            if(charSequence != null && charSequence.length() > 0) {
                charSequence = charSequence.toString().toUpperCase();

                ArrayList<Opera> ricerca = new ArrayList<Opera>();

                for (int i = 0; i < opereCercate.size(); i++) {
                    if (opereCercate.get(i).getNomeOpera().toUpperCase().contains(charSequence)) {
                        Opera singolaOpera = new Opera(
                                opereCercate.get(i).getIdOpera(),
                                opereCercate.get(i).getNomeOpera(),
                                opereCercate.get(i).getDescrizioneOpera(),
                                opereCercate.get(i).getVisibilitaOpera(),
                                opereCercate.get(i).getZona(),
                                opereCercate.get(i).getImmagine(),
                                opereCercate.get(i).getIdMuseo(),
                                opereCercate.get(i).getPosizione(),
                                opereCercate.get(i).getOperaEliminata()
                        );

                        ricerca.add(singolaOpera);
                    }
                }

                Opera operaVuota = new Opera(-1, "", "", false, "", null, Integer.parseInt(OpereActivity.ID_MUSEO), 0, false);
                ricerca.add(operaVuota);

                results.count = ricerca.size();
                results.values = ricerca;
            }
            else {
                results.count = opereCercate.size();
                results.values = opereCercate;
            }

            return results;
        }


        /**
         * Permette l'aggiornamento dell'interfaccia, che mostrerà le sole opere restituite dal
         * metodo performFiltering(), dopo aver effettuato il filtraggio.
         * @param charSequence stringa inserita dall'utente nella barra di ricerca
         * @param results oggetto di classe FilterResults, che contiene il risultato del filtraggio
         */
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            opere = (ArrayList<Opera>) results.values;
            notifyDataSetChanged();
        }
    }
}
