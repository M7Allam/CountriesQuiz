package com.example.countriesquiz.model;

public class Question {

    private String Question;
    private String correctAnswer;
    private String wrongAnswer1;
    private String wrongAnswer2;
    private String wrongAnswer3;
    private int questionIndex;
    private int wrongIndex1;
    private int wrongIndex2;
    private int wrongIndex3;

    public Question(String question, String correctAnswer, String wrongAnswer1, String wrongAnswer2, String wrongAnswer3, int questionIndex, int wrongIndex1, int wrongIndex2, int wrongIndex3) {
        Question = question;
        this.correctAnswer = correctAnswer;
        this.wrongAnswer1 = wrongAnswer1;
        this.wrongAnswer2 = wrongAnswer2;
        this.wrongAnswer3 = wrongAnswer3;
        this.questionIndex = questionIndex;
        this.wrongIndex1 = wrongIndex1;
        this.wrongIndex2 = wrongIndex2;
        this.wrongIndex3 = wrongIndex3;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getWrongAnswer1() {
        return wrongAnswer1;
    }

    public void setWrongAnswer1(String wrongAnswer1e) {
        this.wrongAnswer1 = wrongAnswer1e;
    }

    public String getWrongAnswer2() {
        return wrongAnswer2;
    }

    public void setWrongAnswer2(String wrongAnswer2) {
        this.wrongAnswer2 = wrongAnswer2;
    }

    public String getWrongAnswer3() {
        return wrongAnswer3;
    }

    public void setWrongAnswer3(String wrongAnswer3) {
        this.wrongAnswer3 = wrongAnswer3;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public int getWrongIndex1() {
        return wrongIndex1;
    }

    public void setWrongIndex1(int wrongIndex1) {
        this.wrongIndex1 = wrongIndex1;
    }

    public int getWrongIndex2() {
        return wrongIndex2;
    }

    public void setWrongIndex2(int wrongIndex2) {
        this.wrongIndex2 = wrongIndex2;
    }

    public int getWrongIndex3() {
        return wrongIndex3;
    }

    public void setWrongIndex3(int wrongIndex3) {
        this.wrongIndex3 = wrongIndex3;
    }
}
