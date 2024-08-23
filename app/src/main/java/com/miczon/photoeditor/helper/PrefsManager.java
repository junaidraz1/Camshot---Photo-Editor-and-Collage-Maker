package com.miczon.photoeditor.helper;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String SELECTED_LANGUAGE = "SelectedLanguage";
    private static final String LANG_POS = "LangPos";
    private static final String PRIVACY_BIT = "PrivacyBit";
    private static final String BOARDING_BIT = "BoardinBit";
    private static final String PREMIUM_KEY = "PremiumKey";

    /**
     * Constructor
     *
     * @param context: from where it is called
     */
    public PrefsManager(Context context) {
        this.context = context;
    }

    /**
     * Method to save selected language
     *
     * @param selectedLanguage: code of language
     */
    public void setSelectedLanguage(String selectedLanguage) {
        editor = context.getSharedPreferences(SELECTED_LANGUAGE, MODE_PRIVATE).edit();
        editor.putString(SELECTED_LANGUAGE, selectedLanguage);
        editor.apply();
    }

    /**
     * Method to retrieve saved language
     *
     * @return: language code
     */
    public String getSelectedLanguage() {
        sharedPreferences = context.getSharedPreferences(SELECTED_LANGUAGE, MODE_PRIVATE);
        return sharedPreferences.getString(SELECTED_LANGUAGE, "");
    }

    /**
     * Method to save last selected language position
     *
     * @param langPos: position of saved language
     */
    public void setLangPos(int langPos) {
        editor = context.getSharedPreferences(LANG_POS, MODE_PRIVATE).edit();
        editor.putInt(LANG_POS, langPos);
        editor.apply();
    }

    /**
     * Method to get saved language position
     *
     * @return: position
     */
    public int getLangPos() {
        sharedPreferences = context.getSharedPreferences(LANG_POS, MODE_PRIVATE);
        return sharedPreferences.getInt(LANG_POS, 0);
    }

    /**
     * Method to set state if premium sub is bought or not
     * @param isPremium: flag to save state
     */
    public void setIsPremium(boolean isPremium) {
        editor = context.getSharedPreferences(PREMIUM_KEY, MODE_PRIVATE).edit();
        editor.putBoolean(PREMIUM_KEY, isPremium);
        editor.apply();
    }

    /**
     * Method to get premium sub status i.e. bought or not
     * @return: flag that retains the purchased status of subscription
     */
    public boolean getIsPremium() {
        sharedPreferences = context.getSharedPreferences(PREMIUM_KEY, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREMIUM_KEY, false);
    }


    /**
     * Method to control agreement dialog's visibility
     * @param privacyBit: flag to check if it was shown at first or not
     */
    public void setPrivacyBit(boolean privacyBit) {
        editor = context.getSharedPreferences(PRIVACY_BIT, MODE_PRIVATE).edit();
        editor.putBoolean(PRIVACY_BIT, privacyBit);
        editor.apply();
    }

    /**
     * Method to get state of agreement dialog i.e. if it was shown at first or not
     * @return: flag that contains it's state
     */
    public boolean getPrivacyBit() {
        sharedPreferences = context.getSharedPreferences(PRIVACY_BIT, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PRIVACY_BIT, false);
    }

    /**
     * Method to control on boarding screen's visibility
     * @param boardingBit: flag to check if it was shown at first or not
     */
    public void setBoardingBit(boolean boardingBit) {
        editor = context.getSharedPreferences(BOARDING_BIT, MODE_PRIVATE).edit();
        editor.putBoolean(BOARDING_BIT, boardingBit);
        editor.apply();
    }

    /**
     * Method to get state of on boarding screen's i.e. if it was shown at first or not
     * @return: flag that contains it's state
     */
    public boolean getBoardingBit() {
        sharedPreferences = context.getSharedPreferences(BOARDING_BIT, MODE_PRIVATE);
        return sharedPreferences.getBoolean(BOARDING_BIT, false);
    }


}
