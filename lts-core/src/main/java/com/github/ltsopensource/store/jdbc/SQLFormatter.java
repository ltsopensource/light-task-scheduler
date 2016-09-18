package com.github.ltsopensource.store.jdbc;

import java.util.*;

/**
 * 格式化SQL
 * 来自hibernate更改
 *
 * @author Robert HG (254963746@qq.com) on 3/9/16.
 */
public class SQLFormatter {

    private static final String WHITESPACE = " \n\r\f\t";

    private static final Set<String> BEGIN_CLAUSES = new HashSet<String>();
    private static final Set<String> END_CLAUSES = new HashSet<String>();
    private static final Set<String> LOGICAL = new HashSet<String>();
    private static final Set<String> QUANTIFIERS = new HashSet<String>();
    private static final Set<String> DML = new HashSet<String>();
    private static final Set<String> MISC = new HashSet<String>();
    private static final Set<String> UPPER_CASE = new HashSet<String>();

    static {

        BEGIN_CLAUSES.add("left");
        BEGIN_CLAUSES.add("right");
        BEGIN_CLAUSES.add("inner");
        BEGIN_CLAUSES.add("outer");
        BEGIN_CLAUSES.add("group");
        BEGIN_CLAUSES.add("order");

        END_CLAUSES.add("where");
        END_CLAUSES.add("set");
        END_CLAUSES.add("having");
        END_CLAUSES.add("join");
        END_CLAUSES.add("from");
        END_CLAUSES.add("by");
        END_CLAUSES.add("join");
        END_CLAUSES.add("into");
        END_CLAUSES.add("union");

        LOGICAL.add("and");
        LOGICAL.add("or");
        LOGICAL.add("when");
        LOGICAL.add("else");
        LOGICAL.add("end");

        QUANTIFIERS.add("in");
        QUANTIFIERS.add("all");
        QUANTIFIERS.add("exists");
        QUANTIFIERS.add("some");
        QUANTIFIERS.add("any");

        DML.add("insert");
        DML.add("update");
        DML.add("delete");

        MISC.add("select");
        MISC.add("on");
        //MISC.add("values");

        // 这个是mysql关键字列表, 会转化为大写 (需要的时候添加)
        UPPER_CASE.addAll(Arrays.asList("select", "insert", "update", "delete", "on", "left", "right", "inner", "outer", "group",
                "order", "where", "set", "having", "join", "from", "by", "join", "into",
                "union", "and", "or", "when", "else", "end", "in", "all", "exists", "if", "some", "any", "distinct", "count", "sum", "as", "asc", "desc", "between",
                "alter", "table", "else", "elseif", "index", "ignore", "limit", "like", "offset"
        ));

    }

    String indentString = "    ";
    String initial = "\n    ";

    boolean beginLine = true;
    boolean afterBeginBeforeEnd = false;
    boolean afterByOrSetOrFromOrSelect = false;
    boolean afterValues = false;
    boolean afterOn = false;
    boolean afterBetween = false;
    boolean afterInsert = false;
    int inFunction = 0;
    int parensSinceSelect = 0;
    private LinkedList<Integer> parenCounts = new LinkedList<Integer>();
    private LinkedList<Boolean> afterByOrFromOrSelects = new LinkedList<Boolean>();

    int indent = 1;

    StringBuffer result = new StringBuffer();
    StringTokenizer tokens;
    String lastToken;
    String token;
    String lcToken;

    public SQLFormatter(String sql) {
        tokens = new StringTokenizer(
                sql,
                "()+*/-=<>'`\"[]," + WHITESPACE,
                true
        );
    }

    public SQLFormatter setInitialString(String initial) {
        this.initial = initial;
        return this;
    }

    public SQLFormatter setIndentString(String indent) {
        this.indentString = indent;
        return this;
    }

    public String format() {

        result.append(initial);

        while (tokens.hasMoreTokens()) {
            token = tokens.nextToken();
            lcToken = token.toLowerCase();

            if ("'".equals(token)) {
                String t;
                do {
                    t = tokens.nextToken();
                    token += t;
                }
                while (!"'".equals(t) && tokens.hasMoreTokens()); // cannot handle single quotes
            } else if ("\"".equals(token)) {
                String t;
                do {
                    t = tokens.nextToken();
                    token += t;
                }
                while (!"\"".equals(t));
            }

            if (afterByOrSetOrFromOrSelect && ",".equals(token)) {
                commaAfterByOrFromOrSelect();
            } else if (afterOn && ",".equals(token)) {
                commaAfterOn();
            } else if ("(".equals(token)) {
                openParen();
            } else if (")".equals(token)) {
                closeParen();
            } else if (BEGIN_CLAUSES.contains(lcToken)) {
                beginNewClause();
            } else if (END_CLAUSES.contains(lcToken)) {
                endNewClause();
            } else if ("select".equals(lcToken)) {
                select();
            } else if (DML.contains(lcToken)) {
                updateOrInsertOrDelete();
            } else if ("values".equals(lcToken)) {
                values();
            } else if ("on".equals(lcToken)) {
                on();
            } else if (afterBetween && lcToken.equals("and")) {
                misc();
                afterBetween = false;
            } else if (LOGICAL.contains(lcToken)) {
                logical();
            } else if (isWhitespace(token)) {
                white();
            } else {
                misc();
            }

            if (!isWhitespace(token)) lastToken = lcToken;

        }
        return result.toString();
    }

