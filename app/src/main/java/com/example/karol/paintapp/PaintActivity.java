package com.example.karol.paintapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class PaintActivity extends SurfaceView implements SurfaceHolder.Callback,
        Runnable {
    private SurfaceHolder mPojemnik; // pozwala kontrolować i monitorować powierzchnię
    private Thread mWatekRysujacy; // wątek, który odświeża kanwę
    private boolean mWatekPracuje = false; // flaga logiczna do kontrolowania pracy watku
    private Object mBlokada=new Object(); // obiekt do tworzenia sekcji krytycznych
    private Bitmap mBitmapa = null; // pola klasy
    private Canvas mKanwa = null;
    private Path mSciezka = null;
    // inicjalizacja pozycji kursora
    private float X_move=-1;
    private float Y_move=-1;
    private float X_moveOld=-1;
    private float Y_moveOld=-1;



    public PaintActivity(Context context, AttributeSet attrs) {
        super(context);
        // Pojemnik powierzchni - pozwala kontrolować i monitorować powierzchnię
        mPojemnik = getHolder();
        mPojemnik.addCallback(this);
    }


    public void wznowRysowanie() {
        // uruchomienie wątku rysującego
        mWatekRysujacy = new Thread(this);
        mWatekPracuje = true;
        mWatekRysujacy.start();
    }


    public void pauzujRysowanie() {
        mWatekPracuje = false;
    }


    //obsługa dotknięcia ekranu
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        Paint mFarba;
        mFarba = new Paint();
        // ustawienie szerokości linii
        mFarba.setColor(MainActivity.kolor);
        mFarba.setStrokeWidth(5);
        // styl rysowania – wypełnianie figur
        mFarba.setStyle(Paint.Style.FILL);
        // styl rysowania – rysowanie tylko konturu
        mFarba.setStyle(Paint.Style.STROKE);

        //sekcja krytyczna – modyfikacja rysunku na wyłączność
        synchronized (mBlokada) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pauzujRysowanie();
                    wznowRysowanie();
                    // zapisz pozycje
                    X_moveOld = event.getX();
                    Y_moveOld = event.getY();
                    mKanwa.drawCircle(X_moveOld, Y_moveOld, 8, mFarba);
                    break;

                case MotionEvent.ACTION_MOVE:
                    // zapisz nowe pozycje
                    X_move = event.getX();
                    Y_move = event.getY();
                    // rysuj nowa sciezke zaczynajac w starych, a konczac w
                    // nowych pozycjach
                    mSciezka = new Path();
                    mSciezka.moveTo(X_moveOld, Y_moveOld);
                    mSciezka.lineTo(X_move, Y_move);
                    mKanwa.drawPath(mSciezka, mFarba);
                    // zapisz nowe jako "stare" pozycje
                    X_moveOld = X_move;
                    Y_moveOld = Y_move;
                    break;

                case MotionEvent.ACTION_UP:
                    mKanwa.drawCircle(event.getX(), event.getY(), 8, mFarba);
                    break;
            }
        }
        return true;
    }


    //żeby lint nie wyświetlał ostrzeżeń - onTouchEvent i performClick trzeba
    //implementować razem
    public boolean performClick() {return super.performClick();}


    @Override
    public void run() {
        while (mWatekPracuje) {
            Canvas kanwa = null;
            try {
                // sekcja krytyczna - żaden inny wątek nie może używać pojemnika
                synchronized (mPojemnik) {
                    // czy powierzchnia jest prawidłowa
                    if (!mPojemnik.getSurface().isValid()) continue;
                    // zwraca kanwę, na której można rysować, każdy piksel
                    // kanwy w prostokącie przekazanym jako parametr musi być
                    // narysowany od nowa inaczej: rozpoczęcie edycji
                    // zawartości kanwy
                    kanwa = mPojemnik.lockCanvas(null);
                    //sekcja krytyczna – dostęp do rysunku na wyłączność
                    synchronized (mBlokada) {
                        if (mWatekPracuje) {
                            //rysowanie na lokalnej kanwie
                            // tworzenie bitmapy i związanej z nią kanwy
                            if(MainActivity.cleanScreen == true){ // resetowanie ekranu i wypelnianie bialym kolorem
                                mKanwa.drawARGB(255, 255, 255, 255);
                                MainActivity.setCleanScreen();
                            }
                            kanwa.drawBitmap(mBitmapa, 0, 0, null);
                        }
                    }
                }
            } finally {
                // w bloku finally - gdyby wystąpił wyjątek w powyższym
                // powierzchnia zostanie zostawiona w spójnym stanie
                if (kanwa != null) {
                    kanwa.drawBitmap(mBitmapa, 0, 0, null);
                    // koniec edycji kanwy i wyświetlenie rysunku na ekranie
                    mPojemnik.unlockCanvasAndPost(kanwa);
                }
            }
            try {
                Thread.sleep(1000 / 25); // 25
            } catch (InterruptedException e) {
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        wznowRysowanie(); // zabezpiecza przed czarnym ekranem na start aplikacji
        mBitmapa = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mKanwa = new Canvas(mBitmapa);
        mKanwa.drawARGB(255, 255, 255, 255); // zmalowanie na biało
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // zatrzymanie rysowania
        mWatekPracuje = false;
    }
}