package net.w2s.driverapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.w2s.driverapp.MyWidgets.CircleImageView;
import net.w2s.driverapp.Utilities.ConstantKeys;
import net.w2s.driverapp.Utilities.Utility;

/**
 * Created by Android-2 on 2/26/2016.
 */
public class SetMessageForStudent extends AppCompatActivity implements View.OnClickListener {

    Context ctxSetMessage;
    EditText message1, message2, message3, message4, message5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setnotification_new);

        String language = Utility.getSharedPreferences(SetMessageForStudent.this, ConstantKeys.Setting_Language);
        if (language.equals("1")) {
            ViewCompat.setLayoutDirection(findViewById(R.id.message_root_view), ViewCompat.LAYOUT_DIRECTION_RTL);
        } else {
            ViewCompat.setLayoutDirection(findViewById(R.id.message_root_view), ViewCompat.LAYOUT_DIRECTION_LTR);
        }

        ctxSetMessage = this;
        init();
        ((TextView) findViewById(R.id.txtViewAdd)).setOnClickListener(this);
        ((TextView) findViewById(R.id.txtViewEdit)).setOnClickListener(this);
        ((TextView) findViewById(R.id.txtViewDelete)).setOnClickListener(this);
        ((Button) findViewById(R.id.btnSave)).setOnClickListener(this);

        message1 = (EditText) findViewById(R.id.message1);
        message2 = (EditText) findViewById(R.id.message2);
        message3 = (EditText) findViewById(R.id.message3);
        message4 = (EditText) findViewById(R.id.message4);
        message5 = (EditText) findViewById(R.id.message5);

        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE1))) {
            message1.setText(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE1));
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE2))) {
            message2.setText(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE2));
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE3))) {
            message3.setText(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE3));
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE4))) {
            message4.setText(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE4));
        }
        if (!Utility.isStringNullOrBlank(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE5))) {
            message5.setText(Utility.getSharedPreferences(ctxSetMessage, ConstantKeys.MESSAGE5));
        }
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        } catch (Exception ignored) {
        }
        ((TextView) findViewById(R.id.title)).setText(getString(R.string.set_noti_title));
        ((CircleImageView) findViewById(R.id.toggle_btn)).setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.txtViewAdd:
                break;
            case R.id.txtViewEdit:
                break;
            case R.id.txtViewDelete:
                break;
            case R.id.btnSave:
                if (message1.getText().toString() != null) {
                    Utility.setSharedPreference(ctxSetMessage, ConstantKeys.MESSAGE1, message1.getText().toString());
                }
                if (message2.getText().toString() != null) {
                    Utility.setSharedPreference(ctxSetMessage, ConstantKeys.MESSAGE2, message2.getText().toString());
                }
                if (message3.getText().toString() != null) {
                    Utility.setSharedPreference(ctxSetMessage, ConstantKeys.MESSAGE3, message3.getText().toString());
                }
                if (message4.getText().toString() != null) {
                    Utility.setSharedPreference(ctxSetMessage, ConstantKeys.MESSAGE4, message4.getText().toString());
                }
                if (message5.getText().toString() != null) {
                    Utility.setSharedPreference(ctxSetMessage, ConstantKeys.MESSAGE5, message5.getText().toString());
                }


                startActivity(new Intent( getApplicationContext() , MainActivityNew.class));
                finish();
                break;
        }
    }
}
