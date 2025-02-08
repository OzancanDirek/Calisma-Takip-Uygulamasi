package com.ozancan.kronometre;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {
    TextView textView, progressText;
    EditText timeInput;
    Handler handler;
    Runnable runnable;
    int number = 0;
    int userSetTime = 60;
    Button start, stop, nightModeButton;
    ProgressBar progressBar;
    boolean isRunning = false;
    boolean isNightMode = false;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.mainLayout);
        textView = findViewById(R.id.textView);
        progressText = findViewById(R.id.progressText);
        timeInput = findViewById(R.id.timeInput);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        nightModeButton = findViewById(R.id.nightModeButton);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setMax(userSetTime);
        stop.setEnabled(false);
        handler = new Handler();

        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        isNightMode = preferences.getBoolean("NightMode", false);
        updateUI(isNightMode);

        nightModeButton.setOnClickListener(v -> {
            isNightMode = !isNightMode;
            preferences.edit().putBoolean("NightMode", isNightMode).apply();
            updateUI(isNightMode);
        });

        start.setOnClickListener(v -> startChronometer());
        stop.setOnClickListener(v -> stopChronometer());
    }

    private void updateUI(boolean isNightMode) {
        if (isNightMode) {
            layout.setBackgroundColor(Color.BLACK);
            textView.setTextColor(Color.WHITE);
            progressText.setTextColor(Color.LTGRAY);
            timeInput.setTextColor(Color.WHITE);
            timeInput.setHintTextColor(Color.GRAY);
            nightModeButton.setText("Gündüz Modu");
        } else {
            layout.setBackgroundColor(Color.WHITE);
            textView.setTextColor(Color.BLACK);
            progressText.setTextColor(Color.DKGRAY);
            timeInput.setTextColor(Color.BLACK);
            timeInput.setHintTextColor(Color.GRAY);
            nightModeButton.setText("Gece Modu");
        }
    }

    private void startChronometer() {
        if (!isRunning) {
            String input = timeInput.getText().toString();
            if (!input.isEmpty()) {
                userSetTime = Integer.parseInt(input);
                progressBar.setMax(userSetTime);
            }

            isRunning = true;
            number = 0;
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            start.setEnabled(false);
            stop.setEnabled(true);

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (number < userSetTime) {
                        number++;
                        updateTimerDisplay();
                        progressBar.setProgress(number);
                        progressText.setText((number * 100 / userSetTime) + "% tamamlandı");
                        handler.postDelayed(this, 1000);
                    } else {
                        stopChronometer();
                    }
                }
            };
            handler.post(runnable);
        }
    }

    private void updateTimerDisplay() {
        runOnUiThread(() -> textView.setText(formatTime(number) + " süresince çalışıyorsunuz"));
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return hours + " saat " + minutes + " dakika " + secs + " saniye";
        } else if (minutes > 0) {
            return minutes + " dakika " + secs + " saniye";
        } else {
            return secs + " saniye";
        }
    }

    private void stopChronometer() {
        new AlertDialog.Builder(this)
                .setTitle("Durdur")
                .setMessage("Sayaçı durdurmak istiyor musunuz?")
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (handler != null) {
                            handler.removeCallbacks(runnable);
                        }
                        isRunning = false;
                        start.setEnabled(true);
                        stop.setEnabled(false);
                        progressBar.setProgress(0);
                        progressText.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        textView.setText("Başlamak için Start'a basın");
                    }
                })
                .setNegativeButton("Hayır", (dialog, which) -> Toast.makeText(MainActivity.this, "Sayaç devam ediyor", Toast.LENGTH_SHORT).show())
                .show();
    }
}