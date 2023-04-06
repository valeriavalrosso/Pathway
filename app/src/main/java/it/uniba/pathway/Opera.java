package it.uniba.pathway;

import android.graphics.Bitmap;


/**
 * La classe rappresenta le opere del museo, con tutte le relative proprietà.
 */
public class Opera {

    private int idOpera, idMuseo, posizione;
    private String titolo, descrizione, zona;
    private boolean visibilita, OPERA_ELIMINATA;
    private Bitmap immagine;


    /**
     * Costruttore della classe Opera, utilizzato per inizializzare ogni oggetto.
     * <br><br>
     * @param idOpera ID dell'opera
     * @param titolo titolo dell'opera
     * @param descrizione descrizione dell'opera
     * @param visibilita opera visibile (valore true) o invisibile (valore false)
     * @param zona nome della zona del museo in cui si trova l'opera
     * @param immagine immagine dell'opera inserita dal curatore
     * @param idMuseo ID del museo in cui si trova l'opera
     * @param posizione posizione dell'opera nella zona rispetto alle altre
     * @param OPERA_ELIMINATA flag che indica se il curatore ha selezionato l'opera tra quelle da eliminare in Edit Mode
     */
    public Opera(int idOpera, String titolo, String descrizione, boolean visibilita, String zona, Bitmap immagine, int idMuseo, int posizione, boolean OPERA_ELIMINATA) {
        this.idOpera = idOpera;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.visibilita = visibilita;
        this.zona = zona;
        this.immagine = immagine;
        this.idMuseo = idMuseo;
        this.posizione = posizione;
        this.OPERA_ELIMINATA = OPERA_ELIMINATA;
    }




    /**
     * @return ID dell'opera
     */
    public int getIdOpera() {
        return this.idOpera;
    }

    /**
     * @return titolo dell'opera
     */
    public String getNomeOpera() {
        return this.titolo;
    }

    /**
     * @return descrizione dell'opera
     */
    public String getDescrizioneOpera() {
        return this.descrizione;
    }

    /**
     * @return visibilita dell'opera
     */
    public boolean getVisibilitaOpera() {
        return this.visibilita;
    }

    /**
     * @return nome della zona del museo in cui si trova l'opera
     */
    public String getZona() {
        return this.zona;
    }

    /**
     * @return immagine dell'opera di tipo Bitmap
     */
    public Bitmap getImmagine() {
        return this.immagine;
    }

    /**
     * @return ID del museo in cui si trova l'opera
     */
    public int getIdMuseo() {
        return idMuseo;
    }

    /**
     * @return posizione dell'opera all'interno della zona rispetto alle altre opere
     */
    public int getPosizione() {
        return posizione;
    }

    /**
     * @return flag indica se il curatore ha deciso di eliminare l'opera o meno
     */
    public boolean getOperaEliminata() {
        return OPERA_ELIMINATA;
    }




    /**
     * @param titolo titolo dell'opera
     */
    public void setNomeOpera(String titolo) {
        this.titolo = titolo;
    }

    /**
     * @param descrizione descrizione dell'opera
     */
    public void setDescrizioneOpera(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * @param visibilita visibilità dell'opera (true se è visibile, altrimenti false)
     */
    public void setVisibilitaOpera(boolean visibilita) {
        this.visibilita = visibilita;
    }

    /**
     * @param zona nome della zona del museo in cui si trova l'opera
     */
    public void setZona(String zona) {
        this.zona = zona;
    }

    /**
     * @param immagine immagine dell'opera di tipo Bitmap
     */
    public void setImmagine(Bitmap immagine) {
        this.immagine = immagine;
    }

    /**
     * @param idMuseo ID del museo in cui si trova l'opera
     */
    public void setIdMuseo(int idMuseo) {
        this.idMuseo = idMuseo;
    }

    /**
     * @param posizione posizione dell'opera nella zona rispetto alle altre
     */
    public void setPosizione(int posizione) {
        this.posizione = posizione;
    }

    /**
     * @param OPERA_ELIMINATA true se il curatore ha deciso di eliminare l'opera, altrimenti false
     */
    public void setOperaEliminata(boolean OPERA_ELIMINATA) {
        this.OPERA_ELIMINATA = OPERA_ELIMINATA;
    }
}
