package com.fyp.variato.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

;

public class Progress
{
    public static AlertDialog mDialog;

    public static void CloseProgress() {
        if (Progress.mDialog != null) {
            Progress.mDialog.dismiss();
        }
    }

    public static void ShowProgress(final Context context, final String message) {
        final ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(0);
        mDialog.setMessage(message);
        mDialog.setCanceledOnTouchOutside(false);

        (Progress.mDialog = (AlertDialog) mDialog).show();

    }
}
