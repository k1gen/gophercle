package org.biotstoiq.gophercle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    static BufferedReader bfrdRdr;
    static PrintWriter prntWrtr;
    static URL url;
    static Socket sckt;
    boolean connOk = false;

    String adrUrlStr;
    String tempLine;
    StringTokenizer lineTknzr;
    String fileName;
    String srchUrlStr;

    int txtSizInt;

    ArrayList<String> lnArLst;
    ArrayList<String> hstryArLst;
    ArrayList<String> itmTypArLst;

    boolean backPressed;
    boolean clickDriven = false;
    boolean fileDnlded;
    boolean onBkmrkPg = true;

    EditText adrUrlValET;
    Button stngsBtn;
    Button bkmrkBtn;
    Button rfrshBtn;
    Button adrBtn;
    Button bkBtn;
    Button shrBtn;
    View mainLL;
    View cntntLL;
    TextView srchUrlValTV;
    EditText queryET;
    TextView lnTV;
    Button itmBtn;
    Button adBkmrkBtn;

    String dnldDir;

    AlertDialog.Builder dlgBldr;
    AlertDialog alrtDlg;

    SharedPreferences shrdPrfrncs;
    SharedPreferences.Editor prfEdtr;

    ViewGroup.LayoutParams lprms;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lnArLst = new ArrayList<>();
        hstryArLst = new ArrayList<>();
        itmTypArLst = new ArrayList<>();

        initResources();
        showBkmrks();

        dnldDir = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));

        bkmrkBtn.setOnClickListener(view -> {
            onBkmrkPg = true;
            showBkmrks();
        });

        stngsBtn.setOnClickListener(view -> {
            dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setView(R.layout.alert_dialog_options);
            dlgBldr.setNegativeButton(R.string.cncl, ((dialogInterface, i) -> dialogInterface.cancel()));
            dlgBldr.setNeutralButton(R.string.stngs, ((dialogInterface, i) -> openSettings()));
            dlgBldr.setPositiveButton(R.string.srch, ((dialogInterface, i1) -> initiateSearch()));

            alrtDlg = dlgBldr.create();
            alrtDlg.show();

            srchUrlValTV = alrtDlg.findViewById(R.id.srchUrlValTV);
            srchUrlValTV.setText(srchUrlStr);
            adBkmrkBtn = alrtDlg.findViewById(R.id.adBkmrkBtn);
            shrBtn = alrtDlg.findViewById(R.id.shrBtn);

            if (onBkmrkPg) {
                adBkmrkBtn.setVisibility(View.GONE);
            } else {
                adBkmrkBtn.setOnClickListener(view1 -> {
                    adBkmrk(url.getUrl());
                    alrtDlg.dismiss();
                });
            }

            shrBtn.setOnClickListener(view1 -> {
                if (url != null) {
                    Intent shrIntnt = new Intent(android.content.Intent.ACTION_SEND);
                    shrIntnt.setType("text/plain");
                    shrIntnt.putExtra(Intent.EXTRA_TEXT, url.getUrl());
                    startActivity(Intent.createChooser(shrIntnt, getString(R.string.shr)));
                }
            });
            queryET = alrtDlg.findViewById(R.id.qryET);
        });

        adrBtn.setOnClickListener(view -> {
            dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setView(R.layout.alert_dialog_url);
            dlgBldr.setPositiveButton(R.string.go, (dialogInterface, i) -> {
                connOk = false;
                clickDriven = false;
                adrUrlStr = adrUrlValET.getText().toString();
                // Create a new thread to execute the ConnectAsync task
                Thread thread = new Thread(new ConnectAsync());
                thread.start();
            });
            dlgBldr.setNegativeButton(R.string.cncl, (dialogInterface, i) -> dialogInterface.cancel());
            alrtDlg = dlgBldr.create();
            alrtDlg.show();

            adrUrlValET = alrtDlg.findViewById(R.id.adrUrlValET);
            if (url != null && url.isUrlOkay()) {
                adrUrlValET.setText(url.getUrl());
            } else {
                adrUrlValET.setText(adrUrlStr);
            }
        });

        rfrshBtn.setOnClickListener(view -> {
            if (!adrUrlStr.equals("")) {
                clickDriven = false;
                // Create a new thread to execute the ConnectAsync task
                Thread thread = new Thread(new ConnectAsync());
                thread.start();
            }
        });

        bkBtn.setOnClickListener(view -> {
            int listSize = hstryArLst.size();
            if (listSize > 1) {
                clickDriven = true;
                backPressed = true;
                if (onBkmrkPg) {
                    ++listSize;
                } else {
                    hstryArLst.remove(listSize - 1);
                    itmTypArLst.remove(listSize - 1);
                }
                // Create a new thread to execute the ConnectAsync task
                Thread thread = new Thread(new ConnectAsync());
                thread.start();
            } else if (listSize == 1 && onBkmrkPg) {
                // Create a new thread to execute the ConnectAsync task
                Thread thread = new Thread(new ConnectAsync());
                thread.start();
            }
        });
    }

    void initResources() {
        shrdPrfrncs = PreferenceManager.getDefaultSharedPreferences(this);
        prfEdtr = shrdPrfrncs.edit();

        mainLL = findViewById(R.id.mainLinearLayout);
        cntntLL = findViewById(R.id.contentLinearLayout);
        stngsBtn = findViewById(R.id.stngsBtn);
        bkmrkBtn = findViewById(R.id.bkmrksBtn);
        adrBtn = findViewById(R.id.adrBtn);
        rfrshBtn = findViewById(R.id.rfrshBtn);
        bkBtn = findViewById(R.id.bkBtn);
        lprms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        txtSizInt = shrdPrfrncs.getInt("txt_siz", 14);
        srchUrlStr = shrdPrfrncs.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs");
        setTheme();
    }

    void showBkmrks() {
        ((LinearLayout) cntntLL).removeAllViews();
        String bkmrksStr = shrdPrfrncs.getString("bkmrks", "");
        String[] bkmrksSplt = bkmrksStr.split(",");
        for (int j = 0; j < bkmrksSplt.length; j++) {
            String url = bkmrksSplt[j];
            if(!url.equals("")) {
                Button bkmrkBtn = new Button(this);
                bkmrkBtn.setLayoutParams(lprms);
                bkmrkBtn.setTextAlignment(Button.TEXT_ALIGNMENT_TEXT_START);
                bkmrkBtn.setText(url);
                int jf = j;
                bkmrkBtn.setOnClickListener(view -> {
                    connOk = false;
                    clickDriven = false;
                    dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setPositiveButton(R.string.go, (dialogInterface, i) -> callInitConnection(url, '\0'));
                    dlgBldr.setNegativeButton(R.string.rmv, (dialogInterface, i) -> rmvBkmrk(jf));
                    alrtDlg = dlgBldr.create();
                    alrtDlg.show();
                });
                ((LinearLayout) cntntLL).addView(bkmrkBtn);
            }
        }
    }

    void rmvBkmrk(int pos) {
        String bkmrksStr = shrdPrfrncs.getString("bkmrks", "");
        String[] bkmrksSplt = bkmrksStr.split(",");
        bkmrksStr = "";
        int i = 0;
        for (String bkmrkSpltStr : bkmrksSplt) {
            if(i != pos) {
                bkmrksStr = bkmrksStr.concat(bkmrkSpltStr).concat(",");
            }
            i++;
        }

        prfEdtr.putString("bkmrks",bkmrksStr);
        prfEdtr.apply();
        showBkmrks();
    }

    void adBkmrk(String url) {
        String bkmrksStr = shrdPrfrncs.getString("bkmrks", "");
        String[] bkmrksSplt = bkmrksStr.split(",");
        for (String bkmrkUrlStr : bkmrksSplt) {
            if(url.equals(bkmrkUrlStr)) {
                return;
            }
        }

        prfEdtr.putString("bkmrks", bkmrksStr.concat(",").concat(url));
        prfEdtr.apply();

        Toast.makeText(this, R.string.bkmrk_added, Toast.LENGTH_SHORT).show();
    }

    void setTheme() {
        mainLL.setBackgroundColor(0xff000000);
        stngsBtn.setTextColor(0xffffffff);
        bkmrkBtn.setTextColor(0xffffffff);
        adrBtn.setTextColor(0xffffffff);
        rfrshBtn.setTextColor(0xffffffff);
        bkBtn.setTextColor(0xffffffff);
        getWindow().setNavigationBarColor(0xff000000);
    }

    void openSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void callInitConnection(String inputURL, char itmTyp) {
        closeComponents();
        onBkmrkPg = false;
        url = new URL(inputURL);
        if (clickDriven) {
            url.setUrlItemType(itmTyp);
        }
        if (url.isUrlOkay()) {
            displayMessage('l', inputURL);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.shutdown(); // Shutdown the executor when no longer needed
        } else {
            displayMessage('e', "");
        }
    }

    void closeComponents() {
        if( prntWrtr != null) {
            prntWrtr.close();
            prntWrtr = null;
        }
        if(bfrdRdr != null) {
            try {
                bfrdRdr.close();
                bfrdRdr = null;
            } catch (IOException ignore) {}
        }
        if(sckt != null) {
            try {
                sckt.close();
                sckt = null;
            } catch (IOException ignore) {}
        }
    }

    void displayTxtFile() {
        String txt = "";
        for (int i = 0; i < lnArLst.size(); i++) {
            txt = txt.concat(lnArLst.get(i)).concat("\n");
        }
        lnTV = new TextView(MainActivity.this);
        setTextViewProps(lnTV);
        lnTV.setTextIsSelectable(true);
        lnTV.setText(txt);
        ((LinearLayout) cntntLL).addView(lnTV);
    }

    void addLine() {
        lnTV = new TextView(this);
        setTextViewProps(lnTV);
        lnTV.setText(tempLine);
        ((LinearLayout) cntntLL).addView(lnTV);
    }

    void displayContent() {
        System.out.println(url.getUrlItemType());
        if (connOk) {
            ((LinearLayout) cntntLL).removeAllViews();
            lnTV = new TextView(this);
            switch (url.getUrlItemType()) {
                case '0': {
                    displayTxtFile();
                    break;
                }
                case '1':
                case '7': {
                    for (int i = 0; i < lnArLst.size(); i++) {
                        tempLine = lnArLst.get(i);
                        if(tempLine.length() == 0) continue;
                        char itemType = tempLine.charAt(0);
                        boolean typePresent = true;
                        int resId = 0;
                        switch (itemType) {
                            case '0':
                                resId = R.string.ct_txt;
                                break;
                            case '1':
                                resId = R.string.ct_sub;
                                break;
                            case '2':
                                resId = R.string.ct_ccs;
                                break;
                            case '3':
                                resId = R.string.ct_err;
                                break;
                            case '4':
                                resId = R.string.ct_bhx;
                                break;
                            case '5':
                                resId = R.string.ct_dos;
                                break;
                            case '6':
                                resId = R.string.ct_uue;
                                break;
                            case '7':
                                resId = R.string.ct_srh;
                                break;
                            case '8':
                                resId = R.string.ct_tnt;
                                break;
                            case '9':
                                resId = R.string.ct_bin;
                                break;
                            case '+':
                                resId = R.string.ct_mir;
                                break;
                            case 'g':
                                resId = R.string.ct_gif;
                                break;
                            case 'i':
                                tempLine = tempLine.substring(1, tempLine.indexOf('\t'));
                                break;
                            case 'I':
                                resId = R.string.ct_img;
                                break;
                            default:
                                typePresent = false;
                                break;
                        }
                        if (itemType != 'i' && typePresent) {
                            itmBtn = new Button(this);
                            itmBtn.setLayoutParams(lprms);
                            itmBtn.setTextAlignment(Button.TEXT_ALIGNMENT_TEXT_START);
                            lineTknzr = new StringTokenizer(tempLine, "\t");
                            tempLine = getString(resId).concat(" ").concat(lineTknzr.nextToken().substring(1));
                            itmBtn.setText(tempLine);
                            int finalI = i;
                            itmBtn.setOnClickListener(view -> {
                                clickDriven = true;
                                String[] lineSplit = lnArLst.get(finalI).split("\t");
                                String tempUrl = lineSplit[2].concat(":").concat(lineSplit[3])
                                        .concat(lineSplit[1]);
                                char itmTyp = lineSplit[0].charAt(0);
                                setUrlParts(lineSplit, itemType);
                                switch (itmTyp) {
                                    case '0':
                                    case '1':
                                        callInitConnection(tempUrl, itmTyp);
                                        break;
                                    case '7':
                                        dlgBldr = new AlertDialog.Builder(this);
                                        dlgBldr.setView(R.layout.alert_dialog_options);
                                        dlgBldr.setNegativeButton(R.string.cncl,
                                                ((dialogInterface, i1) -> dialogInterface.cancel()));
                                        dlgBldr.setPositiveButton(R.string.srch,
                                                ((dialogInterface, i1) -> initiateSearch()));
                                        alrtDlg = dlgBldr.create();
                                        alrtDlg.show();

                                        srchUrlValTV = alrtDlg.findViewById(R.id.srchUrlValTV);
                                        adBkmrkBtn = alrtDlg.findViewById(R.id.adBkmrkBtn);
                                        adBkmrkBtn.setVisibility(View.GONE);
                                        queryET = alrtDlg.findViewById(R.id.qryET);
                                        srchUrlValTV.setText(tempUrl);
                                        break;
                                    default:
                                        dlgBldr = new AlertDialog.Builder(this);
                                        dlgBldr.setMessage(getString(R.string.fnm)
                                                .concat(" ")
                                                .concat(lineSplit[1]
                                                        .substring(lineSplit[1].lastIndexOf("/") + 1)));
                                        dlgBldr.setPositiveButton(R.string.dnld,
                                                ((dialogInterface, i1) -> callInitConnection(tempUrl, itmTyp)));
                                        dlgBldr.setNegativeButton(R.string.cncl,
                                                ((dialogInterface, i1) -> dialogInterface.cancel()));
                                        alrtDlg = dlgBldr.create();
                                        alrtDlg.show();
                                        break;
                                }
                            });
                            ((LinearLayout) cntntLL).addView(itmBtn);
                        } else {
                            addLine();
                        }
                    }
                    break;
                }
                default:
                    displayMessage('d', fileName);
                    break;
            }
        } else {
            displayMessage('e', "");
        }
    }

    void initiateSearch() {
        clickDriven = true;
        callInitConnection(srchUrlStr.concat("\t").concat(queryET.getText().toString()), '7');
    }

    void setUrlParts(String[] lineSplit, char itemType) {
        url.setUrlItemType(itemType);
        url.setUrlPath(lineSplit[1]);
        url.setUrlHost(lineSplit[2]);
        url.setUrlPort(Integer.parseInt(lineSplit[3]));
        url.makeURLfromParts();
    }

    void displayMessage(char ch, String param) {
        ((LinearLayout) cntntLL).removeAllViews();
        lnTV = new TextView(MainActivity.this);
        setTextViewProps(lnTV);
        String msg;
        switch (ch) {
            case 'd':
                if (fileDnlded) {
                    msg = getString(R.string.fnm).concat(" ")
                            .concat(param).concat(", ").concat(getString(R.string.lction))
                            .concat(" ").concat(dnldDir);
                } else {
                    msg = getString(R.string.dnld_failed);
                }
                break;
            case 'l':
                msg = getString(R.string.loading).concat(" ").concat(param);
                break;
            case 'e':
                switch (url.getErrorCode()) {
                    case 1:
                        msg = getString(R.string.invalid_protocol);
                        break;
                    case 2:
                        msg = getString(R.string.host_not_found);
                        break;
                    case 3:
                        msg = getString(R.string.invalid_port);
                        break;
                    case 4:
                        msg = getString(R.string.invalid_path);
                        break;
                    default:
                        msg = "";
                        break;
                }
                break;
            default:
                msg = "";
                break;
        }
        lnTV.setText(msg);
        ((LinearLayout) cntntLL).addView(lnTV);
    }

    void setTextViewProps(TextView tv) {
        tv.setTextColor(0xffffffff);
        tv.setTextSize(2, txtSizInt);
        tv.setTypeface(Typeface.MONOSPACE);
    }


    private class ConnectAsync implements Runnable {
        private static final int CONNECTION_TIMEOUT = 5000;

        @Override
        public void run() {
            Socket socket = null;

            try {
                lnArLst.clear();

                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(url.getUrlHost(), url.getUrlPort()), CONNECTION_TIMEOUT);
                } catch (IOException e) {
                    url.setErrorCode(2);
                    connOk = false;
                    e.printStackTrace();
                    return;
                }

                sckt = socket;
                prntWrtr = new PrintWriter(sckt.getOutputStream());
                prntWrtr.write(url.getUrlPath().concat(url.getUrlQuery()).concat("\r\n"));
                prntWrtr.flush();
                int read;
                String tl = "";
                bfrdRdr = new BufferedReader(new InputStreamReader(sckt.getInputStream()));

                byte[] fileSig = new byte[192];
                InputStream inputStream = sckt.getInputStream();
                if ((read = inputStream.read(fileSig)) != -1) {
                    int i = 0;
                    boolean isBinary = false, onceDone = false;
                    while (i < read) {
                        if (fileSig[i] == 10 || fileSig[i] == 13) {
                            if (!onceDone) {
                                onceDone = true;
                            } else {
                                onceDone = false;
                                i++;
                                continue;
                            }
                            lnArLst.add(tl);
                            tl = "";
                            i++;
                            continue;
                        }
                        if (fileSig[i] == 0 || fileSig[i] == 1 || fileSig[i] == 2 || fileSig[i] == 3) {
                            isBinary = true;
                            break;
                        }
                        tl = tl.concat(String.valueOf((char) fileSig[i]));
                        i++;
                    }
                    if (!clickDriven) {
                        if (isBinary) {
                            url.setUrlItemType('9');
                        } else {
                            i = 0;
                            int count = 0;
                            while (i < read) {
                                if (fileSig[i] == 10) {
                                    break;
                                } else if (fileSig[i] == 9) {
                                    count++;
                                }
                                i++;
                            }
                            if (count != 3) {
                                url.setUrlItemType('0');
                            } else {
                                url.setUrlItemType('1');
                            }
                        }
                    }
                } else {
                    url.setErrorCode(4);
                    connOk = false;
                    return;
                }
                switch (url.getUrlItemType()) {
                    case '0':
                    case '1':
                    case '7':
                        if ((tempLine = bfrdRdr.readLine()) != null) {
                            lnArLst.add(tl.concat(tempLine));
                        }
                        while ((tempLine = bfrdRdr.readLine()) != null) {
                            lnArLst.add(tempLine);
                        }
                        break;
                    default:
                        fileDnlded = false;
                        byte[] bytes = new byte[1024];
                        fileName = url.getUrlPath().substring(url.getUrlPath().lastIndexOf("/") + 1);
                        OutputStream fileStream = Files.newOutputStream(Paths.get(dnldDir.concat("/").concat(fileName)));
                        fileStream.write(fileSig, 0, read);
                        while ((read = inputStream.read(bytes)) != -1) {
                            fileStream.write(bytes, 0, read);
                        }
                        fileStream.flush();
                        fileStream.close();
                        fileDnlded = true;
                        break;
                }
                inputStream.close();
            } catch (IOException e) {
                url.setErrorCode(2);
                connOk = false;
                e.printStackTrace();
            } finally {
                // Close the socket in the finally block
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeComponents();

                connOk = true;
                url.makeURLfromParts();
                adrUrlStr = url.getUrl();
                if (!backPressed) {
                    hstryArLst.add(url.getUrl());
                    itmTypArLst.add(String.valueOf(url.getUrlItemType()));
                } else {
                    backPressed = false;
                }

                // Dismiss the dialog outside the runOnUiThread block
                alrtDlg.dismiss();

                runOnUiThread(() -> {
                    if (connOk) {
                        displayContent();
                    } else {
                        displayMessage('e', "");
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage(R.string.cnfrmtn)
                .setPositiveButton("Yes", (dialog, which) -> {
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("on_bkmrk_pg", onBkmrkPg);
        outState.putBoolean("conn_okay", connOk);
        outState.putStringArrayList("line_arraylist", lnArLst);
        outState.putStringArrayList("history_arraylist", hstryArLst);
        outState.putStringArrayList("itemtype_arraylist", itmTypArLst);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        onBkmrkPg = savedInstanceState.getBoolean("on_bkmrk_pg", true);
        connOk = savedInstanceState.getBoolean("conn_okay", false);
        lnArLst = savedInstanceState.getStringArrayList("line_arraylist");
        hstryArLst = savedInstanceState.getStringArrayList("history_arraylist");
        itmTypArLst = savedInstanceState.getStringArrayList("itemtype_arraylist");

        if(!onBkmrkPg) {
            if(url != null) {
                displayContent();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        adrUrlStr = "";
        txtSizInt = shrdPrfrncs.getInt("txt_siz", 14);
        srchUrlStr = shrdPrfrncs.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs");
        setTheme();
    }
}