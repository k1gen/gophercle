package org.biotstoiq.gophercle

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.preference.PreferenceManager

class SettingsActivity : Activity() {
    var saveBtn: Button? = null
    var txtSzeValTV: TextView? = null
    var txtSzeNP: NumberPicker? = null
    var textSizeLL: LinearLayout? = null
    var srchUrlSvdValLL: LinearLayout? = null
    var srchUrlSvdValTV: TextView? = null
    var srchUrlValET: EditText? = null
    var shrdPrfrncs: SharedPreferences? = null
    var txtSizInt = 0
    var srchUrlStr: String? = null
    var alrtDlg: AlertDialog? = null
    var dlgBldr: AlertDialog.Builder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        saveBtn = findViewById(R.id.saveBtn)
        textSizeLL = findViewById(R.id.txtSzeLL)
        txtSzeValTV = findViewById(R.id.txtSzeVal)
        srchUrlSvdValTV = findViewById(R.id.srchUrlSvdValTV)
        srchUrlSvdValLL = findViewById(R.id.srchUrlSvdValLL)
        shrdPrfrncs = PreferenceManager.getDefaultSharedPreferences(this)
        txtSizInt = shrdPrfrncs!!.getInt("txt_siz", 14)
        srchUrlStr = shrdPrfrncs!!.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs")
        val editor = shrdPrfrncs!!.edit()
        txtSzeValTV?.text = txtSizInt.toString()
        srchUrlSvdValTV?.text = srchUrlStr
        saveBtn?.setOnClickListener { v: View? ->
            editor.putInt("txt_siz", txtSizInt)
            editor.putString("srch_url", srchUrlStr)
            editor.apply()
        }
        textSizeLL?.setOnClickListener { view: View? ->
            dlgBldr = AlertDialog.Builder(this)
            dlgBldr!!.setTitle(R.string.txt_size)
            dlgBldr!!.setView(R.layout.activity_numberpicker)
            dlgBldr!!.setNegativeButton(R.string.cncl
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
            dlgBldr!!.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface?, i: Int ->
                txtSizInt = txtSzeNP!!.value
                txtSzeValTV?.text = txtSizInt.toString()
            }
            alrtDlg = dlgBldr!!.create()
            alrtDlg?.show()
            txtSzeNP = alrtDlg?.findViewById(R.id.txtSzeNP)
            txtSzeNP?.maxValue = 20
            txtSzeNP?.minValue = 8
            txtSzeNP?.value = txtSizInt
        }
        srchUrlSvdValLL?.setOnClickListener { view: View? ->
            dlgBldr = AlertDialog.Builder(this)
            dlgBldr!!.setTitle(R.string.url)
            dlgBldr!!.setView(R.layout.alert_dialog_url)
            dlgBldr!!.setNegativeButton(R.string.cncl
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
            dlgBldr!!.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface?, i: Int ->
                srchUrlStr = srchUrlValET!!.text.toString()
                srchUrlSvdValTV?.text = srchUrlStr
            }
            alrtDlg = dlgBldr!!.create()
            alrtDlg?.show()
            srchUrlValET = alrtDlg?.findViewById(R.id.adrUrlValET)
        }
    }
}
