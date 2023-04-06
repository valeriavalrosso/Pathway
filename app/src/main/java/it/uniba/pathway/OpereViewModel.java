package it.uniba.pathway;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;


/**
 * La classe OpereViewModel estende la classe ViewModel, che permette di salvare i dati anche
 * quando si verificano dei refresh dell'activity, come ad esempio la rotazione dello schermo.
 * <p>
 *     In questo caso, l'ArrayList delle opere presenti nella zona non viene istanziata nuovamente
 *     al refresh dell'activity, ma mantiene i suoi dati anche se viene ruotato lo schermo, quindi
 *     non è necessario effettuare nuovamente la richiesta al database per popolarla.
 * </p>
 */
public class OpereViewModel extends ViewModel {

    public ArrayList<Opera> opere;


    /**
     * Costruttore di OpereViewModel, per poter istanzaire oggetti di questa classe nelle activity.
     * Al suo interno, istanzia una nuova ArrayList di oggetti di classe Opera.
     */
    public OpereViewModel() {
        opere = new ArrayList<Opera>();  // ViewModel creato
    }


    /**
     * Override del metodo onCleared() della classe ViewModel, che libera le risorse occupate dai dati salvati,
     * quando l'activity in cui viene istanziato l'oggetto della classe OpereViewModel viene distrutta.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
    }


    /**
     * Metodo che inizializza l'ArrayList di Opere della classe OpereViewModel con le opere
     * passate come parametro di input.
     * <br><br>
     * @param opere ArrayList di opere che si deve salvare ai refresh dell'activity
     */
    public void addAllOpere(ArrayList<Opera> opere) {
        this.opere.addAll(opere);
    }
}
