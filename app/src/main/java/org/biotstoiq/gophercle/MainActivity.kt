package org.biotstoiq.gophercle

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.util.StringTokenizer
import java.util.concurrent.Executors

private const val CONNECTION_TIMEOUT = 5000


class MainActivity : Activity() {
    companion object {
        var bfrdRdr: BufferedReader? = null
        var prntWrtr: PrintWriter? = null
        var url: URL? = null
        var sckt: Socket? = null
    }

    var connOk = false
    var adrUrlStr: String? = null
    var tempLine: String? = null
    var lineTknzr: StringTokenizer? = null
    var fileName: String? = null
    var srchUrlStr: String? = null
    var txtSizInt = 0
    var lnArLst: ArrayList<String?>? = null
    var hstryArLst: ArrayList<String?>? = null
    var itmTypArLst: ArrayList<String>? = null
    var backPressed = false
    var clickDriven = false
    var fileDnlded = false
    var onBkmrkPg = true
    var adrUrlValET: EditText? = null
    var stngsBtn: Button? = null
    var bkmrkBtn: Button? = null
    var rfrshBtn: Button? = null
    var adrBtn: Button? = null
    var bkBtn: Button? = null
    var shrBtn: Button? = null
    var mainLL: View? = null
    var cntntLL: View? = null
    var srchUrlValTV: TextView? = null
    var queryET: EditText? = null
    var lnTV: TextView? = null
    var itmBtn: Button? = null
    var adBkmrkBtn: Button? = null
    var dnldDir: String? = null
    var dlgBldr: AlertDialog.Builder? = null
    var alrtDlg: AlertDialog? = null
    var shrdPrfrncs: SharedPreferences? = null
    var prfEdtr: SharedPreferences.Editor? = null
    var lprms: ViewGroup.LayoutParams? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lnArLst = ArrayList()
        hstryArLst = ArrayList()
        itmTypArLst = ArrayList()
        initResources()
        showBkmrks()
        adrUrlValET = alrtDlg?.findViewById(R.id.adrUrlValET)
        dnldDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()
        bkmrkBtn!!.setOnClickListener { view: View? ->
            onBkmrkPg = true
            showBkmrks()
        }
        stngsBtn!!.setOnClickListener { view: View? ->
            dlgBldr = AlertDialog.Builder(this)
            dlgBldr!!.setView(R.layout.alert_dialog_options)
            dlgBldr!!.setNegativeButton(R.string.cncl) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
            dlgBldr!!.setNeutralButton(R.string.stngs) { dialogInterface: DialogInterface?, i: Int -> openSettings() }
            dlgBldr!!.setPositiveButton(R.string.srch) { dialogInterface: DialogInterface?, i1: Int -> initiateSearch() }
            alrtDlg = dlgBldr!!.create()
            alrtDlg?.show()
            srchUrlValTV = alrtDlg?.findViewById(R.id.srchUrlValTV)
            srchUrlValTV?.text = srchUrlStr
            adBkmrkBtn = alrtDlg?.findViewById(R.id.adBkmrkBtn)
            shrBtn = alrtDlg?.findViewById(R.id.shrBtn)
            if (onBkmrkPg) {
                adBkmrkBtn?.visibility = View.GONE
            } else {
                adBkmrkBtn?.setOnClickListener { view1: View? ->
                    adBkmrk(URL.fetchUrl())
                    alrtDlg?.dismiss()
                }
            }
            shrBtn?.setOnClickListener { view1: View? ->
                if (url != null) {
                    val shrIntnt = Intent(Intent.ACTION_SEND)
                    shrIntnt.setType("text/plain")
                    shrIntnt.putExtra(Intent.EXTRA_TEXT, URL.fetchUrl())
                    startActivity(Intent.createChooser(shrIntnt, getString(R.string.shr)))
                }
            }
            queryET = alrtDlg?.findViewById(R.id.qryET)
        }
        adrBtn?.setOnClickListener { view: View? ->
            dlgBldr = AlertDialog.Builder(this)
            dlgBldr?.setView(R.layout.alert_dialog_url)
            dlgBldr?.setPositiveButton(R.string.go) { dialogInterface: DialogInterface?, i: Int ->
                connOk = false
                clickDriven = false
                adrUrlStr = adrUrlValET?.text?.toString()
                if (adrUrlStr != null) {
                    URL.initializeURL(adrUrlStr!!)
                    Log.d("Debug", "Host: ${URL.fetchUrlHost()}, Port: ${URL.fetchUrlPort()}")  // Add this debug log

                }
                if (URL.isUrlOkay) {
                    Log.d("adrUrlValET", "url != null && url.isUrlOkay()")
                } else {
                    adrUrlValET?.setText(adrUrlStr)
                    Log.d("adrUrlValET", "else")
                }
                // Create a new thread to execute the ConnectAsync task
                val thread = Thread(ConnectAsync())
                thread.start()
            }
            dlgBldr?.setNegativeButton(R.string.cncl) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
            alrtDlg = dlgBldr?.create()
            alrtDlg?.show()
        }



