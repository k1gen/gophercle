package org.biotstoiq.gophercle

@Suppress("SpellCheckingInspection")
class URL internal constructor(url: String) {
    init {
        isUrlOkay = extractURLParts()
    }

    fun extractURLParts(): Boolean {
        var index: Int
        if (url.contains("://")) {
            index = url.indexOf(':')
            protocol = url.substring(0, index)
            if (protocol != "gopher") {
                errorCode = 1
                return false
            }
            url = url.substring(index + 3)
        } else {
            protocol = "gopher"
        }
        index = url.indexOf(":")
        if (index != -1) {
            urlHost = url.substring(0, index)
            val extractedPort: Boolean
            if (url.contains("/")) {
                extractedPort = extractPort(url.substring(index + 1, url.indexOf("/").also { index = it }))
                url = url.substring(index)
            } else {
                extractedPort = extractPort(url.substring(index + 1))
                url = ""
            }
            if (!extractedPort) {
                errorCode = 3
                return false
            }
        } else {
            urlPort = 70
            index = url.indexOf("/")
            if (index != -1) {
                urlHost = url.substring(0, index)
                url = url.substring(index)
            } else {
                urlHost = url
                url = ""
            }
        }
        if (url.length > 1) {
            url = url.substring(1)
            index = url.indexOf('/')
            urlPath = if (index == 1) {
                url.substring(index)
            } else {
                "/" + url
            }
        } else {
            urlPath = "/"
        }
        index = urlPath!!.indexOf('\t')
        urlQuery = if (index != -1) {
            urlPath!!.substring(index)
        } else {
            ""
        }
        return true
    }

    fun makeURLfromParts() {
        url = protocol + "://" + urlHost + ":" + urlPort.toString() + "/" + urlItemType.toString() + urlPath
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

    var errorCode: Int = 0
    var url: String = ""
        private set

    companion object {
        var protocol: String? = null
        var urlHost: String? = null
            private set
        var urlPort: Int = 0
            private set
        var urlPath: String? = null
            private set
        var urlQuery: String? = null
            private set
        var urlItemType: Char = 0.toChar()
            private set
        var isUrlOkay: Boolean = false
            private set
    }
}
