/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import javax.swing.*;
import java.awt.*;
import java.io.*;
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

//TODO Clean up Class to allow for easier gui code readability
class MainGUI {
    private FATFS Fatfs;
    private String Buffer;
    private String BufferPath;

    //GUI Construction
    private JFrame Frame;
    private JTextArea[] BlockAreas;
    private JComboBox<String> FileInfoArea;

    void showMainGUI() {
        //Setup and init for first window
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        Frame = new JFrame("FATFS");
        Frame.setLayout(new GridLayout());
        Frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JButton FATFSButton = new JButton("Create New FATFS Instance");
        JTextField allocationField = new JTextField("256");
        JTextField capacityField = new JTextField("256");
        JLabel allocationLabel = new JLabel("Allocation Unit Size: ");
        JLabel capacityLabel = new JLabel("Block Capacity: ");

        //Add to Frame
        Frame.add(allocationLabel);
        Frame.add(allocationField);
        Frame.add(capacityLabel);
        Frame.add(capacityField);
        Frame.add(FATFSButton);

        Frame.pack();

        FATFSButton.addActionListener(e -> showFATFS(Integer.parseInt(allocationField.getText()), Integer.parseInt(capacityField.getText())));

        //final line
        Frame.setVisible(true);

    }

    private void showFATFS(int allocationUnitSize, int blockCapacity) {
        //TODO Show BlockInfo
        //Clear previous Frame
        Frame.getContentPane().removeAll();
        Fatfs = new FATFS(allocationUnitSize, blockCapacity);

        //Setup and init
        JButton openFile = new JButton("Read File into Buffer");
        JButton writeBuffer = new JButton("Write Buffer to FATFS");
        JButton deleteFile = new JButton("Delete Selected File");
        JButton fileWriteButton = new JButton("Write from FATFS to file");
        JPanel boundingPanel = new JPanel();
        BlockAreas = new JTextArea[blockCapacity];
        JPanel blockAreaPanel = new JPanel(new GridLayout(24, 12, 2, 2));
        FileInfoArea = new JComboBox<>();
        JTextArea blockInfoArea = new JTextArea("Test");

        //Add to components/frames
        Frame.getContentPane().setLayout(new FlowLayout());
        Frame.getContentPane().add(openFile);
        Frame.getContentPane().add(writeBuffer);
        Frame.getContentPane().add(FileInfoArea);
        Frame.getContentPane().add(deleteFile);
        Frame.getContentPane().add(fileWriteButton);
        boundingPanel.add(new JLabel("Block Info"));
        boundingPanel.add(blockInfoArea);
        Frame.getContentPane().add(boundingPanel);
        Frame.getContentPane().add(blockAreaPanel);

        //Create and add block areas
        for (int i = 0; i < blockCapacity; i++) {
            BlockAreas[i] = new JTextArea(2, allocationUnitSize / 50);
            BlockAreas[i].setEditable(false);
            blockAreaPanel.add(new JLabel("Block " + i));
            blockAreaPanel.add(BlockAreas[i]);
        }

        //Resize
        boundingPanel.setLayout(new GridLayout(5, 2));
        boundingPanel.setPreferredSize(new Dimension(5, 5));
        Dimension frameDimension = new Dimension();
        frameDimension.setSize(Frame.getContentPane().getPreferredSize().getWidth(), Frame.getContentPane().getPreferredSize().getHeight() + (100 * (blockCapacity / 256)));
        Frame.setSize(frameDimension);
        blockInfoArea.setPreferredSize(new Dimension(100, Frame.getHeight()));
        Frame.revalidate();

        //Listeners
        openFile.addActionListener(e -> openFile());
        writeBuffer.addActionListener(e -> writeBufferToFATFS());
        FileInfoArea.addActionListener(e -> deleteFile.setEnabled(true));
        deleteFile.addActionListener(e -> deleteFile());
        fileWriteButton.addActionListener(e -> saveFile());

        //final line
        Frame.setVisible(true);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        File file = null;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        Buffer = ControllerUtilities.readFile(file);
        BufferPath = file != null ? file.getName() : null;
    }

    private boolean saveFile() {
        String fileString;
        if (Fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        if (Objects.equals(FileInfoArea.getSelectedItem(), "")) {
            System.err.println("No File selected for saving!");
            return false;
        } else {
            fileString = Fatfs.read((String) FileInfoArea.getSelectedItem());
        }
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String savePath;
            try {
                savePath = fileChooser.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            ControllerUtilities.writeFile(savePath, fileString);
        }
        return true;
    }

    private void writeBufferToFATFS() {
        if (Fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        if (Objects.equals(Buffer, "")) {
            System.err.println("Buffer is empty!");
        }
        Fatfs.write(BufferPath, Buffer);
        updateFrame();
    }

    private void deleteFile() {
        if (Fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        if (Objects.equals(FileInfoArea.getSelectedItem(), "")) {
            System.err.println("No File selected for deletion!");
        } else {
            Fatfs.drop((String) FileInfoArea.getSelectedItem());
        }
        updateFrame();
    }


    private void updateFrame() {
        if (Fatfs == null) {
            throw new RuntimeException("FATFS has not been initialized!");
        }
        int blocksUsed = Fatfs.getBlocksUsed();
        String[] blocks = Fatfs.getBlocks();
        FileInfo[] filesInfos = Fatfs.getFileInfos();
        for (int i = 0; i < blocksUsed; i++) {
            if (blocks[i].length() > 10) {
                if (blocks[i] == null) continue;
                BlockAreas[i].setText("..." + blocks[i].substring(blocks[i].length() - 10, blocks[i].length() - 1));
            } else BlockAreas[i].setText(blocks[i]);
        }
        FileInfoArea.removeAllItems();
        for (FileInfo info :
                filesInfos) {
            if (info == null) continue;
            FileInfoArea.addItem(info.getFilename());
        }
        Frame.revalidate();
    }
}

class ControllerUtilities {

    static String readFile(File file) {
        String out = "";

        try {
            byte[] buffer = new byte[(int) file.length()];

            FileInputStream inputStream =
                    new FileInputStream(file);
            while (inputStream.read(buffer) != -1) {
                out += new String(buffer);
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

/*    static void getResourceUsage() {
        Runtime runtime = Runtime.getRuntime();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory: ").append(humanReadableByteCount(freeMemory, true)).append("\n");
        sb.append("allocated memory: ").append(humanReadableByteCount(allocatedMemory, true)).append("\n");
        sb.append("max memory: ").append(humanReadableByteCount(maxMemory, true)).append("\n");
        sb.append("total free memory: ").append(humanReadableByteCount(freeMemory + (maxMemory - allocatedMemory), true)).append("\n");
        System.out.println(sb);
    }*/

/*    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }*/
}