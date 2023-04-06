package it.uniba.pathway;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import it.uniba.pathway.Helper.ItemTouchHelperAdapter;
import it.uniba.pathway.Helper.OnStartDragListener;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable, ItemTouchHelperAdapter {

    Boolean editMode;
    static List<String> titles;
    List<Integer> visibility;
    static List<Integer> ids;
    LayoutInflater inflater;
    Context ctx;
    Dialog dialogg;
    private String vecchioTitolo;
    List<String> nomiZoneCercate;
    CustomFilter customFilter;


    private OnRecyclerViewClickListener listener;

    //ButterKnife
    OnStartDragListener listenerh;


    //Scambio di due zone tra loro
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        if(editMode){
            if(fromPosition<toPosition){
                for (int i=fromPosition; i<toPosition; i++){
                    Integer a = ids.get(i);
                    ids.set(i, ids.get(toPosition));
                    ids.set(toPosition, a);
                    notifyItemMoved(fromPosition, toPosition);
                }
            }
            else{
                for (int i=fromPosition; i>toPosition; i--){
                    Integer a = ids.get(i);
                    ids.set(i, ids.get(toPosition));
                    ids.set(toPosition, a);
                    notifyItemMoved(fromPosition, toPosition);
                }
            }

            System.out.println(ids.toString());
            return true;
        }
        else{
            return false;
        }

    }

    @Override
    public void onItemDismiss(int position) {
        titles.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnRecyclerViewClickListener{
        void OnItemClick(int position);
    }

    public void OnRecyclerViewClickListener(OnRecyclerViewClickListener listener){
        this.listener = listener;
    }


    public Adapter(Context ctx, List<String> titles,List<Integer> visibility,List<Integer> ids,Boolean editMode, OnStartDragListener listenerh){
        this.titles = titles;
        this.visibility = visibility;
        this.inflater = LayoutInflater.from(ctx);
        this.ids = ids;
        this.editMode=editMode;
        this.ctx=ctx;

        this.nomiZoneCercate = titles;

        this.listenerh = listenerh;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.custom_grid_layout, parent, false));
    }

    //Aggiorna il contenuto degli itemView settandone la visibilità, l’animazione e l’avvio del drag & drop
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //utili per la non visibilità della zona
        holder.title.setText(titles.get(position));
        holder.cardview.setId(ids.get(position));
        vecchioTitolo = titles.get(position);
        if(!editMode){
            holder.eye.setVisibility(View.INVISIBLE);
            holder.cestino.setVisibility(View.INVISIBLE);
            holder.editname.setVisibility(View.INVISIBLE);

            holder.title.setText(titles.get(position));
            holder.cardview.setId(ids.get(position));

        }else{
            holder.eye.setVisibility(View.VISIBLE);
            holder.cestino.setVisibility(View.VISIBLE);
            holder.editname.setVisibility(View.VISIBLE);

            //animation and drag & drop
            setAnimation(holder.cardview, position);
            holder.title.setText(new StringBuilder().append(position+1)); //id zona
            holder.title.setText(titles.get(position)); //nome della zona

            //start drag & drop
            holder.cardview.setOnLongClickListener(v -> {
                listenerh.onStartDrag(holder);
                return false;
            });

        }

        if (visibility.get(position)==0){
            holder.cardviewOverflow.setVisibility(View.VISIBLE);
            holder.eye.setVisibility(View.INVISIBLE);
            holder.editname.setVisibility(View.INVISIBLE);
            holder.closeyee.setVisibility(View.VISIBLE);
            holder.cestinoo.setVisibility(View.VISIBLE);
            holder.editnamee.setVisibility(View.VISIBLE);
        }else{
            holder.cardviewOverflow.setVisibility(View.INVISIBLE);
        }

    }


    //animation cardiview for drag & drop
    private void setAnimation(CardView cardview, int position) {
        Animation animation = AnimationUtils.loadAnimation(ctx, R.anim.shake_animation);
        cardview.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }


    //Descrizione per singoli itemView
    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.title)
        TextView title;
        EditText edittitle;
        @BindView(R.id.cardView)
        CardView cardview;
        ImageButton eye;
        ImageButton closeye;
        ImageButton editname;
        ImageButton closeyee;
        ImageButton cestino;
        ImageButton cestinoo;
        ImageButton editnamee;
        @BindView(R.id.cardViewOverFlow)
        CardView cardviewOverflow;
        Unbinder unbinder;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //testo, icone occhio, occhio chiuso, cestino, matita e card quando sono vosibili e non
            title = (TextView) itemView.findViewById(R.id.ZonaText);
            cardview = (CardView) itemView.findViewById(R.id.cardView);
            eye = (ImageButton) itemView.findViewById(R.id.imageButton2);
            closeye = (ImageButton) itemView.findViewById(R.id.imageButtonCloseEye);
            editname = (ImageButton) itemView.findViewById(R.id.imageButtonName);
            closeyee = (ImageButton) itemView.findViewById(R.id.imageButtonCloseEyee);
            cestino = (ImageButton) itemView.findViewById(R.id.imageButton3);
            cestinoo = (ImageButton) itemView.findViewById(R.id.imageButton33);
            editnamee = (ImageButton) itemView.findViewById(R.id.imageButtonNamee);
            cardviewOverflow = (CardView) itemView.findViewById(R.id.cardViewOverFlow);

            //unbinder for drag&drop
            unbinder = ButterKnife.bind(title);
            unbinder = ButterKnife.bind(cardview);
            unbinder = ButterKnife.bind(cardviewOverflow);

            eye.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Homepage)ctx).startLoader();
                    setInvisibilityZone(cardview.getId());
                }
            });


            cestino.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupEliminazione();
                }
            });


            closeye.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Homepage)ctx).startLoader();
                    setVisibilityZone(cardview.getId());
                }
            });

            editname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupNuovoNomeZona(cardview.getId());
                }
            });

            cestinoo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupEliminazione();
                }
            });


            closeyee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Homepage)ctx).startLoader();
                    setVisibilityZone(cardview.getId());
                }
            });

            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!editMode){

                        String nomeZona = titles.get(ids.indexOf(cardview.getId()));

                        Intent intent = new Intent(ctx, OpereActivity.class);// SOSTITUIRE OpereZona con OpereActivity (Elisa)

                        intent.putExtra("nome_zona", nomeZona);
                        ctx.startActivity(intent);

                    }
                }
            });
        }

        private void setInvisibilityZone(Integer id) {
            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    ((Homepage)ctx).populateZoneAll();
                }
            }).execute(Database.FLAG_UPDATE, "UPDATE zone SET visibility = 0 WHERE id="+id+" && idMuseo="+Homepage.ID_MUSEO);
        }


        private void setVisibilityZone(Integer id) {
            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    ((Homepage)ctx).populateZoneAll();
                }
            }).execute(Database.FLAG_UPDATE, "UPDATE zone SET visibility = 1 WHERE id="+id+" && idMuseo="+Homepage.ID_MUSEO);
        }

        private void deleteZone(Integer id) {
            System.out.println("ID ZONA DA ELIMINARE = "+id);
            Database asyncTask = (Database) new Database(new Database.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    ((Homepage)ctx).populateZoneAll();
                }
            }).execute(Database.FLAG_DELETE, "DELETE from zone where id="+id+" && idMuseo="+Homepage.ID_MUSEO);
        }


        public void popupEliminazione(){
            dialogg = new Dialog(ctx);
            dialogg.setContentView(R.layout.popupeliminazione);
            dialogg.getWindow().setBackgroundDrawableResource(R.drawable.background);
            dialogg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogg.setCancelable(false);

            Button elimina = dialogg.findViewById(R.id.btn_elimina);
            Button annull = dialogg.findViewById(R.id.btn_annulla);

            dialogg.show();

            elimina.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Homepage)ctx).startLoader();
                    deleteZone(cardview.getId());
                    dialogg.dismiss();
                    Toast.makeText(ctx, R.string.zona_eliminata, Toast.LENGTH_SHORT).show();
                }
            });

            annull.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogg.dismiss();
                }
            });


        }


        public void popupNuovoNomeZona(Integer id) {
            //POPUP
            dialogg = new Dialog(ctx);
            dialogg.setContentView(R.layout.popup_cambianomezona);
            dialogg.getWindow().setBackgroundDrawableResource(R.drawable.background);
            dialogg.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogg.setCancelable(false);

            EditText zoneName = (EditText) dialogg.findViewById(R.id.conferma);
            Button confirm = dialogg.findViewById(R.id.btn_confirm);
            Button annulla = dialogg.findViewById(R.id.btn_cancel);

            dialogg.show();

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = zoneName.getText().toString();

                    if (!name.isEmpty()) {
                        //caricamento..insert DB
                        Database nomeZonaVecchio = (Database) new Database(new Database.AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                try {

                                    while (Database.rs.next()) {
                                        vecchioTitolo = Database.rs.getString("Nome");
                                    }
                                    Database aggiornamentoNomeZona = (Database) new Database(new Database.AsyncResponse() {

                                        @Override
                                        public void processFinish(String output) {

                                            boolean result = Boolean.parseBoolean(output);

                                            if (result) {
                                                ((Homepage)ctx).populateZoneAll();
                                                Toast.makeText(ctx, R.string.zona_modificata_con_successo, Toast.LENGTH_SHORT).show();
                                                dialogg.dismiss();
                                            } else {
                                                Toast.makeText(ctx, R.string.errore_modifica_zona, Toast.LENGTH_SHORT).show();
                                                dialogg.dismiss();
                                            }

                                            Database aggiornamentoZoneOpere = (Database) new Database(new Database.AsyncResponse() {
                                                @Override
                                                public void processFinish(String output) {

                                                }
                                            }).execute(Database.FLAG_UPDATE, "UPDATE opere SET Zona = '"+name+"' WHERE Zona = '"+vecchioTitolo+"' && idMuseo="+Homepage.ID_MUSEO);

                                        }
                                    }).execute(Database.FLAG_UPDATE, "UPDATE zone SET Nome='"+name+"' WHERE id="+id+" && idMuseo="+Homepage.ID_MUSEO);

                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            }
                        }).execute(Database.FLAG_SELECT_RAW, "SELECT Nome FROM zone WHERE id = "+id);

                    } else {
                        Toast.makeText(ctx, R.string.nome_invalido, Toast.LENGTH_SHORT).show();
                    }

                }
            });

            annulla.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogg.dismiss();
                }
            });


        }


    }




    public Filter getFilter() {
        if (customFilter == null) {
            customFilter = new CustomFilter();
        }
        return customFilter;
    }


    // INNER CLASS per ricerca zona
    class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();

            if(charSequence != null && charSequence.length() > 0) {
                charSequence = charSequence.toString().toUpperCase();

                List<String> ricerca = new ArrayList<String>();

                for (int i = 0; i < nomiZoneCercate.size(); i++) {
                    if (nomiZoneCercate.get(i).toUpperCase().contains(charSequence)) {
                        String nomeZonaCurr = nomiZoneCercate.get(i);
                        ricerca.add(nomeZonaCurr);
                    }
                }

                results.count = ricerca.size();
                results.values = ricerca;
            }
            else {
                results.count = nomiZoneCercate.size();
                results.values = nomiZoneCercate;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            titles = (List<String>) results.values;
            notifyDataSetChanged();
        }
    }

}
