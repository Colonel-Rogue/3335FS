/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import com.sun.istack.internal.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.Objects;

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
    private String buffer;
    private String bufferPath;

    //GUI Construction
    private JFrame frame;
    private JTextArea[] blockAreas;
    private JComboBox<String> FileInfoArea;

    void showMainGUI() {
        //Setup and init for first window
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("FATFS");
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JButton FATFSButton = new JButton("Create New FATFS Instance");
        JTextField allocationField = new JTextField("256");
        JTextField capacityField = new JTextField("256");
        JLabel allocationLabel = new JLabel("Allocation Unit Size: ");
        JLabel capacityLabel = new JLabel("Block Capacity: ");

        //Add to frame
        frame.add(allocationLabel);
        frame.add(allocationField);
        frame.add(capacityLabel);
        frame.add(capacityField);
        frame.add(FATFSButton);

        frame.pack();

        FATFSButton.addActionListener(e -> showFATFS(Integer.parseInt(allocationField.getText()), Integer.parseInt(capacityField.getText())));

        //final line
        frame.setVisible(true);

    }

    private void showFATFS(int allocationUnitSize, int blockCapacity) {
        //Clear previous frame
        frame.getContentPane().removeAll();
        System.out.println(allocationUnitSize);
        System.out.println(blockCapacity);
        fatfs = new FATFS(allocationUnitSize, blockCapacity);

        //Setup and init
        JButton openFile = new JButton("Read File into buffer");
        JButton writeBuffer = new JButton("Write Buffer to FATFS");
        JButton deleteFile = new JButton("Delete Selected File");
        blockAreas = new JTextArea[blockCapacity];
        JPanel blockAreaPanel = new JPanel(new GridLayout(24, 12, 2, 2));
        FileInfoArea = new JComboBox<>();

        //Add to components/frames
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(openFile);
        frame.getContentPane().add(writeBuffer);
        frame.getContentPane().add(FileInfoArea);
        frame.getContentPane().add(deleteFile);
        frame.getContentPane().add(blockAreaPanel);

        //Create and add block areas
        for (int i = 0; i < blockCapacity; i++) {
            blockAreas[i] = new JTextArea(2, allocationUnitSize / 50);
            blockAreas[i].setEditable(false);
            blockAreaPanel.add(new JLabel("Block " + i));
            blockAreaPanel.add(blockAreas[i]);
        }

        //Resize
        Dimension frameDimension = new Dimension();
        frameDimension.setSize(frame.getContentPane().getPreferredSize().getWidth(), frame.getContentPane().getPreferredSize().getHeight() + (100 * (blockCapacity / 256)));
        frame.setSize(frameDimension);
        frame.revalidate();

        //Listeners
        openFile.addActionListener(e -> openFile());
        writeBuffer.addActionListener(e -> writeBufferToFATFS());
        FileInfoArea.addActionListener(e -> deleteFile.setEnabled(true));
        deleteFile.addActionListener(e -> deleteFile());

        //final line
        frame.setVisible(true);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        File file = null;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        buffer = ControllerUtilities.readFile(file);
        bufferPath = file != null ? file.getName() : null;
    }

    private void writeBufferToFATFS() {
        if (fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        if (Objects.equals(buffer, "")) {
            System.err.println("Buffer is empty!");
        }
        fatfs.write(bufferPath, buffer);
        updateFrame();
    }

    private void deleteFile() {
        if (fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        if (Objects.equals(FileInfoArea.getSelectedItem(), "")) {
            System.err.println("No File selected for deletion!");
        } else {
            fatfs.drop((String) FileInfoArea.getSelectedItem());
        }
        updateFrame();
    }


    private void updateFrame() {
        if (fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        int blocksUsed = fatfs.getBlocksUsed();
        String[] blocks = fatfs.getBlocks();
        FileInfo[] filesInfos = fatfs.getFileInfos();
        for (int i = 0; i < blocksUsed; i++) {
            if (blocks[i].length() > 10) {
                if (blocks[i] == null) continue;
                blockAreas[i].setText("..." + blocks[i].substring(blocks[i].length() - 10, blocks[i].length() - 1));
            } else blockAreas[i].setText(blocks[i]);
        }
        FileInfoArea.removeAllItems();
        for (FileInfo info :
                filesInfos) {
            if (info == null) continue;
            FileInfoArea.addItem(info.getFilename());
        }
        frame.revalidate();
    }
}

class ControllerUtilities {

    @NotNull
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