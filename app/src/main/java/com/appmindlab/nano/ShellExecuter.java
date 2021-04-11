package com.appmindlab.nano;

/**
 * Created by saelim on 1/6/2016.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellExecuter {
    public ShellExecuter() {}

    public String Executer(String command) {
        StringBuffer output = new StringBuffer();
        Process p;

        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                output.append(line + Const.NEWLINE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();
    }
}
