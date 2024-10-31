package org.example;

import static org.example.ConfigToTomlConverter.runner;

public class Main {
    public static void main(String[] args) {
        String[] s = new String[2];
        s[0]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\input.txt";
        s[1]="C:\\Users\\nazar\\OneDrive\\Desktop\\study\\thirdConfig\\src\\main\\resources\\output.toml";
        runner(s);
    }
}