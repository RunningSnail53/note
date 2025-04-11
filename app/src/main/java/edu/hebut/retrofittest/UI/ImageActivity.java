package edu.hebut.retrofittest.UI;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import edu.hebut.retrofittest.R;

public class ImageActivity extends AppCompatActivity {
    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        path = getIntent().getStringExtra("path");
        Glide.with(this).load(path).into((ImageView) findViewById(R.id.ivPic));
    }
}