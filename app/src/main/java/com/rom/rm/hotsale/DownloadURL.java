package com.rom.rm.hotsale;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
//download file json từ url
public class DownloadURL {
    public String readUrl(String myUrl) throws IOException{
        String data="";
        InputStream inputStream=null;
        HttpURLConnection urlConnection=null;
        try {
            URL url= new URL(myUrl);
            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.connect();

            //Đọc dữ liệu từ URL
            inputStream=urlConnection.getInputStream();
            BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer= new StringBuffer();

            //Đọc từng dòng 1
            String line="";
            while ((line=reader.readLine())!=null){
                stringBuffer.append(line);
            }
            data=stringBuffer.toString();
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;

    }
}
