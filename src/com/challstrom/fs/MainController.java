/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
public class MainController {
    public static void main(String[] args) {
        MainGUI mainGUI = new MainGUI();
        mainGUI.showMainGUI();
    }


}

class MainGUI {
    private FATFS fatfs;
    private JFrame frame;
    private String buffer;

    void showMainGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("FATFS");
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JButton FATFSButton = new JButton("Create New FATFS Instance");
        JTextField allocationField = new JTextField("256");
        JTextField capacityField = new JTextField("256");
        JLabel allocationLabel = new JLabel("Allocation Unit Size: ");
        JLabel capacityLabel = new JLabel("Block Capacity: ");

        frame.add(allocationLabel);
        frame.add(allocationField);
        frame.add(capacityLabel);
        frame.add(capacityField);
        frame.add(FATFSButton);
        frame.pack();

        FATFSButton.addActionListener(e -> showFATFS(Integer.parseInt(allocationField.getText()), Integer.parseInt(capacityField.getText())));

        frame.setVisible(true);

    }

    private void showFATFS(int allocationUnitSize, int blockCapacity) {
        frame.getContentPane().removeAll();
        System.out.println(allocationUnitSize);
        System.out.println(blockCapacity);
        fatfs = new FATFS(allocationUnitSize, blockCapacity);

        JButton openFile = new JButton("Read File into buffer");
        JButton writeBuffer = new JButton("Write Buffer to FATFS");


        frame.getContentPane().add(openFile);
        frame.getContentPane().add(writeBuffer);
        frame.setVisible(true);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        File file = null;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        ControllerUtilities.readFile(file);
    }
}

class ControllerUtilities {
    static String readFile(File file) {
        String line = null;
        String out = "";

        try {
            byte[] buffer = new byte[(int) file.length()];

            FileInputStream inputStream =
                    new FileInputStream(file);
            int total = 0;
            int nRead;
            while ((nRead = inputStream.read(buffer)) != -1) {
                out += new String(buffer);
                total += nRead;
            }
            inputStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" + file.getName() + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '" + file.getName() + "'");
        }
        return out;
    }

    static void writeFile(String filename, String data) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void getResourceUsage() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory: ").append(humanReadableByteCount(freeMemory, true)).append("\n");
        sb.append("allocated memory: ").append(humanReadableByteCount(allocatedMemory, true)).append("\n");
        sb.append("max memory: ").append(humanReadableByteCount(maxMemory, true)).append("\n");
        sb.append("total free memory: ").append(humanReadableByteCount(freeMemory + (maxMemory - allocatedMemory), true)).append("\n");
        System.out.println(sb);
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}