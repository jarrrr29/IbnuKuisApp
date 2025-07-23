package com.example.ibnukuisapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText edtName;
    Button btnMulai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edtName);
        btnMulai = findViewById(R.id.btnMulai);

        btnMulai.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Masukkan nama terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                intent.putExtra("player_name", name);
                startActivity(intent);
            }
        });
    }
}
