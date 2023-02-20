package com.example.webmagic.util;

import java.io.*;

public class execStream extends Thread {
    private InputStream is;
    private String type;
    private OutputStream os;

    public execStream(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public execStream(InputStream is, String type, OutputStream os) {
        this.is = is;
        this.type = type;
        this.os = os;
    }

    @Override
    public  void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            if (os != null){
                pw = new PrintWriter(os);
            }

            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                if (pw != null) {
                    pw.println(line);
                }
            }

            if (pw != null) {
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (pw!=null){
                    pw.close();
                }
                if (br!=null){
                    br.close();
                }
                if (isr!=null){
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
