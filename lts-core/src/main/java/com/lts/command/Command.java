package com.lts.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Robert HG (254963746@qq.com) on 10/26/15.
 */
public class Command extends CommandRequest {

    private String result;

    public void proceedRequest(InputStream is) throws IOException {

        BufferedReader bufferedReader = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    public String getResult() {
        return result;
    }

}
