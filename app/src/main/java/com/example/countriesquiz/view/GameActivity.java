package com.example.countriesquiz.view;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.example.countriesquiz.Game;
import com.example.countriesquiz.R;
import com.example.countriesquiz.databinding.ActivityGameBinding;
import com.example.countriesquiz.model.City;
import com.example.countriesquiz.model.Country;
import com.example.countriesquiz.model.DatabaseAccess;
import com.example.countriesquiz.model.Question;

import java.util.ArrayList;


public class GameActivity extends AppCompatActivity implements Game, View.OnClickListener{

    //UI Related
    private ActivityGameBinding binding;
    private Intent intent1, intent2;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private final String SHARED_GAME1_FILE_NAME = "Game1";
    private final String SHARED_GAME2_FILE_NAME = "Game2";
    private final String SHARED_GAME3_FILE_NAME = "Game3";
    private final String SHARED_KEY_Question = "Question";
    public static final String KEY_SHARED_ROUND = "Shared Round";
    private int sharedRound = 0;

    //MediaPlayer Related
    private MediaPlayer mediaCounter, mediaCorrectAnswer, mediaWrongAnswer;

    //Database Related
    private DatabaseAccess access;
    private ArrayList<Country> listCountriesEasy = new ArrayList<>();
    private ArrayList<Country> listCountriesMedium = new ArrayList<>();
    private ArrayList<Country> listCountriesHard = new ArrayList<>();
    private ArrayList<Country> listCountriesVeryHard = new ArrayList<>();
    private ArrayList<Country> listCountriesExpert = new ArrayList<>();
    private ArrayList<Question> listQuestionsAll = new ArrayList<>();
    private ArrayList<City> listCities = new ArrayList<>();
    private int[] listPreviousGame1QuestionsIndexes;
    private int[] listPreviousGame2QuestionsIndexes;
    private int[] listPreviousGame3QuestionsIndexes ;
    private final String[] LEVELS = {"Easy", "Medium", "Hard", "Very Hard", "Expert"};
    //Game Related
    private int gameNumber = 1;
    private int gameRound = 1;
    private int correctButton = 0;
    private int selectedOption = 0;
    private boolean isCorrectAnswer = false;
    private int points = 0;
    private int numCorrect = 0;
    public static final String KEY_GAME_NUMBER = "Game";
    public static final String KEY_POINTS = "Points";
    public static final String KEY_CORRECT_ANSWER = "Correct Answer";
    //Timer Related
    private CountDownTimer timer;
    private final long timerTotalSeconds = 11000;
    private final long timerAnswerSeconds = 10000;
    private boolean isTimerRunning = false;


    //Activity Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Layout inflate
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Media Player
        mediaCorrectAnswer = MediaPlayer.create(this, R.raw.correct_answer);
        mediaWrongAnswer = MediaPlayer.create(this, R.raw.wrong_answer);

        //Button Actions
        binding.buttonOption1.setOnClickListener(this);
        binding.buttonOption2.setOnClickListener(this);
        binding.buttonOption3.setOnClickListener(this);
        binding.buttonOption4.setOnClickListener(this);

        //Prepare Game Data
        intent1= getIntent();
        gameNumber = intent1.getIntExtra(KEY_GAME_NUMBER, 1);
        sharedRound = intent1.getIntExtra(KEY_SHARED_ROUND, 0);
        points = 0;
        numCorrect = 0;

    //get Game's All Questions
        //get data from SharedPreferences before choose Questions
        listPreviousGame1QuestionsIndexes = getQuestionsFromSharedPreferences(shared, SHARED_GAME1_FILE_NAME);
        listPreviousGame2QuestionsIndexes = getQuestionsFromSharedPreferences(shared, SHARED_GAME2_FILE_NAME);
        listPreviousGame3QuestionsIndexes = getQuestionsFromSharedPreferences(shared, SHARED_GAME3_FILE_NAME);
        //get Questions from DB and choose Questions
        getAllQuestions();
        //save in SharedPreferences after choose Questions
        saveAllInSharedPreferences(gameNumber, sharedRound);

