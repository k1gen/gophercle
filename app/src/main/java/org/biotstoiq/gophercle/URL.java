package org.biotstoiq.gophercle;

public class URL {

    static String url;
    static String protocol;
    static String host;
    static int port;
    static String path;
    static String query;
    static char itemType;

    static int errorCode;

    static boolean urlOkay;

    URL(String input) {
        url = input;
        urlOkay = extractURLParts();
    }

    boolean extractURLParts() {
        int index;

        if(url.contains("://")) {
            index = url.indexOf(':');
            protocol = url.substring(0, index);
            if(!protocol.equals("gopher")) {
                errorCode = 1;
                return false;
            }
            url = url.substring(index+ + 3);
        } else {
            protocol = "gopher";
        }

        index = url.indexOf(":");
        if(index != -1) {
            host = url.substring(0, index);
            boolean extractedPort;
            if(url.contains("/")) {
                extractedPort = extractPort(url.substring(index + 1, index = url.indexOf("/")));
                url = url.substring(index);
            } else {
                extractedPort = extractPort(url.substring(index + 1));
                url = "";
            }
            if (!extractedPort) {
                errorCode = 3;
                return false;
            }
        } else {
            port = 70;
            index = url.indexOf("/");
            if(index != -1) {
              host = url.substring(0, index);
              url = url.substring(index);
            } else {
              host = url;
              url = "";
            }
        }

        if(url.length() > 1) {
          url = url.substring(1);
          index = url.indexOf('/');
          if(index == 1) {
            path = url.substring(index);
          } else {
            path = "/".concat(url);
          }
        } else {
          path = "/";
        }

        index = path.indexOf('\t');
        if (index != -1) {
            query = path.substring(index);
        } else {
            query = "";
        }
        return true;
    }

    void makeURLfromParts() {
        url = protocol.concat("://").concat(host).concat(":")
                .concat(String.valueOf(port).concat("/"))
                .concat(String.valueOf(itemType)).concat(path);
    }

    boolean extractPort(String string) {
        int port;
        try {
            port = Integer.parseInt(string);
        } catch(NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        URL.port = port;
        return true;
    }

    int getErrorCode() { return errorCode; }
    void setErrorCode(int ec) { errorCode = ec; }

    String getUrl() {
        return url;
    }

    boolean isUrlOkay() { return urlOkay; }

    String getUrlHost() { return host; }
    void setUrlHost(String urlHst) { host = urlHst; }

    int getUrlPort() { return port; }
    void setUrlPort(int urlPrt) { port = urlPrt; }

    String getUrlPath() { return path; }
    void setUrlPath(String urlPth) { path = urlPth; }

    String getUrlQuery() { return query; }

    char getUrlItemType() { return itemType; }
    void setUrlItemType(char itmTyp) { itemType = itmTyp; }
}
