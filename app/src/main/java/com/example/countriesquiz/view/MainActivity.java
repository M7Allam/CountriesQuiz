package com.example.countriesquiz.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.countriesquiz.R;
import com.example.countriesquiz.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MediaPlayer mediaBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Layout inflate
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Buttons Click
        binding.buttonSinglePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                stopAudioBackground();
                startActivity(intent);
            }
        });
        binding.buttonMultiPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Soon...",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startAudioBackground();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAudioBackground();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaBackground.stop();
    }

    private void startAudioBackground(){
        mediaBackground = MediaPlayer.create(this, R.raw.music_background);
        if(!mediaBackground.isPlaying()){
            mediaBackground.setLooping(true);
            mediaBackground.start();
        }

    }

    private void stopAudioBackground(){
        if(mediaBackground.isPlaying()){
            mediaBackground.stop();
        }
    }


}