    //Round 1
        // set Question
        setQuestion();
        startQuestionTimer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAudioTimer();
            }
        },1000);

    }

    @Override
    public void onClick(View v) {
        //Stop timer
        stopQuestionTimer();
        stopAudioTimer();
        disableButtons();

        //Get Answer
        if(Integer.parseInt(binding.tvCounter.getText().toString()) > 0){
            //Get Answer from User
            selectedOption = getAnswer(v);
            //Check Answer Correct
            checkAnswer(selectedOption, correctButton, v);
            calculatePoints(isCorrectAnswer, gameRound);
        }

        //Change Question
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        },2000);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void printSharedBefore(int[] x, int num){
        System.out.println("Shared "+ num +" Indexes before save");
        for(int i=0; i<x.length; i++){
            System.out.println("Shared Question " + (i + 1) + " = " + x[i]);
        }
        System.out.println("----------------------------------------------------------------------------");
    }

    public void printQuestionGameAfterShared(){
        System.out.println("Game = " + gameNumber);
        for(int i=0; i<listQuestionsAll.size(); i++){
            System.out.println("Game Question "+(i+1)+" = "+ listQuestionsAll.get(i).getQuestionIndex());
        }
        System.out.println("----------------------------------------------------------------------------");
    }

    //Game Interface Methods
    @Override
    public ArrayList<Country> getCountryDataFromDB(String level) {
        ArrayList<Country> list;
        access = DatabaseAccess.getInstance(this);
        access.openDB();
        list = access.getCountryDataFromDB(level);
        access.closeDB();
        return list;
    }

    @Override
    public ArrayList<City> getCityDataFromDB() {
        ArrayList<City> list;
        access = DatabaseAccess.getInstance(this);
        access.openDB();
        list = access.getCityDataFromDB();
        access.closeDB();
        return list;
    }

    @Override
    public ArrayList<Question> chooseQuestions(ArrayList<Country> listEasy, ArrayList<Country> listMedium, ArrayList<Country> listHard, ArrayList<Country> listVeryHard, ArrayList<Country> listExpert, ArrayList<City> listCities,  int[] listPrevious1, int[] listPrevious2, int[] listPrevious3) {
        ArrayList<Question> listAllQuestions = new ArrayList<>();
        String countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital;
        int correctIndex, wrongIndex1, wrongIndex2, wrongIndex3;
        String wrongCapitalSameCountryName;
        int randomWrongCapitalSameCountryName;

        //Round 1
        //Level Easy
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listEasy.size()-1);
        }while(correctIndex == listPrevious1[0] || correctIndex == listPrevious1[1] || correctIndex == listPrevious1[2] || correctIndex == listPrevious2[0] || correctIndex == listPrevious2[1] || correctIndex == listPrevious2[2] || correctIndex == listPrevious3[0] || correctIndex == listPrevious3[1] || correctIndex == listPrevious3[2]);
        countryName = listEasy.get(correctIndex).getCountryName();
        correctCapital =  listEasy.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listEasy.size()-1);
        }while(correctIndex == wrongIndex1);
        wrong1Capital =  listEasy.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listEasy.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1);
        wrong2Capital =  listEasy.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";

            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listEasy.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2);
            wrong3Capital =  listEasy.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 2
        //Level Easy
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listEasy.size()-1);
        }while(correctIndex == listAllQuestions.get(0).getQuestionIndex() || correctIndex == listPrevious1[0] || correctIndex == listPrevious1[1] || correctIndex == listPrevious1[2] || correctIndex == listPrevious2[0] || correctIndex == listPrevious2[1] || correctIndex == listPrevious2[2] || correctIndex == listPrevious3[0] || correctIndex == listPrevious3[1] || correctIndex == listPrevious3[2]);

        countryName = listEasy.get(correctIndex).getCountryName();
        correctCapital =  listEasy.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listEasy.size()-1);
        }while(wrongIndex1 == correctIndex || wrongIndex1 == listAllQuestions.get(0).getQuestionIndex());
        wrong1Capital =  listEasy.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listEasy.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1 || wrongIndex2 == listAllQuestions.get(0).getQuestionIndex());
        wrong2Capital =  listEasy.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listEasy.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2 || wrongIndex3 == listAllQuestions.get(0).getQuestionIndex());
            wrong3Capital =  listEasy.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 3
        //Level Easy
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listEasy.size()-1);
        }while(correctIndex == listAllQuestions.get(0).getQuestionIndex() || correctIndex == listAllQuestions.get(1).getQuestionIndex() || correctIndex == listPrevious1[0] || correctIndex == listPrevious1[1] || correctIndex == listPrevious1[2] || correctIndex == listPrevious2[0] || correctIndex == listPrevious2[1] || correctIndex == listPrevious2[2] || correctIndex == listPrevious3[0] || correctIndex == listPrevious3[1] || correctIndex == listPrevious3[2]);
        countryName = listEasy.get(correctIndex).getCountryName();
        correctCapital =  listEasy.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listEasy.size()-1);
        }while(wrongIndex1 == correctIndex || wrongIndex1 == listAllQuestions.get(0).getQuestionIndex() || wrongIndex1 == listAllQuestions.get(1).getQuestionIndex());
        wrong1Capital =  listEasy.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listEasy.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1 || wrongIndex2 == listAllQuestions.get(0).getQuestionIndex() || wrongIndex2 == listAllQuestions.get(1).getQuestionIndex());
        wrong2Capital =  listEasy.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listEasy.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2 || wrongIndex3 == listAllQuestions.get(0).getQuestionIndex() || wrongIndex3 == listAllQuestions.get(1).getQuestionIndex());
            wrong3Capital =  listEasy.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 4
        //Level Medium
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listMedium.size()-1);
        }while(correctIndex == listPrevious1[3] || correctIndex == listPrevious1[4] || correctIndex == listPrevious1[5] || correctIndex == listPrevious2[3] || correctIndex == listPrevious2[4] || correctIndex == listPrevious2[5] || correctIndex == listPrevious3[3] || correctIndex == listPrevious3[4] || correctIndex == listPrevious3[5]);
        countryName = listMedium.get(correctIndex).getCountryName();
        correctCapital =  listMedium.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listMedium.size()-1);
        }while(correctIndex == wrongIndex1);
        wrong1Capital =  listMedium.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listMedium.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1);
        wrong2Capital =  listMedium.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listMedium.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2);
            wrong3Capital =  listMedium.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 5
        //Level Medium
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listMedium.size()-1);
        }while(correctIndex == listAllQuestions.get(3).getQuestionIndex() || correctIndex == listPrevious1[3] || correctIndex == listPrevious1[4] || correctIndex == listPrevious1[5] || correctIndex == listPrevious2[3] || correctIndex == listPrevious2[4] || correctIndex == listPrevious2[5] || correctIndex == listPrevious3[3] || correctIndex == listPrevious3[4] || correctIndex == listPrevious3[5]);

        countryName = listMedium.get(correctIndex).getCountryName();
        correctCapital =  listMedium.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listMedium.size()-1);
        }while(wrongIndex1 == correctIndex || wrongIndex1 == listAllQuestions.get(3).getQuestionIndex());
        wrong1Capital =  listMedium.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listMedium.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1 || wrongIndex2 == listAllQuestions.get(3).getQuestionIndex());
        wrong2Capital =  listMedium.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listMedium.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2 || wrongIndex3 == listAllQuestions.get(3).getQuestionIndex());
            wrong3Capital =  listMedium.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 6
        //Level Medium
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listMedium.size()-1);
        }while(correctIndex == listAllQuestions.get(3).getQuestionIndex() || correctIndex == listAllQuestions.get(4).getQuestionIndex() || correctIndex == listPrevious1[3] || correctIndex == listPrevious1[4] || correctIndex == listPrevious1[5] || correctIndex == listPrevious2[3] || correctIndex == listPrevious2[4] || correctIndex == listPrevious2[5] || correctIndex == listPrevious3[3] || correctIndex == listPrevious3[4] || correctIndex == listPrevious3[5]);
        countryName = listMedium.get(correctIndex).getCountryName();
        correctCapital =  listMedium.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listMedium.size()-1);
        }while(wrongIndex1 == correctIndex || wrongIndex1 == listAllQuestions.get(3).getQuestionIndex() || wrongIndex1 == listAllQuestions.get(4).getQuestionIndex());
        wrong1Capital =  listMedium.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listMedium.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1 || wrongIndex2 == listAllQuestions.get(3).getQuestionIndex() || wrongIndex2 == listAllQuestions.get(4).getQuestionIndex());
        wrong2Capital =  listMedium.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listMedium.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2 || wrongIndex3 == listAllQuestions.get(3).getQuestionIndex() || wrongIndex3 == listAllQuestions.get(4).getQuestionIndex());
            wrong3Capital =  listMedium.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 7
        //Level Hard
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listHard.size()-1);
        }while(correctIndex == listPrevious1[6] || correctIndex == listPrevious1[7] || correctIndex == listPrevious2[6] || correctIndex == listPrevious2[7] || correctIndex == listPrevious3[6] || correctIndex == listPrevious3[7]);
        countryName = listHard.get(correctIndex).getCountryName();
        correctCapital =  listHard.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listHard.size()-1);
        }while(correctIndex == wrongIndex1);
        wrong1Capital =  listHard.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listHard.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1);
        wrong2Capital =  listHard.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listHard.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2);
            wrong3Capital =  listHard.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 8
        //Level Hard
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listHard.size()-1);
        }while(correctIndex == listAllQuestions.get(6).getQuestionIndex() || correctIndex == listPrevious1[6] || correctIndex == listPrevious1[7] || correctIndex == listPrevious2[6] || correctIndex == listPrevious2[7] || correctIndex == listPrevious3[6] || correctIndex == listPrevious3[7]);

        countryName = listHard.get(correctIndex).getCountryName();
        correctCapital =  listHard.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listHard.size()-1);
        }while(wrongIndex1 == correctIndex || wrongIndex1 == listAllQuestions.get(6).getQuestionIndex());
        wrong1Capital =  listHard.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listHard.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1 || wrongIndex2 == listAllQuestions.get(6).getQuestionIndex());
        wrong2Capital =  listHard.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listHard.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2 || wrongIndex3 == listAllQuestions.get(6).getQuestionIndex());
            wrong3Capital =  listHard.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));


        //Round 9
        //Level Very Hard
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listVeryHard.size()-1);
        }while(correctIndex == listPrevious1[8] || correctIndex == listPrevious2[8]|| correctIndex == listPrevious3[8]);
        countryName = listVeryHard.get(correctIndex).getCountryName();
        correctCapital =  listVeryHard.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listVeryHard.size()-1);
        }while(correctIndex == wrongIndex1);
        wrong1Capital =  listVeryHard.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listVeryHard.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1);
        wrong2Capital =  listVeryHard.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listVeryHard.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2);
            wrong3Capital =  listVeryHard.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));

        //Round 10
        //Level Expert
        //Correct Answer
        do{
            correctIndex = randomIndex(0, listExpert.size()-1);
        }while(correctIndex == listPrevious1[9] || correctIndex == listPrevious2[9]|| correctIndex == listPrevious3[9]);
        countryName = listExpert.get(correctIndex).getCountryName();
        correctCapital =  listExpert.get(correctIndex).getCountryCapital();
        //Wrong Answer 1
        do{
            wrongIndex1 = randomIndex(0, listExpert.size()-1);
        }while(correctIndex == wrongIndex1);
        wrong1Capital =  listExpert.get(wrongIndex1).getCountryCapital();
        //Wrong Answer 2
        do{
            wrongIndex2 = randomIndex(0, listExpert.size()-1);
        }while(wrongIndex2 == correctIndex || wrongIndex2 == wrongIndex1);
        wrong2Capital =  listExpert.get(wrongIndex2).getCountryCapital();
        //Wrong Answer 3
        randomWrongCapitalSameCountryName = randomIndex(1,6);
        if(randomWrongCapitalSameCountryName == 3 && !correctCapital.contains(countryName.substring(0,4))){
            if(MainActivity.language.equals("ar")){
                wrongCapitalSameCountryName = "مدينة " + countryName ;
            }else
                wrongCapitalSameCountryName = countryName + " City";
            wrong3Capital = wrongCapitalSameCountryName;
            wrongIndex3 = -1;
        }else{
            do{
                wrongIndex3 = randomIndex(0, listExpert.size()-1);
            }while(wrongIndex3 == correctIndex || wrongIndex3 == wrongIndex1 || wrongIndex3 == wrongIndex2);
            wrong3Capital =  listExpert.get(wrongIndex3).getCountryCapital();
        }
        //Add Question
        listAllQuestions.add(new Question(countryName, correctCapital, wrong1Capital, wrong2Capital, wrong3Capital
                , correctIndex, wrongIndex1, wrongIndex2, wrongIndex3));

        return listAllQuestions;
    }

    @Override
    public void getAllQuestions() {
        //Get Data From DB
        listCountriesEasy = getCountryDataFromDB(LEVELS[0]);
        listCountriesMedium = getCountryDataFromDB(LEVELS[1]);
        listCountriesHard = getCountryDataFromDB(LEVELS[2]);
        listCountriesVeryHard = getCountryDataFromDB(LEVELS[3]);
        listCountriesExpert = getCountryDataFromDB(LEVELS[4]);
        listCities = getCityDataFromDB();
        //Choose 10 Questions from All levels
        listQuestionsAll = chooseQuestions(listCountriesEasy, listCountriesMedium, listCountriesHard, listCountriesVeryHard, listCountriesExpert, listCities, listPreviousGame1QuestionsIndexes, listPreviousGame2QuestionsIndexes, listPreviousGame3QuestionsIndexes);
    }

    @Override
    public int randomAnswers() {
        int correctOption=1;
        int random = randomIndex(1,12);
        switch(random){
            case 1:
            case 11:
                correctOption = 1;
                break;
            case 2:
            case 8:
            case 10:
                correctOption = 2;
                break;
            case 3:
            case 9:
            case 12:
                correctOption = 3;
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                correctOption = 4;
                break;
        }
        return correctOption;
    }

    @Override
    public void setQuestion() {
        //TextViews
        binding.tvRound.setText(getString(R.string.string_game_activity_tv_round) + " " + gameRound + "/" + listQuestionsAll.size());
        binding.tvPoints.setText(getString(R.string.string_game_activity_tv_points) + " " + points);
        binding.tvCountry.setText(listQuestionsAll.get(0).getQuestion());
        binding.tvCounter.setText(String.valueOf((timerAnswerSeconds)/1000));

        //Buttons
        correctButton = randomAnswers();
        switch(correctButton){
            case 1:
                binding.buttonOption1.setText(listQuestionsAll.get(0).getCorrectAnswer());
                binding.buttonOption2.setText(listQuestionsAll.get(0).getWrongAnswer1());
                binding.buttonOption3.setText(listQuestionsAll.get(0).getWrongAnswer2());
                binding.buttonOption4.setText(listQuestionsAll.get(0).getWrongAnswer3());
                break;
            case 2:
                binding.buttonOption2.setText(listQuestionsAll.get(0).getCorrectAnswer());
                binding.buttonOption1.setText(listQuestionsAll.get(0).getWrongAnswer1());
                binding.buttonOption3.setText(listQuestionsAll.get(0).getWrongAnswer2());
                binding.buttonOption4.setText(listQuestionsAll.get(0).getWrongAnswer3());
                break;
            case 3:
                binding.buttonOption3.setText(listQuestionsAll.get(0).getCorrectAnswer());
                binding.buttonOption2.setText(listQuestionsAll.get(0).getWrongAnswer1());
                binding.buttonOption1.setText(listQuestionsAll.get(0).getWrongAnswer2());
                binding.buttonOption4.setText(listQuestionsAll.get(0).getWrongAnswer3());
                break;
            case 4:
                binding.buttonOption4.setText(listQuestionsAll.get(0).getCorrectAnswer());
                binding.buttonOption2.setText(listQuestionsAll.get(0).getWrongAnswer1());
                binding.buttonOption3.setText(listQuestionsAll.get(0).getWrongAnswer2());
                binding.buttonOption1.setText(listQuestionsAll.get(0).getWrongAnswer3());
                break;
        }
    }

    @Override
    public int getAnswer(View view) {
        int selectedOption=1;
        switch (view.getId()) {
            case R.id.button_option1:
                selectedOption = 1;
                break;

            case R.id.button_option2:
                selectedOption = 2;
                break;

            case R.id.button_option3:
                selectedOption = 3;
                break;

            case R.id.button_option4:
                selectedOption = 4;
                break;
        }
        return selectedOption;
    }

    @Override
    public void checkAnswer(int selectedOption, int correctOption, View view) {
        if(selectedOption == correctOption){
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            mediaCorrectAnswer.start();
            isCorrectAnswer = true;
            numCorrect++;
        }else{
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            mediaWrongAnswer.start();
            switch(correctButton){
                case 1:
                    binding.buttonOption1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    binding.buttonOption2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    binding.buttonOption3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    binding.buttonOption4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }
            isCorrectAnswer = false;
        }
    }

    @Override
    public void calculatePoints(boolean isCorrectAnswer, int round) {
        int counterNum = Integer.parseInt(binding.tvCounter.getText().toString());

        switch(round){
            case 1:
            case 2:
            case 3:
                if(isCorrectAnswer) {
                    if(counterNum > 5){
                        points += 5 + 5*counterNum;
                    }else{
                        points += 2 + 2*counterNum;
                    }
                }
                break;

            case 4:
            case 5:
            case 6:
                if(isCorrectAnswer) {
                    if(counterNum > 5){
                        points += 10 + 10*counterNum;
                    }else{
                        points += 5 + 5*counterNum;
                    }
                }
                break;

            case 7:
            case 8:
                if(isCorrectAnswer) {
                    if(counterNum > 5){
                        points += 20 + 20*counterNum;
                    }else{
                        points += 10 + 10*counterNum;
                    }
                }
                break;

            case 9:
                if(isCorrectAnswer) {
                    if(counterNum > 5){
                        points += 40 + 40*counterNum;
                    }else{
                        points += 20 + 20*counterNum;
                    }
                }
                break;

            case 10:
                if(isCorrectAnswer) {
                    if(counterNum > 5){
                        points += 80 + 80*counterNum;
                    }else{
                        points += 40 + 40*counterNum;
                    }
                }
                break;
        }

        binding.tvPoints.setText(getString(R.string.string_game_activity_tv_points) + " " + points);


    }

    @Override
    public void changeQuestion() {
        //Check Round <= 10
        if(gameRound < listQuestionsAll.size()){
            gameRound++;
            //Random Answers
            correctButton = randomAnswers();
            //Animation
            playAnim(binding.tvCountry,0,0);
            playAnim(binding.buttonOption1,0,1);
            playAnim(binding.buttonOption2,0,2);
            playAnim(binding.buttonOption3,0,3);
            playAnim(binding.buttonOption4,0,4);
            binding.tvRound.setText(getString(R.string.string_game_activity_tv_round) +  " " + gameRound + "/" +listQuestionsAll.size());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.tvCounter.setText(String.valueOf((timerAnswerSeconds)/1000));
                }
            },250);
            startQuestionTimer();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enableButtons();
                    startAudioTimer();
                }
            },1000);
        }else{
            //go to Score Activity
            intent2 = new Intent(GameActivity.this, ScoreActivity.class);
            intent2.putExtra(KEY_GAME_NUMBER, gameNumber);
            intent2.putExtra(KEY_POINTS, points);
            intent2.putExtra(KEY_CORRECT_ANSWER, numCorrect);
            intent2.putExtra(KEY_SHARED_ROUND, sharedRound);
            startActivity(intent2);
            GameActivity.this.finish();
        }
    }

    @Override
    public void playAnim(final View view, final int value, final int viewNum){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500)
                .setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(value == 0){
                            //Set new Question
                            if(viewNum == 0){
                                ((TextView)view).setText(listQuestionsAll.get(gameRound-1).getQuestion());
                            }else{
                                switch(correctButton){
                                    case 1:
                                    case 11:
                                        switch(viewNum){
                                            case 1:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getCorrectAnswer());
                                                break;

                                            case 2:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer1());
                                                break;

                                            case 3:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer2());
                                                break;

                                            case 4:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer3());
                                                break;
                                        }
                                        break;

                                    case 2:
                                    case 8:
                                    case 10:
                                        switch(viewNum){
                                            case 1:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer1());
                                                break;

                                            case 2:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getCorrectAnswer());

                                                break;

                                            case 3:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer2());
                                                break;

                                            case 4:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer3());
                                                break;
                                        }
                                        break;

                                    case 3:
                                    case 9:
                                    case 12:
                                        switch(viewNum){
                                            case 1:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer2());
                                                break;

                                            case 2:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer1());
                                                break;

                                            case 3:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getCorrectAnswer());

                                                break;

                                            case 4:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer3());
                                                break;
                                        }
                                        break;

                                    case 4:
                                    case 5:
                                    case 6:
                                    case 7:
                                        switch(viewNum){
                                            case 1:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer3());
                                                break;

                                            case 2:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer1());
                                                break;

                                            case 3:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getWrongAnswer2());
                                                break;

                                            case 4:
                                                ((Button)view).setText(listQuestionsAll.get(gameRound-1).getCorrectAnswer());

                                                break;
                                        }
                                        break;
                                }
                            }

                            //Reset Buttons Color
                            if(viewNum != 0){
                                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E99C03")));
                            }
                            //Anim Change
                            playAnim(view,1,viewNum);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
    }

    @Override
    public void startQuestionTimer() {
        timer = new CountDownTimer(timerTotalSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished < timerAnswerSeconds){
                    binding.tvCounter.setText(String.valueOf(millisUntilFinished/1000));
                }
            }
            @Override
            public void onFinish() {
                changeQuestion();
            }
        };
        timer.start();
        isTimerRunning = true;
    }

    @Override
    public void stopQuestionTimer() {
        if(isTimerRunning){
            timer.cancel();
            isTimerRunning = false;
        }
    }

    @Override
    public void startAudioTimer() {
        mediaCounter = MediaPlayer.create(this, R.raw.countdown_10sec);
        if(!mediaCounter.isPlaying()){
            mediaCounter.start();
        }
    }

    @Override
    public void stopAudioTimer() {
        if(mediaCounter.isPlaying()){
            mediaCounter.stop();
        }
    }

    @Override
    public int randomIndex(int min, int max) {
        return (int) ( Math.random() * ( max - min ) ) + min;
    }

    @Override
    public void enableButtons() {
        binding.buttonOption1.setEnabled(true);
        binding.buttonOption2.setEnabled(true);
        binding.buttonOption3.setEnabled(true);
        binding.buttonOption4.setEnabled(true);
    }

    @Override
    public void disableButtons() {
        binding.buttonOption1.setEnabled(false);
        binding.buttonOption2.setEnabled(false);
        binding.buttonOption3.setEnabled(false);
        binding.buttonOption4.setEnabled(false);
    }

    @Override
    public void saveQuestionsInSharedPreferences(SharedPreferences shared, SharedPreferences.Editor editor, final String SHARED_GAME_FILE_NAME){
        shared = getSharedPreferences(SHARED_GAME_FILE_NAME, MODE_PRIVATE);
        editor = shared.edit();
        for(int i=0; i<listQuestionsAll.size(); i++){
            int index = listQuestionsAll.get(i).getQuestionIndex();
            editor.putInt(SHARED_KEY_Question+(i+1), index);
            editor.apply();
        }
    }

    @Override
    public int[] getQuestionsFromSharedPreferences(SharedPreferences shared, final String SHARED_GAME_FILE_NAME){
        shared = getSharedPreferences(SHARED_GAME_FILE_NAME, MODE_PRIVATE);
        int[] list = new int[10];

        for(int i=0; i<10; i++){
            list[i] = shared.getInt(SHARED_KEY_Question+(i+1), -1);
        }

        return list;
    }

    @Override
    public void saveAllInSharedPreferences(int gameNumber, int sharedRound){
        int sharedCheckFile = gameNumber - 3 * sharedRound;
        switch(sharedCheckFile){
            case 1:
                saveQuestionsInSharedPreferences(shared, editor, SHARED_GAME1_FILE_NAME);
                break;
            case 2:
                saveQuestionsInSharedPreferences(shared, editor, SHARED_GAME2_FILE_NAME);
                break;
            case 3:
                saveQuestionsInSharedPreferences(shared, editor, SHARED_GAME3_FILE_NAME);
                break;
        }
    }
}
