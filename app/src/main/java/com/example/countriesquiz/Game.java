package com.example.countriesquiz;

import android.content.SharedPreferences;
import android.view.View;

import com.example.countriesquiz.model.City;
import com.example.countriesquiz.model.Country;
import com.example.countriesquiz.model.Question;

import java.util.ArrayList;

public interface Game {

    public ArrayList<Country> getCountryDataFromDB(String level);

    public ArrayList<City> getCityDataFromDB();

    public ArrayList<Question> chooseQuestions(ArrayList<Country> listEasy, ArrayList<Country> listMedium, ArrayList<Country> listHard, ArrayList<Country> listVeryHard, ArrayList<Country> listExpert, ArrayList<City> listCities, int[] listPrevious1, int[] listPrevious2, int[] listPrevious3);

    public  void getAllQuestions();

    public int randomAnswers();

    public void setQuestion();

    public int getAnswer(View view);

    public void checkAnswer(int selectedOption, int correctOption, View view);

    public void calculatePoints(boolean isCorrectAnswer, int round);

    public void changeQuestion();

    public void playAnim(final View view, final int value, final int viewNum);

    public void startQuestionTimer();

    public void stopQuestionTimer();

    public void startAudioTimer();

    public void stopAudioTimer();

    public int randomIndex(int min, int max);

    public void enableButtons();

    public void disableButtons();

    public void saveQuestionsInSharedPreferences(SharedPreferences shared, SharedPreferences.Editor editor, final String SHARED_GAME_FILE_NAME);

    public int[] getQuestionsFromSharedPreferences(SharedPreferences shared, final String SHARED_GAME_FILE_NAME);

    public void saveAllInSharedPreferences(int gameNumber, int sharedRound);
}
