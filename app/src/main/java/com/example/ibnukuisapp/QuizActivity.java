package com.example.ibnukuisapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    TextView tvQuestion, tvScore, tvTimer, tvLives;
    Button btnA, btnB, btnC, btnD, btnMainUlang, btnKeluar;

    String[] questions = {
            "Dari negara mana kopi pertama kali dibudidayakan secara luas?",
            "Siapa yang membawa kopi ke Eropa pada abad ke-17?",
            "Kopi Arabika pertama kali ditemukan di mana?"
    };

    String[][] options = {
            {"Ethiopia", "Brasil", "Yaman", "India"},
            {"Pedagang Arab", "Penjelajah Portugis", "Pedagang Belanda", "Orang Prancis"},
            {"Etiopia", "Brazil", "Arab Saudi", "Vietnam"}
    };

    int[] correctAnswers = {2, 0, 0}; // indeks jawaban benar

    int index = 0;
    int score = 0;
    int lives = 3;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Inisialisasi view
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvLives = findViewById(R.id.tvLives);
        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnMainUlang = findViewById(R.id.btnMainUlang);
        btnKeluar = findViewById(R.id.btnKeluar);

        // Listener untuk pilihan jawaban
        btnA.setOnClickListener(v -> checkAnswer(btnA.getText().toString()));
        btnB.setOnClickListener(v -> checkAnswer(btnB.getText().toString()));
        btnC.setOnClickListener(v -> checkAnswer(btnC.getText().toString()));
        btnD.setOnClickListener(v -> checkAnswer(btnD.getText().toString()));

        // Tombol "Main Lagi"
        btnMainUlang.setOnClickListener(v -> {
            index = 0;
            score = 0;
            lives = 3;
            btnMainUlang.setVisibility(View.GONE);
            btnKeluar.setVisibility(View.GONE);
            showAllButtons();
            loadQuestion();
        });

        // Tombol "Keluar"
        btnKeluar.setOnClickListener(v -> finishAffinity()); // keluar aplikasi

        loadQuestion();
    }

    void loadQuestion() {
        if (index >= questions.length) {
            endGame();
            return;
        }

        tvQuestion.setText(questions[index]);
        btnA.setText(options[index][0]);
        btnB.setText(options[index][1]);
        btnC.setText(options[index][2]);
        btnD.setText(options[index][3]);

        tvScore.setText("Skor: " + score);
        tvLives.setText("Nyawa: " + lives);
        startTimer();
    }

    void startTimer() {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Timer: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Waktu habis!", Toast.LENGTH_SHORT).show();
                lives--;
                if (lives <= 0) {
                    endGame();
                } else {
                    index++;
                    loadQuestion();
                }
            }
        }.start();
    }

    void checkAnswer(String selected) {
        if (timer != null) timer.cancel();

        String correct = options[index][correctAnswers[index]];
        if (selected.equals(correct)) {
            score += 10;
            Toast.makeText(this, "Benar!", Toast.LENGTH_SHORT).show();
        } else {
            lives--;
            Toast.makeText(this, "Salah! Jawaban: " + correct, Toast.LENGTH_SHORT).show();
        }

        if (lives <= 0 || index + 1 > questions.length) {
            index++;
            endGame();
        } else {
            index++;
            loadQuestion();
        }
    }

    void endGame() {
        if (timer != null) timer.cancel();

        tvQuestion.setText("Permainan selesai!\nSkor akhir: " + score);
        hideAllButtons();
        btnMainUlang.setVisibility(View.VISIBLE);
        btnKeluar.setVisibility(View.VISIBLE);
    }

    void hideAllButtons() {
        btnA.setVisibility(View.GONE);
        btnB.setVisibility(View.GONE);
        btnC.setVisibility(View.GONE);
        btnD.setVisibility(View.GONE);
    }

    void showAllButtons() {
        btnA.setVisibility(View.VISIBLE);
        btnB.setVisibility(View.VISIBLE);
        btnC.setVisibility(View.VISIBLE);
        btnD.setVisibility(View.VISIBLE);
    }
}
