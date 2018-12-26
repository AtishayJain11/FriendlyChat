package project.beryl.com.newfirebaseapplication.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

/**
 * reated by punamchand 12/12/2018alam on 10/18/2017.
 */

public class MyTextWatcher implements TextWatcher {

    private View view;

    public MyTextWatcher(View view) {
        this.view = view;
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

   /* public void afterTextChanged(Editable editable) {
        switch (view.getId()) {
            case R.id.et_usermail:
                validateName();
                break;
            case R.id.input_email:
                validateEmail();
                break;
            case R.id.input_password:
                validatePassword();
                break;
        }
    }*/
}
