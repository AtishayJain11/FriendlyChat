package project.beryl.com.newfirebaseapplication.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import project.beryl.com.newfirebaseapplication.R;

/**
 * reated by punamchand 12/12/2018alam on 10/17/2017.
 */

public class AppUtils {
    private Dialog  mDialog;;
    private static AppUtils appUtils;

    public static AppUtils getInstance(){
        if (appUtils==null){
            return new AppUtils();
        }else {
           return appUtils;
        }
    }

    /**
     * Show Snackbar
     *
     * @param message
     * @param view
     */

    public void showSnackBar(String message, View view){
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.RED);
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();

    }

    public void showProgessDialog(Activity mActivity){
        if(mDialog == null){
            mDialog = new Dialog(mActivity, android.R.style.Theme_Translucent);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setContentView(R.layout.layout_custom_progress_dialog);
            mDialog.setCancelable(false);
            mDialog.show();
        }/*else {
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setContentView(R.layout.custom_progress_dialog);
            mDialog.setCancelable(true);
            mDialog.show();
        }*/
    }

    public void hideProgessDialog(){
        if(mDialog != null) {
            mDialog.dismiss();
        }
    }


    public boolean isNetworkAvailable(Context context){

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    public  String getFormattedDateFromTimestamp(long timestampInMilliSeconds)
    {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate=new SimpleDateFormat("EEE, d MMM yyyy HH:mm").format(date);
        return formattedDate;

    }
}
