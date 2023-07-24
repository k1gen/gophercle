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

    companion object {
        @JvmField
        var errorCode: Int = 0

        @JvmField
        var url: String = ""

        @JvmField
        var protocol: String? = null

        @JvmField
        var urlHost: String? = null

        @JvmField
        var urlPort: Int = 0

        @JvmField
        var urlPath: String? = null

        @JvmField
        var urlQuery: String? = null

        @JvmField
        var urlItemType: Char = 0.toChar()

        @JvmField
        var isUrlOkay: Boolean = false

        @JvmStatic
        fun setErrorCode(ec: Int) {
            errorCode = ec
        }

        fun fetchErrorCode(): Int {
            return errorCode
        }

        fun fetchUrl(): String {
            return url
        }

        fun isUrlOkay(): Boolean {
            return isUrlOkay
        }

        fun fetchUrlHost(): String? {
            return urlHost
        }

        fun setUrlHost(urlHst: String) {
            urlHost = urlHst
        }

        fun fetchUrlPort(): Int {
            return urlPort
        }

        fun setUrlPort(urlPrt: Int) {
            urlPort = urlPrt
        }

        fun fetchUrlPath(): String? {
            return urlPath
        }

        fun setUrlPath(urlPth: String) {
            urlPath = urlPth
        }

        fun fetchUrlQuery(): String? {
            return urlQuery
        }

        fun fetchUrlItemType(): Char {
            return urlItemType
        }

        fun setUrlItemType(itmTyp: Char) {
            urlItemType = itmTyp
        }

        fun makeURLfromParts() {
            url = "$protocol://$urlHost:$urlPort/$urlItemType$urlPath"
        }
    }
}
