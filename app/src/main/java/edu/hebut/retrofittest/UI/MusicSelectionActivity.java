package edu.hebut.retrofittest.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import edu.hebut.retrofittest.R;

public class MusicSelectionActivity extends AppCompatActivity {
    private TextView tvMusic1, tvMusic2, tvMusic3;
    public static String music;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_selection);
        tvMusic1 = findViewById(R.id.tvMusic1);
        tvMusic2 = findViewById(R.id.tvMusic2);
        tvMusic3 = findViewById(R.id.tvMusic3);
        tvMusic1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getIntent().putExtra("data", R.raw.brave+"");
                music = R.raw.brave+"";
                setResult(1, i);
                finish();
            }
        });
        tvMusic2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getIntent().putExtra("data", R.raw.lovers+"");
                music = R.raw.lovers+"";
                setResult(1, i);
                finish();
            }
        });
        tvMusic3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getIntent().putExtra("data", R.raw.snow+"");
                music = R.raw.snow+"";
                setResult(1, i);
                finish();
            }
        });
    }
}
