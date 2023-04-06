package it.uniba.pathway;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.constraintlayout.widget.ConstraintLayout;


/**
 * Classe che estende la classe ListView, ovvero una view che permette di visualizzare al suo
 * interno un insieme di views, in modo che siano scorrevoli in senso verticale.
 */
public class CustomListView extends ListView {
    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;


    /**
     * Dichiarazione dell'interfaccia Listener e del suo metodo swapElements, implementato
     * nell'activity ListSortingActivity, nel metodo setListDragDropAdapter().
     */
    public interface Listener {
        void swapElements(int indexOne, int indexTwo);
    }

    private int mLastEventY = -1;

    private int mDownY = -1;
    private int mDownX = -1;

    private int mTotalOffset = 0;

    private boolean mCellIsMobile = false;
    private boolean mIsMobileScrolling = false;
    private int mSmoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;
    private int rowWidth = -1;

    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean mIsWaitingForScrollFinish = false;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    Listener listener;
    ConstraintLayout selectedView;


    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setOnScrollListener(mScrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mSmoothScrollAmountAtEdge = (int)(SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }


    /**
     * Metodo onGrab, chiamato nell'activity ListSortingActivity nel metodo setListDragDropAdapter().
     * <p>
     *     Nel momento in cui un'opera viene "presa", tramite l'adapter si determina l'ID della
     *     riga selezionata, si crea la riga sopraelevata tramite il metodo getAndAddHoverView() e
     *     viene resa invisibile l'opera sopraelevata nella lista effettiva. Inoltre, vengono anche
     *     aggiornate le opere che si trovano attorno alla riga sopraelevata mentre viene trascinata.
     * </p>
     * <br>
     * @param position posizione della riga dell'opera corrente nella lista delle opere
     * @param selectedView layout della riga dell'opera (file opere_item.xml)
     */
    public void onGrab(int position, ConstraintLayout selectedView) {
        mTotalOffset = 0;
        mMobileItemId = getAdapter().getItemId(position);
        mHoverCell = getAndAddHoverView(selectedView);
        this.selectedView = selectedView;
        selectedView.setVisibility(INVISIBLE);

        mCellIsMobile = true;

        updateNeighborViewsForID(mMobileItemId);
    }

    final static int topPadding = 5;
    final static int bottomPadding = 10;
    final static int rightPadding = 10;
    final static int shadowPadding = topPadding + bottomPadding;


    /**
     * Crea la riga sopraelevata con la bitmap appopriata, tramite una chiamata al metodo
     * getBitmapFromView(), che trasforma la vista passata in input nella bitmap corrispondente,
     * e con le dimensioni appropriate. La BitmapDrawable della riga sopraelevata viene posta appena
     * sopra la bitmap ogni volta che viene effettuata una chiamata non valida.
     * <br><br>
     * @param v è la vista che viene trasformata in Bitmap e successivamente in BitmapDrawable
     * @return la BitmapDrawable costruita a partire dalla view in input
     */
    private BitmapDrawable getAndAddHoverView(View v) {

        int w = v.getWidth() + rightPadding;
        rowWidth = v.getWidth();
        int h = v.getHeight() + bottomPadding;
        int top = v.getTop() - topPadding;
        int left = v.getLeft();

        Bitmap b = getBitmapFromView(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }


    /**
     * Restituisce una bitmap che mostra uno screenshot della vista passata in input.
     * <br><br>
     * @param v è la vista che viene trasformata in Bitmap
     * @return la bitmap che viene creata a partire dalla view passata in input
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth() + rightPadding,
                v.getHeight() + shadowPadding, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas (bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.opera_background));
        paint.setShadowLayer(8, 2, 4, getResources().getColor(R.color.opera_background));

        Rect rect = new Rect(5, 0, v.getWidth() - rightPadding, v.getHeight());
        canvas.drawRect(rect, paint);

        Rect st = new Rect(5, 0, v.getWidth() - rightPadding, v.getHeight());
        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(1);
        paint2.setColor(getResources().getColor(R.color.opera_background));
        canvas.drawRect(st, paint2);

        Bitmap b = Bitmap.createBitmap(v.getWidth() - 15, v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas (b);

        v.draw(can);

        canvas.drawBitmap(b, 0, 0, null);
        return bitmap;
    }


    /**
     * Memorizza un riferimento alle view che si trovano sopra e sotto l'elemento che attualmente
     * corrisponde alla riga sopraelevata.
     * <p>
     *     Infatti, viene passato l'ID dell'elemento corrente, da cui si può ricavare la posizione
     *     all'interno della lista e, di conseguenza, la posizione delle viste attorno.
     * </p>
     * <p>
     *     NB : se l'elemento corrente si trova all'inizio o alla fine della lista, mAboveItemId o
     *     mBelowItemId potrebbero risultare invalidi
     * </p>
     * <br>
     * @param itemID l'ID dell'elemento corrente
     */
    private void updateNeighborViewsForID(long itemID) {
        int position = getPositionForID(itemID);
        ListDragDropAdapter adapter = ((ListDragDropAdapter)getAdapter());
        mAboveItemId = adapter.getItemId(position - 1);
        mBelowItemId = adapter.getItemId(position + 1);
    }


    /**
     * Restituisce la view nella lista che corrisponde all'ID passato come parametro,
     * tramite l'adapter ListDragDropAdapter.
     * <br><br>
     * @param itemID ID dell'elemento corrente
     * @return vista dell'elemento corrente
     */
    public View getViewForID (long itemID) {
        int firstVisiblePosition = getFirstVisiblePosition();
        ListDragDropAdapter adapter = ((ListDragDropAdapter)getAdapter());
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemID) {
                return v;
            }
        }
        return null;
    }