        rfrshBtn!!.setOnClickListener { view: View? ->
            if (adrUrlStr != "") {
                clickDriven = false
                // Create a new thread to execute the ConnectAsync task
                val thread = Thread(ConnectAsync())
                thread.start()
            }
        }
        bkBtn!!.setOnClickListener { view: View? ->
            var listSize = hstryArLst!!.size
            if (listSize > 1) {
                clickDriven = true
                backPressed = true
                if (onBkmrkPg) {
                    ++listSize
                } else {
                    hstryArLst!!.removeAt(listSize - 1)
                    itmTypArLst!!.removeAt(listSize - 1)
                }
                // Create a new thread to execute the ConnectAsync task
                val thread = Thread(ConnectAsync())
                thread.start()
            } else if (listSize == 1 && onBkmrkPg) {
                // Create a new thread to execute the ConnectAsync task
                val thread = Thread(ConnectAsync())
                thread.start()
            }
        }
    }

    fun initResources() {
        shrdPrfrncs = PreferenceManager.getDefaultSharedPreferences(this)
        prfEdtr = shrdPrfrncs?.edit()
        mainLL = findViewById(R.id.mainLinearLayout)
        cntntLL = findViewById(R.id.contentLinearLayout)
        stngsBtn = findViewById(R.id.stngsBtn)
        bkmrkBtn = findViewById(R.id.bkmrksBtn)
        adrBtn = findViewById(R.id.adrBtn)
        rfrshBtn = findViewById(R.id.rfrshBtn)
        bkBtn = findViewById(R.id.bkBtn)
        lprms = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        txtSizInt = shrdPrfrncs!!.getInt("txt_siz", 14)
        srchUrlStr = shrdPrfrncs?.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs")
        setTheme()
    }

    fun showBkmrks() {
        (cntntLL as LinearLayout?)!!.removeAllViews()
        val bkmrksStr = shrdPrfrncs!!.getString("bkmrks", "")
        val bkmrksSplt =
            bkmrksStr!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (j in bkmrksSplt.indices) {
            val url = bkmrksSplt[j]
            Log.d("url", "bkmrksSplt[j]")
            if (url != "") {
                val bkmrkBtn = Button(this)
                bkmrkBtn.layoutParams = lprms
                bkmrkBtn.textAlignment = Button.TEXT_ALIGNMENT_TEXT_START
                bkmrkBtn.text = url
                Log.d("bkmrkBtn", "setText(url)")
                bkmrkBtn.setOnClickListener { view: View? ->
                    connOk = false
                    clickDriven = false
                    dlgBldr = AlertDialog.Builder(this)
                    dlgBldr!!.setPositiveButton(R.string.go) { dialogInterface: DialogInterface?, i: Int ->
                        callInitConnection(
                            url,
                            '\u0000'
                        )
                    }
                    dlgBldr!!.setNegativeButton(R.string.rmv) { dialogInterface: DialogInterface?, i: Int ->
                        rmvBkmrk(
                            j
                        )
                    }
                    alrtDlg = dlgBldr!!.create()
                    alrtDlg?.show()
                }
                (cntntLL as LinearLayout?)!!.addView(bkmrkBtn)
            }
        }
    }

    fun rmvBkmrk(pos: Int) {
        var bkmrksStr = shrdPrfrncs!!.getString("bkmrks", "")
        val bkmrksSplt =
            bkmrksStr!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        bkmrksStr = ""
        var i = 0
        for (bkmrkSpltStr in bkmrksSplt) {
            if (i != pos) {
                bkmrksStr = "$bkmrksStr$bkmrkSpltStr,"
            }
            i++
        }
        prfEdtr!!.putString("bkmrks", bkmrksStr)
        prfEdtr!!.apply()
        showBkmrks()
    }

    fun adBkmrk(url: String?) {
        val bkmrksStr = shrdPrfrncs!!.getString("bkmrks", "")
        val bkmrksSplt =
            bkmrksStr!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (bkmrkUrlStr in bkmrksSplt) {
            if (url == bkmrkUrlStr) {
                return
            }
        }
        prfEdtr!!.putString("bkmrks", "$bkmrksStr,$url")
        prfEdtr!!.apply()
        Toast.makeText(this, R.string.bkmrk_added, Toast.LENGTH_SHORT).show()
    }

    fun setTheme() {
        mainLL!!.setBackgroundColor(-0x1000000)
        stngsBtn!!.setTextColor(-0x1)
        bkmrkBtn!!.setTextColor(-0x1)
        adrBtn!!.setTextColor(-0x1)
        rfrshBtn!!.setTextColor(-0x1)
        bkBtn!!.setTextColor(-0x1)
        window.navigationBarColor = -0x1000000
    }

    fun openSettings() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)
    }

    private fun callInitConnection(inputURL: String, itmTyp: Char) {
        closeComponents()
        onBkmrkPg = false
        Log.d("url", "new URL(inputURL)")
        if (clickDriven) {
            URL.urlItemType = itmTyp
        }
        if (URL.isUrlOkay) {
            displayMessage('l', inputURL)
            Log.d("url", "displayMessage('l', inputURL)")
            val executor = Executors.newSingleThreadExecutor()
            Log.d("url", "new URL(inputURL)")
            executor.shutdown() // Shutdown the executor when no longer needed
            Log.d("url", "ExecutorService executor = Executors.newSingleThreadExecutor()")
        } else {
            displayMessage('e', "")
            Log.d("url", "displayMessage('e')")
        }
    }

    fun closeComponents() {
        if (prntWrtr != null) {
            prntWrtr!!.close()
            prntWrtr = null
        }
        if (bfrdRdr != null) {
            try {
                bfrdRdr!!.close()
                bfrdRdr = null
            } catch (ignore: IOException) {
            }
        }
        if (sckt != null) {
            try {
                sckt!!.close()
                sckt = null
            } catch (ignore: IOException) {
            }
        }
    }

    fun displayTxtFile() {
        var txt = ""
        for (i in lnArLst!!.indices) {
            txt = txt + lnArLst!![i] + "\n"
        }
        lnTV = TextView(this@MainActivity)
        setTextViewProps(lnTV!!)
        lnTV!!.setTextIsSelectable(true)
        lnTV!!.text = txt
        (cntntLL as LinearLayout?)!!.addView(lnTV)
    }

    fun addLine() {
        lnTV = TextView(this)
        setTextViewProps(lnTV!!)
        lnTV!!.text = tempLine
        (cntntLL as LinearLayout?)!!.addView(lnTV)
    }

    fun displayContent() {
        println(URL.fetchUrlItemType())
        Log.d("", "System.out.println(url.getUrlItemType())")
        if (connOk) {
            (cntntLL as LinearLayout?)!!.removeAllViews()
            lnTV = TextView(this)
            when (URL.fetchUrlItemType()) {
                '0' -> {
                    displayTxtFile()
                }

                '1', '7' -> {
                    var i = 0
                    while (i < lnArLst!!.size) {
                        tempLine = lnArLst!![i]
                        if (tempLine!!.length == 0) {
                            i++
                            continue
                        }
                        val itemType = tempLine!![0]
                        var typePresent = true
                        var resId = 0
                        when (itemType) {
                            '0' -> resId = R.string.ct_txt
                            '1' -> resId = R.string.ct_sub
                            '2' -> resId = R.string.ct_ccs
                            '3' -> resId = R.string.ct_err
                            '4' -> resId = R.string.ct_bhx
                            '5' -> resId = R.string.ct_dos
                            '6' -> resId = R.string.ct_uue
                            '7' -> resId = R.string.ct_srh
                            '8' -> resId = R.string.ct_tnt
                            '9' -> resId = R.string.ct_bin
                            '+' -> resId = R.string.ct_mir
                            'g' -> resId = R.string.ct_gif
                            'i' -> tempLine = tempLine!!.substring(1, tempLine!!.indexOf('\t'))
                            'I' -> resId = R.string.ct_img
                            else -> typePresent = false
                        }
                        if (itemType != 'i' && typePresent) {
                            itmBtn = Button(this)
                            itmBtn!!.layoutParams = lprms
                            itmBtn!!.textAlignment = Button.TEXT_ALIGNMENT_TEXT_START
                            lineTknzr = StringTokenizer(tempLine, "\t")
                            tempLine = getString(resId) + " " + lineTknzr!!.nextToken().substring(1)
                            itmBtn!!.text = tempLine
                            val finalI = i
                            itmBtn!!.setOnClickListener { view: View? ->
                                clickDriven = true
                                val lineSplit = lnArLst!![finalI]!!.split("\t".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                                val tempUrl = lineSplit[2] + ":" + lineSplit[3] + lineSplit[1]
                                val itmTyp = lineSplit[0][0]
                                setUrlParts(lineSplit, itemType)
                                when (itmTyp) {
                                    '0', '1' -> callInitConnection(tempUrl, itmTyp)
                                    '7' -> {
                                        dlgBldr = AlertDialog.Builder(this)
                                        dlgBldr!!.setView(R.layout.alert_dialog_options)
                                        dlgBldr!!.setNegativeButton(
                                            R.string.cncl
                                        ) { dialogInterface: DialogInterface, i1: Int -> dialogInterface.cancel() }
                                        dlgBldr!!.setPositiveButton(
                                            R.string.srch
                                        ) { dialogInterface: DialogInterface?, i1: Int -> initiateSearch() }
                                        alrtDlg = dlgBldr!!.create()
                                        alrtDlg?.show()
                                        srchUrlValTV = alrtDlg?.findViewById(R.id.srchUrlValTV)
                                        adBkmrkBtn = alrtDlg?.findViewById(R.id.adBkmrkBtn)
                                        adBkmrkBtn?.visibility = View.GONE
                                        queryET = alrtDlg?.findViewById(R.id.qryET)
                                        srchUrlValTV?.text = tempUrl
                                    }

                                    else -> {
                                        dlgBldr = AlertDialog.Builder(this)
                                        dlgBldr!!.setMessage(
                                            getString(R.string.fnm)
                                                    + " " + lineSplit[1]
                                                .substring(lineSplit[1].lastIndexOf("/") + 1)
                                        )
                                        dlgBldr!!.setPositiveButton(
                                            R.string.dnld
                                        ) { dialogInterface: DialogInterface?, i1: Int ->
                                            callInitConnection(
                                                tempUrl,
                                                itmTyp
                                            )
                                        }
                                        dlgBldr!!.setNegativeButton(
                                            R.string.cncl
                                        ) { dialogInterface: DialogInterface, i1: Int -> dialogInterface.cancel() }
                                        alrtDlg = dlgBldr!!.create()
                                        alrtDlg?.show()
                                    }
                                }
                            }
                            (cntntLL as LinearLayout?)!!.addView(itmBtn)
                        } else {
                            addLine()
                        }
                        i++
                    }
                }

                else -> displayMessage('d', fileName)
            }
        } else {
            displayMessage('e', "")
        }
    }

    fun initiateSearch() {
        clickDriven = true
        callInitConnection(srchUrlStr + "\t" + queryET!!.text.toString(), '7')
    }

    fun setUrlParts(lineSplit: Array<String>, itemType: Char) {
        URL.urlItemType = itemType
        URL.urlPath = lineSplit[1]
        URL.urlHost = lineSplit[2]
        URL.urlPort = lineSplit[3].toInt()
        URL.makeURLfromParts()
    }

    fun displayMessage(ch: Char, param: String?) {
        (cntntLL as LinearLayout?)!!.removeAllViews()
        lnTV = TextView(this@MainActivity)
        setTextViewProps(lnTV!!)
        val msg: String
        msg = when (ch) {
            'd' -> if (fileDnlded) {
                getString(R.string.fnm) + " " + param + ", " + getString(R.string.lction) + " " + dnldDir
            } else {
                getString(R.string.dnld_failed)
            }

            'l' -> getString(R.string.loading) + " " + param
            'e' -> when (URL.fetchErrorCode()) {
                1 -> getString(R.string.invalid_protocol)
                2 -> getString(R.string.host_not_found)
                3 -> getString(R.string.invalid_port)
                4 -> getString(R.string.invalid_path)
                else -> ""
            }

            else -> ""
        }
        lnTV!!.text = msg
        (cntntLL as LinearLayout?)!!.addView(lnTV)
    }

    fun setTextViewProps(tv: TextView) {
        tv.setTextColor(-0x1)
        tv.setTextSize(2, txtSizInt.toFloat())
        tv.typeface = Typeface.MONOSPACE
    }

    private inner class ConnectAsync : Runnable {
        override fun run() {
            var socket: Socket? = null
            try {
                lnArLst!!.clear()
                try {

                    socket = Socket()
                    val host = URL.fetchUrlHost()
                    Log.d("Socket Connection", "Host: $host, Port: $URL.fetchUrlPort()")
                    if (host != null) {
                        socket.connect(
                            InetSocketAddress(host, URL.fetchUrlPort()),
                            CONNECTION_TIMEOUT
                        )
                    } else {

                        Log.d("Socket Connection", "Host: $host, Port: $URL.fetchUrlPort()")
                    }
                } catch (e: IOException) {
                    URL.errorCode = 2
                    connOk = false
                    e.printStackTrace()
                    Log.d("socket", "e.printStackTrace()")
                    return
                }
                sckt = socket
                prntWrtr = PrintWriter(sckt!!.getOutputStream())
                prntWrtr!!.write(URL.fetchUrlPath() + URL.fetchUrlQuery() + "\r\n")
                prntWrtr!!.flush()
                var read: Int
                var tl = ""
                bfrdRdr = BufferedReader(InputStreamReader(sckt!!.getInputStream()))
                val fileSig = ByteArray(192)
                val inputStream = sckt!!.getInputStream()
                if (inputStream.read(fileSig).also { read = it } != -1) {
                    var i = 0
                    var isBinary = false
                    var onceDone = false
                    while (i < read) {
                        if (fileSig[i].toInt() == 10 || fileSig[i].toInt() == 13) {
                            if (!onceDone) {
                                onceDone = true
                            } else {
                                onceDone = false
                                i++
                                continue
                            }
                            lnArLst!!.add(tl)
                            tl = ""
                            i++
                            continue
                        }
                        if (fileSig[i].toInt() == 0 || fileSig[i].toInt() == 1 || fileSig[i].toInt() == 2 || fileSig[i].toInt() == 3) {
                            isBinary = true
                            break
                        }
                        tl = tl + Char(fileSig[i].toUShort()).toString()
                        i++
                    }
                    if (!clickDriven) {
                        if (isBinary) {
                            URL.urlItemType = 9.toChar()
                        } else {
                            i = 0
                            var count = 0
                            while (i < read) {
                                if (fileSig[i].toInt() == 10) {
                                    break
                                } else if (fileSig[i].toInt() == 9) {
                                    count++
                                }
                                i++
                            }
                            if (count != 3) {
                                URL.urlItemType = '0'
                            } else {
                                URL.urlItemType = '1'
                            }
                        }
                    }
                } else {
                    URL.errorCode = 4
                    connOk = false
                    return
                }
                when (URL.fetchUrlItemType()) {
                    '0', '1', '7' -> {
                        if (bfrdRdr!!.readLine().also { tempLine = it } != null) {
                            lnArLst!!.add(tl + tempLine)
                        }
                        while (bfrdRdr!!.readLine().also { tempLine = it } != null) {
                            lnArLst!!.add(tempLine)
                        }
                    }

                    else -> {
                        fileDnlded = false
                        val bytes = ByteArray(1024)
                        fileName = URL.fetchUrlPath()!!
                            .substring(URL.fetchUrlPath()!!.lastIndexOf("/") + 1)
                        val fileStream = Files.newOutputStream(Paths.get("$dnldDir/$fileName"))
                        fileStream.write(fileSig, 0, read)
                        while (inputStream.read(bytes).also { read = it } != -1) {
                            fileStream.write(bytes, 0, read)
                        }
                        fileStream.flush()
                        fileStream.close()
                        fileDnlded = true
                    }
                }
                inputStream.close()
            } catch (e: IOException) {
                URL.errorCode = 2
                connOk = false
                e.printStackTrace()
                Log.d("url", "e.printStackTrace()")
            } finally {
                // Close the socket in the finally block
                if (socket != null) {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.d("socket != null", "e.printStackTrace()")
                    }
                }
                closeComponents()
                connOk = true
                if (url != null) {
                    URL.makeURLfromParts()
                    Log.d("url", "makeURLfromParts()")
                    adrUrlStr = URL.fetchUrl()
                    if (!backPressed) {
                        hstryArLst!!.add(URL.fetchUrl())
                        itmTypArLst!!.add(URL.fetchUrlItemType().toString())
                    } else {
                        backPressed = false
                        Log.d("", "backPressed = false")
                    }
                }

                // Dismiss the dialog outside the runOnUiThread block
                alrtDlg!!.dismiss()
                runOnUiThread {
                    if (connOk) {
                        displayContent()
                    } else {
                        displayMessage('e', "")
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage(R.string.cnfrmtn)
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                val pid = Process.myPid()
                Process.killProcess(pid)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("on_bkmrk_pg", onBkmrkPg)
        outState.putBoolean("conn_okay", connOk)
        outState.putStringArrayList("line_arraylist", lnArLst)
        outState.putStringArrayList("history_arraylist", hstryArLst)
        outState.putStringArrayList("itemtype_arraylist", itmTypArLst)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        onBkmrkPg = savedInstanceState.getBoolean("on_bkmrk_pg", true)
        connOk = savedInstanceState.getBoolean("conn_okay", false)
        lnArLst = savedInstanceState.getStringArrayList("line_arraylist")
        hstryArLst = savedInstanceState.getStringArrayList("history_arraylist")
        itmTypArLst = savedInstanceState.getStringArrayList("itemtype_arraylist")
        if (!onBkmrkPg) {
            if (url != null) {
                displayContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adrUrlStr = ""
        Log.d("", "adrUrlStr = null")
        txtSizInt = shrdPrfrncs!!.getInt("txt_siz", 14)
        srchUrlStr = shrdPrfrncs!!.getString("srch_url", "gopher://gopher.floodgap.com/v2/vs")
        setTheme()
    }


}