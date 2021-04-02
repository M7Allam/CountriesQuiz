package com.example.countriesquiz.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;

import com.example.countriesquiz.R;
import com.example.countriesquiz.databinding.ActivityScoreBinding;

public class ScoreActivity extends AppCompatActivity {

    private ActivityScoreBinding binding;
    private Intent intent1, intent2;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private int gameNumber, points, correctAnswers, wrongAnswers, sharedRound, highestScore;
    private final String KEY_HIGHEST_SCORE = "Highest Score";

    //Activity Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Layout inflate
        binding = ActivityScoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get Score from Game Activity
        intent1 = getIntent();
        gameNumber = intent1.getIntExtra(GameActivity.KEY_GAME_NUMBER, 0);
        points = intent1.getIntExtra(GameActivity.KEY_POINTS, 0);
        correctAnswers = intent1.getIntExtra(GameActivity.KEY_CORRECT_ANSWER, 0);
        sharedRound = intent1.getIntExtra(GameActivity.KEY_SHARED_ROUND, 0);

        //Shared Preferences
        shared = PreferenceManager.getDefaultSharedPreferences(this);
        //get Highest Score from Shared Preferences
        highestScore = shared.getInt(KEY_HIGHEST_SCORE, 0);
        //Check Game points is highest score
        if(points > highestScore){
            highestScore = points;
            editor = shared.edit();
            editor.putInt(KEY_HIGHEST_SCORE, highestScore);
            editor.apply();
        }

        //set Score on View
        binding.tvScorePoints.setText(" " + points);
        binding.tvHighestScorePoints.setText(" " + highestScore);

        //Button Play Again on Click
        binding.buttonPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent2 = new Intent(ScoreActivity.this, GameActivity.class);
                gameNumber++;
                if(gameNumber %3 == 1){
                    sharedRound++;
                }
                intent2.putExtra(GameActivity.KEY_GAME_NUMBER, gameNumber);
                intent2.putExtra(GameActivity.KEY_SHARED_ROUND, sharedRound);
                startActivity(intent2);
                ScoreActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
