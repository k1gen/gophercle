package org.biotstoiq.gophercle

@Suppress("SpellCheckingInspection")
class URL {
    companion object {
        var errorCode: Int = 0
        var url: String = ""
        var protocol: String? = null
        var urlHost: String? = null
        var urlPort: Int = 0
        var urlPath: String? = null
        var urlQuery: String? = null
        var urlItemType: Char = 0.toChar()
        var isUrlOkay: Boolean = false

        fun fetchErrorCode(): Int {
            return errorCode
        }

        fun fetchUrl(): String {
            return url
        }

        fun fetchUrlHost(): String? {
            return urlHost
        }


        fun fetchUrlPort(): Int {
            return urlPort
        }


        fun fetchUrlPath(): String? {
            return urlPath
        }


        fun fetchUrlQuery(): String? {
            return urlQuery
        }

        fun fetchUrlItemType(): Char {
            return urlItemType
        }


        fun makeURLfromParts() {
            url = "$protocol://$urlHost:$urlPort/$urlItemType$urlPath"
        }
        fun initializeURL(urlString: String) {
            url = urlString
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
                index = url.indexOf("/")
                if (index != -1) {
                    urlHost = url.substring(0, index)
                    urlPort = 70
                    url = url.substring(index)
                } else {
                    urlHost = url
                    urlPort = 70
                    url = ""
                }
            }
            if (url.isNotEmpty()) {
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

        private fun extractPort(string: String): Boolean {
            return try {
                urlPort = string.toInt()
                true
            } catch (e: NumberFormatException) {
                errorCode = 2
                false
            }
        }

    }
}
