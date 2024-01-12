package com.example.webmagic.util;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {

        int i = 1234;
        int p;
        while(i != 0){
            p = i%10;
            System.out.print(p);
            i /= 10;
        }
        System.out.println();
        System.out.println("OK");

    }

}
