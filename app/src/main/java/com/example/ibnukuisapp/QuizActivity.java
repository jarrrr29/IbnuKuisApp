package com.example.ibnukuisapp;

import android.content.Context; //
import android.content.Intent;
import android.hardware.Sensor; //
import android.hardware.SensorEvent; //
import android.hardware.SensorEventListener; //
import android.hardware.SensorManager; //
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

    // Variabel Sensor
    SensorManager sensorManager; //
    Sensor accelerometer; //
    Sensor lightSensor; //

    // Thresholds untuk deteksi goyangan
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F; // Ambang batas gravitasi untuk deteksi goyangan
    private static final int SHAKE_SLOPE_WINDOW = 200; // Jendela waktu untuk deteksi goyangan (ms)
    private long lastShakeTime;
    private float lastX, lastY, lastZ;

    // Threshold untuk deteksi senter
    private static final float LIGHT_THRESHOLD_INCREASE = 1000f; // Peningkatan lux minimum untuk deteksi senter
    private float initialLightLevel = -1f; // Level cahaya awal untuk perbandingan

    // Status untuk menunggu interaksi sensor
    boolean waitingForRightShake = false;
    boolean waitingForGeneralShake = false;
    boolean waitingForFlashlight = false;
    boolean isGameEnded = false; // Flag untuk melacak apakah game sudah berakhir

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

        // Inisialisasi SensorManager dan Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); //
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //
        }

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
            isGameEnded = false; // Reset flag game ended
            btnMainUlang.setVisibility(View.GONE);
            btnKeluar.setVisibility(View.GONE);
            showAllButtons();
            loadQuestion();
        });

        // Tombol "Keluar"
        btnKeluar.setOnClickListener(v -> finishAffinity()); // keluar aplikasi

        loadQuestion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Daftarkan listener sensor saat aktivitas dilanjutkan
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME); //
        }
        if (lightSensor != null) {
            sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL); //
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Batalkan pendaftaran listener sensor saat aktivitas dijeda untuk menghemat baterai
        sensorManager.unregisterListener(sensorListener); //
    }

    // Implementasi SensorEventListener
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) { //
            if (isGameEnded) return; // Jangan proses sensor jika game sudah berakhir

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { //
                float x = event.values[0]; //
                float y = event.values[1]; //
                float z = event.values[2]; //

                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > SHAKE_SLOPE_WINDOW) {
                    float deltaX = Math.abs(x - lastX);
                    float deltaY = Math.abs(y - lastY);
                    float deltaZ = Math.abs(z - lastZ);

                    // Deteksi Goyangan Umum (untuk melanjutkan setelah jawaban salah)
                    if (waitingForGeneralShake && (deltaX > SHAKE_THRESHOLD_GRAVITY || deltaY > SHAKE_THRESHOLD_GRAVITY || deltaZ > SHAKE_THRESHOLD_GRAVITY)) {
                        Toast.makeText(QuizActivity.this, "Goyangan Terdeteksi! Melanjutkan...", Toast.LENGTH_SHORT).show();
                        waitingForGeneralShake = false;
                        waitingForFlashlight = false; // Batalkan mode senter
                        initialLightLevel = -1f; // Reset level cahaya
                        index++;
                        if (index < questions.length) {
                            loadQuestion();
                        } else {
                            endGame();
                        }
                        return; // Penting untuk kembali setelah aksi sensor
                    }

                    // Deteksi Goyangan ke Kanan (setelah jawaban benar)
                    // Perlu kalibrasi ambang batas untuk mendeteksi goyangan kanan dengan akurat
                    // Misalnya, x positif signifikan, y dan z relatif stabil
                    if (waitingForRightShake && x > SHAKE_THRESHOLD_GRAVITY * 1.5 && Math.abs(y) < SHAKE_THRESHOLD_GRAVITY / 2 && Math.abs(z) < SHAKE_THRESHOLD_GRAVITY / 2) {
                        Toast.makeText(QuizActivity.this, "Goyangan Kanan Terdeteksi! 🎉", Toast.LENGTH_SHORT).show();
                        waitingForRightShake = false;
                        index++;
                        if (index < questions.length) {
                            loadQuestion();
                        } else {
                            endGame();
                        }
                        return; // Penting untuk kembali setelah aksi sensor
                    }
                    lastShakeTime = currentTime;
                }
                lastX = x;
                lastY = y;
                lastZ = z;

            } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) { //
                if (waitingForFlashlight) {
                    float currentLight = event.values[0]; //

                    if (initialLightLevel == -1f) {
                        initialLightLevel = currentLight; // Ambil nilai cahaya awal saat mode senter aktif
                    }

                    // Deteksi senter (peningkatan cahaya drastis)
                    // Perlu kalibrasi nilai LIGHT_THRESHOLD_INCREASE
                    if (currentLight > initialLightLevel + LIGHT_THRESHOLD_INCREASE) {
                        Toast.makeText(QuizActivity.this, "Senter Terdeteksi! Jawaban Terungkap!", Toast.LENGTH_SHORT).show();
                        tvQuestion.setText("Jawaban Benar: " + options[index][correctAnswers[index]]); // Tampilkan jawaban
                        // Opsional: tambahkan penalti di sini, misal score -= 5; atau lives--;

                        // Nonaktifkan deteksi senter sementara atau setelah menampilkan jawaban
                        waitingForFlashlight = false; // Menonaktifkan agar tidak terus-menerus mendeteksi senter
                        initialLightLevel = -1f; // Reset level cahaya untuk deteksi berikutnya

                        // Berikan waktu bagi user untuk membaca jawaban, lalu kembali ke perintah goyangkan
                        new CountDownTimer(3000, 1000) { // Tampilkan jawaban selama 3 detik
                            @Override
                            public void onTick(long millisUntilFinished) {
                                // Tidak ada aksi selama tick
                            }

                            @Override
                            public void onFinish() {
                                // Setelah 3 detik, kembali ke instruksi goyangkan
                                if (!isGameEnded) { // Pastikan game belum berakhir saat kembali
                                    tvQuestion.setText("Salah! Jawaban yang benar adalah: " + options[index][correctAnswers[index]] + "\nGoyangkan handphone untuk melanjutkan.");
                                    waitingForGeneralShake = true; // Kembali menunggu goyangan
                                    waitingForFlashlight = true; // Aktifkan lagi mode senter jika user ingin mencoba lagi
                                }
                            }
                        }.start();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { //
            // Tidak ada implementasi khusus yang diperlukan di sini
        }
    };

    void loadQuestion() {
        if (index >= questions.length) {
            endGame();
            return;
        }

        // Pastikan tidak ada state sensor yang aktif saat memuat pertanyaan baru
        waitingForRightShake = false;
        waitingForGeneralShake = false;
        waitingForFlashlight = false;
        initialLightLevel = -1f; // Reset light level

        tvQuestion.setText(questions[index]);
        btnA.setText(options[index][0]);
        btnB.setText(options[index][1]);
        btnC.setText(options[index][2]);
        btnD.setText(options[index][3]);

        tvScore.setText("Skor: " + score);
        tvLives.setText("Nyawa: " + lives);
        showAllButtons(); // Pastikan tombol pilihan terlihat
        startTimer();
    }

    void startTimer() {
        if (timer != null) timer.cancel();

        timer = new CountDownTimer(20000, 1000) { // Timer 20 detik
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Timer: " + millisUntilFinished / 1000); //
            }

            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Waktu habis!", Toast.LENGTH_SHORT).show(); //
                lives--; //
                if (lives <= 0) { //
                    endGame(); //
                } else {
                    // Jika waktu habis, langsung tampilkan jawaban benar dan minta goyangan untuk melanjutkan
                    String correct = options[index][correctAnswers[index]];
                    tvQuestion.setText("Waktu habis! Jawaban yang benar adalah: " + correct + "\nGoyangkan handphone untuk melanjutkan, atau senter soal ini untuk melihat jawabannya!");
                    hideAllButtons();
                    waitingForGeneralShake = true;
                    waitingForFlashlight = true;
                    initialLightLevel = -1f;
                }
            }
        }.start();
    }

    void checkAnswer(String selected) { //
        if (timer != null) timer.cancel(); //

        String correct = options[index][correctAnswers[index]]; //
        if (selected.equals(correct)) { //
            score += 10; //
            Toast.makeText(this, "Benar!", Toast.LENGTH_SHORT).show(); //
            tvQuestion.setText("Benar! Goyangkan handphone Anda ke kanan 🎉 untuk melanjutkan.");
            hideAllButtons(); // Sembunyikan tombol jawaban
            waitingForRightShake = true; // Aktifkan deteksi goyangan kanan
        } else {
            lives--; //
            Toast.makeText(this, "Salah!", Toast.LENGTH_SHORT).show(); // Pesan asli "Salah! Jawaban: " + correct diganti
            tvQuestion.setText("Salah! Jawaban yang benar adalah: " + correct + "\nGoyangkan handphone untuk melanjutkan, atau senter soal ini untuk melihat jawabannya!");
            hideAllButtons(); // Sembunyikan tombol jawaban
            waitingForGeneralShake = true; // Aktifkan deteksi goyangan umum
            waitingForFlashlight = true; // Aktifkan deteksi senter
            initialLightLevel = -1f; // Reset level cahaya untuk deteksi senter baru
        }

        // Logic untuk mengakhiri game sekarang ditangani di dalam onSensorChanged setelah aksi goyangan,
        // atau langsung jika nyawa habis.
        if (lives <= 0) {
            endGame(); // Jika nyawa habis, langsung panggil endGame
        }
        // Jika semua pertanyaan terjawab, endGame akan dipanggil di loadQuestion() setelah index mencapai batas
    }

    void endGame() { //
        if (timer != null) timer.cancel(); //

        isGameEnded = true; // Set flag game ended
        tvQuestion.setText("Permainan selesai!\nSkor akhir: " + score); //
        hideAllButtons(); //
        btnMainUlang.setVisibility(View.VISIBLE); //
        btnKeluar.setVisibility(View.VISIBLE); //

        // Pastikan semua listener sensor dinonaktifkan
        waitingForRightShake = false;
        waitingForGeneralShake = false;
        waitingForFlashlight = false;
        initialLightLevel = -1f; // Reset light level
    }

    void hideAllButtons() { //
        btnA.setVisibility(View.GONE); //
        btnB.setVisibility(View.GONE); //
        btnC.setVisibility(View.GONE); //
        btnD.setVisibility(View.GONE); //
    }

    void showAllButtons() { //
        btnA.setVisibility(View.VISIBLE); //
        btnB.setVisibility(View.VISIBLE); //
        btnC.setVisibility(View.VISIBLE); //
        btnD.setVisibility(View.VISIBLE); //
    }
}