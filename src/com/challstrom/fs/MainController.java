/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import java.io.*;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
public class MainController {
    public static void main(String[] args) {

        String inputsam = readFile("7oldsamr.txt");
        String inputCV = readFile("cv.txt");

        //Num
        String numString = "";
        for (int i = 0; i < 300; i++) {
            numString += i;
        }

        FATFS filesystem = new FATFS();
        filesystem.write("numbers.txt", numString);
        filesystem.write("cv.txt", inputCV);
        filesystem.write("sam.txt", inputsam);
        System.out.println(filesystem);

        //Now Let's spit it back out
        writeFile("sam-OUTPUT.txt",filesystem.read("sam.txt"));
        writeFile("cv-OUTPUT.txt",filesystem.read("cv.txt"));
        writeFile("numbers-OUTPUT.txt",filesystem.read("numbers.txt"));
    }



    private static String readFile(String fileName) {
        String out = "";

        try {
            byte[] buffer = new byte[100000];

            FileInputStream inputStream =
                    new FileInputStream(fileName);
            while (inputStream.read(buffer) != -1) {
                out+=new String(buffer);
            }
            inputStream.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return out;
    }

    private static void writeFile(String filename, String data) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

