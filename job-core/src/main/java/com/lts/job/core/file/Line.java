package com.lts.job.core.file;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 * 文件中的一行
 */
public class Line {
    private String line;

    public Line(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return line;
    }
}
