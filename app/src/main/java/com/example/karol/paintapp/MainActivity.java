package com.example.karol.paintapp;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class MainActivity extends Activity implements View.OnClickListener{
    private PaintActivity powierzchnia;
    public static int kolor = Color.BLACK;
    public static boolean cleanScreen = false;
    Button redButton, yellowButton, greenButton, blueButton, cleanScreenButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtons();
        powierzchnia = new PaintActivity(MainActivity.this, null);
    }


    public static void setCleanScreen(){
        cleanScreen = false;
    }


    private void setButtons(){
        // Odczytanie przycisku kazdego przycisku po id oraz ustawienie mu listenera
        redButton = (Button) findViewById(R.id.redButton);
        redButton.setOnClickListener(this);
        yellowButton = (Button) findViewById(R.id.yellowButton);
        yellowButton.setOnClickListener(this);
        greenButton = (Button) findViewById(R.id.greenButton);
        greenButton.setOnClickListener(this);
        blueButton = (Button) findViewById(R.id.blueButton);
        blueButton.setOnClickListener(this);
        cleanScreenButton = (Button) findViewById(R.id.cleanScreenButton);
        cleanScreenButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // Obsluga przyciskow
        switch (v.getId()) {
            case R.id.redButton:
                kolor=Color.RED;
                break;
            case R.id.yellowButton:
                kolor=Color.YELLOW;
                break;
            case R.id.blueButton:
                kolor=Color.BLUE;
                break;
            case R.id.greenButton:
                kolor=Color.GREEN;
                break;
            case R.id.cleanScreenButton:
                cleanScreen=true;
                break;
            default:
                break;
        }
    }
}