    private void commaAfterOn() {
        out();
        indent--;
        newline();
        afterOn = false;
        afterByOrSetOrFromOrSelect = true;
    }

    private void commaAfterByOrFromOrSelect() {
        out();
        newline();
    }

    private void logical() {
        if ("end".equals(lcToken)) indent--;
        newline();
        out();
        beginLine = false;
    }

    private void on() {
        indent++;
        afterOn = true;
        newline();
        out();
        beginLine = false;
    }

    private void misc() {
        out();
        if ("between".equals(lcToken)) {
            afterBetween = true;
        }
        if (afterInsert) {
            newline();
            afterInsert = false;
        } else {
            beginLine = false;
            if ("case".equals(lcToken)) {
                indent++;
            }
        }
    }

    private void white() {
        if (!beginLine) {
            result.append(" ");
        }
    }

    private void updateOrInsertOrDelete() {
        out();
        indent++;
        beginLine = false;
        if ("update".equals(lcToken)) newline();
        if ("insert".equals(lcToken)) afterInsert = true;
    }

    private void select() {
        out();
        indent++;
        newline();
        parenCounts.addLast(parensSinceSelect);
        afterByOrFromOrSelects.addLast(afterByOrSetOrFromOrSelect);
        parensSinceSelect = 0;
        afterByOrSetOrFromOrSelect = true;
    }

    private void out() {
        if (UPPER_CASE.contains(token.toLowerCase())) {
            token = token.toUpperCase();
        }
        result.append(token);
    }

    private void endNewClause() {
        if (!afterBeginBeforeEnd) {
            indent--;
            if (afterOn) {
                indent--;
                afterOn = false;
            }
            newline();
        }
        out();
        if (!"union".equals(lcToken)) indent++;
        newline();
        afterBeginBeforeEnd = false;
        afterByOrSetOrFromOrSelect = "by".equals(lcToken)
                || "set".equals(lcToken)
                || "from".equals(lcToken);
    }

    private void beginNewClause() {
        if (!afterBeginBeforeEnd) {
            if (afterOn) {
                indent--;
                afterOn = false;
            }
            indent--;
            newline();
        }
        out();
        beginLine = false;
        afterBeginBeforeEnd = true;
    }

    private void values() {
        indent--;
        newline();
        out();
        indent++;
        newline();
        afterValues = true;
    }

    private void closeParen() {
        parensSinceSelect--;
        if (parensSinceSelect < 0) {
            indent--;
            parensSinceSelect = parenCounts.removeLast();
            afterByOrSetOrFromOrSelect = afterByOrFromOrSelects.removeLast();
        }
        if (inFunction > 0) {
            inFunction--;
            out();
        } else {
            if (!afterByOrSetOrFromOrSelect) {
                indent--;
                newline();
            }
            out();
        }
        beginLine = false;
    }

    private void openParen() {
        if (isFunctionName(lastToken) || inFunction > 0) {
            inFunction++;
        }
        beginLine = false;
        if (inFunction > 0) {
            out();
        } else {
            out();
            if (!afterByOrSetOrFromOrSelect) {
                indent++;
                newline();
                beginLine = true;
            }
        }
        parensSinceSelect++;
    }

    private static boolean isFunctionName(String tok) {
        final char begin = tok.charAt(0);
        final boolean isIdentifier = Character.isJavaIdentifierStart(begin) || '"' == begin;
        return isIdentifier &&
                !LOGICAL.contains(tok) &&
                !END_CLAUSES.contains(tok) &&
                !QUANTIFIERS.contains(tok) &&
                !DML.contains(tok) &&
                !MISC.contains(tok);
    }

    private static boolean isWhitespace(String token) {
        return WHITESPACE.contains(token);
    }

    private void newline() {
        result.append("\n");
        for (int i = 0; i < indent; i++) {
            result.append(indentString);
        }
        beginLine = true;
    }


    public static String format(String sql) {
        try {
            return new SQLFormatter(sql).format();
        } catch (Throwable t) {
            return sql;
        }
    }
}
