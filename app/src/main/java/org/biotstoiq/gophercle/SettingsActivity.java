package org.biotstoiq.gophercle;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    Button saveBtn;
    TextView txtSzeValTV;
    NumberPicker txtSzeNP;
    LinearLayout textSizeLL;
    LinearLayout srchUrlSvdValLL;
    TextView srchUrlSvdValTV;
    EditText srchUrlValET;
    Switch darkThemeSwtch;

    SharedPreferences shrdPrfrncs;

    int txtSizInt;
    String srchUrlStr;
    boolean drkThmBool;

    AlertDialog alrtDlg;
    AlertDialog.Builder dlgBldr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveBtn = findViewById(R.id.saveBtn);
        textSizeLL = findViewById(R.id.txtSzeLL);
        txtSzeValTV = findViewById(R.id.txtSzeVal);
        srchUrlSvdValTV = findViewById(R.id.srchUrlSvdValTV);
        srchUrlSvdValLL = findViewById(R.id.srchUrlSvdValLL);
        darkThemeSwtch = findViewById(R.id.drkThmSwtch);

        shrdPrfrncs = PreferenceManager.getDefaultSharedPreferences(this);

        txtSizInt = shrdPrfrncs.getInt("txt_siz", 14);
        srchUrlStr = shrdPrfrncs.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs");
        drkThmBool = shrdPrfrncs.getBoolean("drk_thm", false);
        SharedPreferences.Editor editor = shrdPrfrncs.edit();

        txtSzeValTV.setText(String.valueOf(txtSizInt));
        srchUrlSvdValTV.setText(srchUrlStr);
        darkThemeSwtch.setChecked(drkThmBool);

        saveBtn.setOnClickListener(v -> {
            boolean thmchngd = darkThemeSwtch.isChecked()
                    != shrdPrfrncs.getBoolean("drk_thm", false);
            editor.putInt("txt_siz", txtSizInt);
            editor.putString("srch_url", srchUrlStr);
            editor.putBoolean("drk_thm", darkThemeSwtch.isChecked());
            editor.apply();
            if(thmchngd) {
                this.finishAffinity();
            }
        });

        textSizeLL.setOnClickListener(view -> {
            dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.txt_size);
            dlgBldr.setView(R.layout.activity_numberpicker);
            dlgBldr.setNegativeButton(R.string.cncl,
                    (dialogInterface, i) -> dialogInterface.cancel());
            dlgBldr.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                txtSizInt = txtSzeNP.getValue();
                txtSzeValTV.setText(String.valueOf(txtSizInt));
            });
            alrtDlg = dlgBldr.create();
            alrtDlg.show();

            txtSzeNP = alrtDlg.findViewById(R.id.txtSzeNP);
            txtSzeNP.setMaxValue(20);
            txtSzeNP.setMinValue(8);
            txtSzeNP.setValue(txtSizInt);
        });

        srchUrlSvdValLL.setOnClickListener(view -> {
            dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.url);
            dlgBldr.setView(R.layout.alert_dialog_url);
            dlgBldr.setNegativeButton(R.string.cncl,
                    (dialogInterface, i) -> dialogInterface.cancel());
            dlgBldr.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                srchUrlStr = srchUrlValET.getText().toString();
                srchUrlSvdValTV.setText(srchUrlStr);
            });
            alrtDlg = dlgBldr.create();
            alrtDlg.show();

            srchUrlValET = alrtDlg.findViewById(R.id.adrUrlValET);
        });
    }
}
