package com.miczon.photoeditor.eventListeners;

import android.app.AlertDialog;

public interface DialogClickListener {
    void onButtonClick(String status, String message, String data, AlertDialog alertDialog);

}
