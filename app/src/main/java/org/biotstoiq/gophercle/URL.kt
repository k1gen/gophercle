package org.biotstoiq.gophercle

@Suppress("SpellCheckingInspection")
class URL internal constructor(var url: String) {
    init {
        isUrlOkay = extractURLParts()
    }

    fun extractURLParts(): Boolean {
        var index: Int
        if (Companion.url.contains("://")) {
            index = Companion.url.indexOf(':')
            protocol = Companion.url.substring(0, index)
            if (protocol != "gopher") {
                Companion.errorCode = 1
                return false
            }
            Companion.url = Companion.url.substring(index + 3)
        } else {
            protocol = "gopher"
        }
        index = Companion.url.indexOf(":")
        if (index != -1) {
            urlHost = Companion.url.substring(0, index)
            val extractedPort: Boolean
            if (Companion.url.contains("/")) {
                extractedPort = extractPort(Companion.url.substring(index + 1, Companion.url.indexOf("/").also { index = it }))
                Companion.url = Companion.url.substring(index)
            } else {
                extractedPort = extractPort(Companion.url.substring(index + 1))
                Companion.url = ""
            }
            if (!extractedPort) {
                Companion.errorCode = 3
                return false
            }
        } else {
            urlPort = 70
            index = Companion.url.indexOf("/")
            if (index != -1) {
                urlHost = Companion.url.substring(0, index)
                Companion.url = Companion.url.substring(index)
            } else {
                urlHost = Companion.url
                Companion.url = ""
            }
        }
        if (Companion.url.length > 1) {
            Companion.url = Companion.url.substring(1)
            index = Companion.url.indexOf('/')
            if (index == 1) {
                urlPath = Companion.url.substring(index)
            } else {
                urlPath = "/" + Companion.url
            }
        } else {
            urlPath = "/"
        }
        index = urlPath!!.indexOf('\t')
        if (index != -1) {
            urlQuery = urlPath!!.substring(index)
        } else {
            urlQuery = ""
        }
        return true
    }

    fun makeURLfromParts() {
        Companion.url = protocol + "://" + urlHost + ":" + urlPort.toString() + "/" + urlItemType.toString() + urlPath
    }

    fun extractPort(string: String): Boolean {
        val port: Int
        port = try {
            string.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return false
        }
        urlPort = port
        return true
    }

    var errorCode: Int
        get() = Companion.errorCode
        set(ec) {
            Companion.errorCode = ec
        }
    val url: String
        get() = Companion.url

    companion object {
        var protocol: String? = null
        var urlHost: String? = null
            get() = Companion.field
            set(urlHst) {
                urlHost = urlHst
            }
        var urlPort = 0
            get() = Companion.field
            set(urlPrt) {
                urlPort = urlPrt
            }
        var urlPath: String? = null
            get() = Companion.field
            set(urlPth) {
                urlPath = urlPth
            }
        var urlQuery: String? = null
            get() = Companion.field
        var urlItemType = 0.toChar()
            get() = Companion.field
            set(itmTyp) {
                urlItemType = itmTyp
            }
        var errorCode = 0
        var isUrlOkay: Boolean
            get() = Companion.field
    }
}