    /**
     * Restituisce la posizione della riga nella lista corrispondente all'ID dell'elemento corrente.
     * <br><br>
     * @param itemID ID dell'elemento corrente, da cui si ricava la vista
     * @return posizione della riga nella lista dell'elemento corrente
     */
    public int getPositionForID (long itemID) {
        View v = getViewForID(itemID);
        if (v == null) {
            return -1;
        } else {
            return getPositionForView(v);
        }
    }


    /**
     * Override del metodo dispatchDraw() della classe ListView, che permette di mantenere la riga
     * sopraelevata in BitmapDrawable sopra tutti gli elementi della lista, mentre la lista viene
     * modificata a causa dello spostamento della riga sopraelevata stessa. Questo metodo viene
     * invocato quando le altre righe della lista devono essere gestite dallo spostamento.
     * <br><br>
     * @param canvas oggetto di classe Canvas che rappresenta la riga sopraelevata
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        }
    }

    boolean enableX = false;
    boolean enableY = false;
    boolean isTrasboxHover = false;
    int enableStartX;
    int enableStartY;


    /**
     * Override del metodo onTouchEvent, che permette di gestire l'azione delle opere della lista
     * per ogni tipo di movimento.
     * <p>
     *     In particolare, gestisce i seguenti casi:<br>
     *         -ACTION_DOWN che indica l'inizio di un'azione e contiene la posizione iniziale del
     *             movimento<br>
     *         -ACTION_MOVE che indica l'avvenimento di una modifica durante il movimento e contiene
     *             il punto più recente, oltre a tutte le posizioni intermedie<br>
     *         -ACTION_UP che indica la fine di un'azione e contiene la posizione finale in cui la
     *             riga è stata rilasciata, oltre a tutte le posizioni intermedie<br>
     *         -ACTION_CANCEL che indica l'annullamento dell'azione corrente<br>
     *         -ACTION_POINTER_UP che gestisce eventuale multi-touch durante le azioni di drag&drop
     * </p>
     * <br>
     * @param event evento attuale, che viene categorizzato secondo le modalità sopra descritte
     * @return restituisce true o false in base ai parametri ottenuti durante il movimento e al
     *         tipo di movimento
     */
    @Override
    public boolean onTouchEvent (MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int)event.getX();
                mDownY = (int)event.getY();
                enableX = false;
                enableY = false;

                enableStartX = mDownX;
                enableStartY = mDownY;


                mActivePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);

                mLastEventY = (int) event.getY(pointerIndex);


                if (mCellIsMobile) {

                    int deltaX = 0;
                    int deltaY = 0;

                    final int x = (int)event.getX();
                    final int y = (int)event.getY();

                    int absX = Math.abs(mDownX - x);
                    int absY = Math.abs(mDownY - y);

                    if(absX > 10 && false == enableX && false == enableY) {
                        enableX = true;
                        enableY = false;
                        enableStartY = y;
                    }
                    else if(absY > 10 && false == enableY && false == enableX) {
                        enableX = false;
                        enableY = true;
                        enableStartX = x;
                    }

                    if(enableX) {
                        deltaX = mDownX - x;
                    }
                    else if(enableY) {
                        deltaY = mLastEventY - mDownY;
                    }
                    else {
                        deltaX = mDownX - x;
                        deltaY = mLastEventY - mDownY;
                    }

                    if(enableX && 100 < Math.abs(enableStartY - y)) {
                        enableX = false;
                        enableY = true;
                        enableStartX = x;
                    }
                    else if(enableY && 100 < Math.abs(enableStartX - x)) {
                        enableX = true;
                        enableY = false;
                        enableStartY = y;
                    }

                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left - deltaX,
                            mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();

                    if(enableY) {
                        hovoerTrashbox(false);
                        handleCellSwitch();
                    }
                    else {
                        if(deltaX > (rowWidth / 2) && false == isTrasboxHover) {
                            hovoerTrashbox(true);
                        }
                        else if(deltaX < (rowWidth / 2) && true == isTrasboxHover) {
                            hovoerTrashbox(false);
                        }
                    }

                    mIsMobileScrolling = false;
                    handleMobileCellScroll();

                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:

            {
                int x = (int)event.getX();
                int deltaX = mDownX - x;
                if(deltaX > (rowWidth / 2) && mCellIsMobile &&
                        isTrasboxHover) {

                    // Reaching to the left
                    touchEventsEnded();
                }
                else {
                    touchEventsEnded();
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    void hovoerTrashbox(boolean isTrasboxHover) {
        this.isTrasboxHover = isTrasboxHover;
    }


    /**
     * Determina se la riga sopraelevata è stata spostata abbastanza lontano da invocare lo
     * scambio tra le due righe. Se risulta abbastanza distante, viene determinata la riga
     * coinvolta dal movimento di quella sopraelevata e le opere nell'ArrayList vengono scambiate
     * di conseguenza. Dopo aver notificato la modifica effettuata sull'ArrayList, viene invocato
     * il layout che permette di riposizionare le righe nel nuovo ordine.
     * Utilizzando un ViewTreeObserver e l'OnPreDrawListener corrispondente, si può animare lo
     * spostamento della riga e il suo rilascio nella giusta posizione nella lista.
     */
    private void handleCellSwitch() {
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewForID(mBelowItemId);
        View mobileView = getViewForID(mMobileItemId);
        View aboveView = getViewForID(mAboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {

            final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
            View switchView = isBelow ? belowView : aboveView;

            if(null != switchView) {
                selectedView = (ConstraintLayout)switchView.findViewById(
                        R.id.opera_singola_layout);
            }

            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighborViewsForID(mMobileItemId);
                return;
            }

            listener.swapElements(originalItem, getPositionForView(switchView));

            ((BaseAdapter) getAdapter()).notifyDataSetChanged();

            mDownY = mLastEventY;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViewsForID(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    View switchView = getViewForID(switchItemID);

                    mTotalOffset += deltaY;

                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    switchView.setTranslationY(delta);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(switchView,
                            View.TRANSLATION_Y, 0);
                    animator.setDuration(MOVE_DURATION);
                    animator.start();

                    return true;
                }
            });
        }
    }


    /**
     * Quando l'azione termina, vengono reimpostati tutti gli elementi allo stato di default, mentre
     * viene eseguita l'animazione per posizionare la riga alla sua corretta posizione.
     */
    private void touchEventsEnded () {
        final View mobileView = getViewForID(mMobileItemId);
        if (mCellIsMobile|| mIsWaitingForScrollFinish) {
            mCellIsMobile = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left,
                    mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(
                    mHoverCell, "bounds", sBoundEvaluator,
                    mHoverCellCurrentBounds);

            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAboveItemId = INVALID_ID;
                    mMobileItemId = INVALID_ID;
                    mBelowItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    mHoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }


    /**
     * Quando l'azione viene annullata, vengono reimpostati tutti gli elementi allo stato di default.
     */
    private void touchEventsCancelled () {
        View mobileView = getViewForID(mMobileItemId);
        if (mCellIsMobile) {
            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            invalidate();
        }
        mCellIsMobile = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }


    /**
     * Il TypeEvaluator è un tipo di dato che viene utilizzato per animare la BitmapDrawable verso
     * la sua posizione finale quando l'utente rilascia la riga, modificandone la posizione e,
     * di conseguenza, le sue righe confinanti.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };


    /**
     * Determina se la lista sta scorrendo, ovvero quando la riga sopraelevata viene trascinata al
     * di fuori dei confini superiore o inferiore della lista, in modo da poter essere posizionata
     * in una posizione rispettivamente superiore o inferiore a quelle attualmente visibili.
     */
    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }


    /**
     * Determina se la riga sopraelevata si trova sopra o sotto i confini della lista. In tal caso,
     * la lista effettua uno scorrimento verso l'alto o verso il basso per mostrare altri elementi.
     * <p>
     *     La differenza con il metodo qui sopra è la presenza di un parametro di input di classe
     *     Rect e un valore di tipo boolean in output.
     * </p>
     * <br>
     * @param r oggetto di classe Rect, che contiene le coordinate dei quattro angoli del rettangolo
     *          che rappresenta la riga sopraelevata della lista
     * @return valore di tipo boolean, che assume valore <i>true</i> se la lista necessita di
     * scorrere oppure <i>false</i> se la riga sopraelevata è ancora nei confini della lista
     */
    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }


    /**
     * Implementazione dell'interfaccia OnScrollListener dichiarata all'interno della classe
     * AbsListView, con i suoi due metodi onScrollStateChanged() e onScroll().
     * <p>
     *     Il listener implementato permette di gestire lo scambio delle righe quando la riga
     *     si trova in cima o alla base della lista. Se la riga sopraelevata si trova in uno dei
     *     due confini (superiore o inferiore) della lista, la lista inizierà a scorrere.
     * </p>
     * <p>
     *     Mentre la lista scorre, si controlla continuamente se la nuova riga diventa visibile e
     *     determina se è possibile effettuare lo scambio delle righe.
     * </p>
     */
    private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener () {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;


        /**
         * Implementazione del metodo di callback onScroll() dell'interfaccia OnScrollListener.
         * <p>
         *     Il metodo viene invocato quando la lista ha effettuato uno scorrimento, controllando:<br>
         *         -se ci sono differenze tra la prima riga visibile prima e dopo lo scorrimento<br>
         *         -se ci sono differenze tra il numero di righe visibili prima e dopo lo scorrimento.
         * </p>
         * <br>
         * @param view la lista che ha effettuato lo scorrimento
         * @param firstVisibleItem l'indice della prima riga visibile
         * @param visibleItemCount il numero di righe visibili
         * @param totalItemCount il numero totale di righe della lista (comprese quelle non visibili)
         */
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
                    : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
                    : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }


        /**
         * Implementazione del metodo di callback onScrollStateChanged() dell'interfaccia OnScrollListener.
         * <p>
         *     Il metodo viene invocato mentre la lista sta scorrendo, in particolare prima che il
         *     prossimo elemento della lista venga visualizzato, ovvero prima che venga effettuata
         *     la chiamata al metodo ListDragDropAdapter.getView() per la prossima riga della lista.
         * </p>
         * <br>
         * @param view la lista che sta scorrendo
         * @param scrollState lo stato attuale dello scorrimento, che può assumere valore
         *                    SCROLL_STATE_TOUCH_SCROLL con valore 1, indicando che l'utente sta
         *                    scorrendo la lista, oppure valore SCROLL_STATE_IDLE con valore 0,
         *                    indicando che la lista non sta scorrendo.
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            mScrollState = scrollState;
            isScrollCompleted();
        }


        /**
         * Questo metodo gestisce due casi.
         * <p>
         *     Se la lista sta scorrendo perchè la riga sopraelevata si trova sopra o sotto i
         *     confini rispettivamente superiore o inferiore della lista, allora fa continuare
         *     lo scorrimento.
         * </p>
         * <p>
         *     Se, invece, la riga sopraelevata è stata rilasciata, viene invocata l'animazione
         *     con cui la riga torna alla sua corretta posizione, solo dopo che la lista è entrata
         *     nello stato SCROLL_STATE_IDLE.
         * </p>
         */
        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (mCellIsMobile && mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }


        /**
         * Determina se la lista è scorsa abbastanza da mostrare una nuova riga in cima alla lista.
         * In tal caso, vengono aggiornati i relativi parametri.
         */
        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }


        /**
         * Determina se la lista è scorsa abbastanza da mostrare una nuova riga in fondo alla lista.
         * In tal caso, vengono aggiornati i relativi parametri.
         */
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };
}
