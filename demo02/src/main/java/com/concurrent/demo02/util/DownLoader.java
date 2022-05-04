package com.concurrent.demo02.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ProgZhou
 * @createTime 2022/04/27
 */
public class DownLoader {

    public static List<String> download() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://www.baidu.com").openConnection();

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null){
            lines.add(line);
        }

        reader.close();
        return lines;
    }

}
