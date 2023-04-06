package it.uniba.pathway;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

public class ItemClass {

    private CardView cardview;
    private TextView title;
    private ImageButton eye;
    private ImageButton cestino;
    private CardView cardviewOverflow;

    public ItemClass(){
        this.cardview = cardview;
        this.title = title;
        this.eye = eye;
        this.cestino = cestino;
        this.cardviewOverflow = cardviewOverflow;
    }

    public CardView getCard() {
        return cardview;
    }

    public TextView getText() {
        return title;
    }

    public ImageButton geteye() {
        return eye;
    }

    public ImageButton getcestino(){
        return cestino;
    }

    public CardView getcardOver(){
        return cardviewOverflow;
    }


}
