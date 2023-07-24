package org.biotstoiq.gophercle

import android.util.Log

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
            Log.d("Debug", "Received URL: $url")
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

            // Extract host and port
            if (url.contains(":")) {
                index = url.indexOf(":")
                val hostAndPort = url.split(":", limit = 2)
                urlHost = hostAndPort[0]
                val portAndPath = hostAndPort[1].split("/", limit = 2)
                urlPort = portAndPath[0].toIntOrNull() ?: 70
                url = if (portAndPath.size > 1) "/${portAndPath[1]}" else ""
            } else {
                val hostAndPath = url.split("/", limit = 2)
                urlHost = hostAndPath[0]
                urlPort = 70
                url = if (hostAndPath.size > 1) "/${hostAndPath[1]}" else ""
            }

            // Extract path
            urlPath = if (url.isNotEmpty()) url else "/"

            // Extract query
            index = urlPath!!.indexOf('\t')
            urlQuery = if (index != -1) {
                urlPath!!.substring(index)
            } else {
                ""
            }

            // Add debug logging to check parsed values
            Log.d("Debug", "Parsed protocol: $protocol")
            Log.d("Debug", "Parsed host: $urlHost")
            Log.d("Debug", "Parsed port: $urlPort")
            Log.d("Debug", "Parsed path: $urlPath")
            Log.d("Debug", "Parsed query: $urlQuery")

            return true
        }


        // Helper function to find first occurrence of '/', '?' or '#'
        fun findFirstSpecialChar(str: String): Int {
            val firstSlash = str.indexOf("/")
            val firstQuestion = str.indexOf("?")
            val firstHash = str.indexOf("#")
            return minOf(
                if (firstSlash != -1) firstSlash else str.length,
                if (firstQuestion != -1) firstQuestion else str.length,
                if (firstHash != -1) firstHash else str.length
            )
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
