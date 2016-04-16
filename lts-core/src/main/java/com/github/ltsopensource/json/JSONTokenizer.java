package com.github.ltsopensource.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Robert HG (254963746@qq.com) on 12/28/15.
 */
public class JSONTokenizer {

    private long character;
    private boolean eof;
    private long index;
    private long line;
    private char previous;
    private Reader reader;
    private boolean usePrevious;

    public JSONTokenizer(Reader reader) {
        this.reader = reader.markSupported()
                ? reader
                : new BufferedReader(reader);
        this.eof = false;
        this.usePrevious = false;
        this.previous = 0;
        this.index = 0;
        this.character = 1;
        this.line = 1;
    }

    public JSONTokenizer(String s) {
        this(new StringReader(s));
    }

    public void back() throws JSONException {
        if (this.usePrevious || this.index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        this.index -= 1;
        this.character -= 1;
        this.usePrevious = true;
        this.eof = false;
    }

    public boolean end() {
        return this.eof && !this.usePrevious;
    }

    public char next() throws JSONException {
        int c;
        if (this.usePrevious) {
            this.usePrevious = false;
            c = this.previous;
        } else {
            try {
                c = this.reader.read();
            } catch (IOException exception) {
                throw new JSONException(exception);
            }

            if (c <= 0) {
                this.eof = true;
                c = 0;
            }
        }
        this.index += 1;
        if (this.previous == '\r') {
            this.line += 1;
            this.character = c == '\n' ? 0 : 1;
        } else if (c == '\n') {
            this.line += 1;
            this.character = 0;
        } else {
            this.character += 1;
        }
        this.previous = (char) c;
        return this.previous;
    }

    public char next(char c) throws JSONException {
        char n = this.next();
        if (n != c) {
            throw this.syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
        }
        return n;
    }

    public String next(int n) throws JSONException {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = this.next();
            if (this.end()) {
                throw this.syntaxError("Substring bounds error");
            }
            pos += 1;
        }
        return new String(chars);
    }

    public char nextClean() throws JSONException {
        for (; ; ) {
            char c = this.next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = this.next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw this.syntaxError("Unterminated string");
                case '\\':
                    c = this.next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char) Integer.parseInt(this.next(4), 16));
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw this.syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    public Object nextValue() throws JSONException {
        char c = this.nextClean();
        String string;

        switch (c) {
            case '"':
            case '\'':
                return this.nextString(c);
            case '{':
                this.back();
                return new JSONObject(this);
            case '[':
                this.back();
                return new JSONArray(this);
        }

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = this.next();
        }
        this.back();

        string = sb.toString().trim();
        if ("".equals(string)) {
            throw this.syntaxError("Missing value");
        }
        return stringToValue(string);
    }

    private Object stringToValue(String string) {
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '-') {
            try {
                if (string.indexOf('.') > -1 || string.indexOf('e') > -1
                        || string.indexOf('E') > -1) {
                    Double d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (string.equals(myLong.toString())) {
                        if (myLong == myLong.intValue()) {
                            return myLong.intValue();
                        } else {
                            return myLong;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return string;
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this.toString());
    }

    public String toString() {
        return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
    }
}
