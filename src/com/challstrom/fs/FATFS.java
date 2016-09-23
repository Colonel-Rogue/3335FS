/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
class FATFS {
    private int ALLOCATION_UNIT_SIZE;
    private int BLOCK_CAPACITY;

    private FileInfo[] FileInfos;
    private int[] BlockInfo;
    private String[] Blocks;
    private int BlocksAvailable;

    FATFS(int ALLOCATION_UNIT_SIZE, int BLOCK_CAPACITY) {
        this.ALLOCATION_UNIT_SIZE = ALLOCATION_UNIT_SIZE;
        this.BLOCK_CAPACITY = BLOCK_CAPACITY;
        FileInfos = new FileInfo[BLOCK_CAPACITY];
        BlockInfo = new int[BLOCK_CAPACITY];
        Blocks = new String[BLOCK_CAPACITY];
        BlocksAvailable = BLOCK_CAPACITY;
    }

    boolean write(String filename, String inputString) {
        String[] inputBlocks = FilesystemUtilities.covertToBlocks(ALLOCATION_UNIT_SIZE, inputString);
        int next = getNextAvailableBlock(-1);
        int firstBlock = next;
        int last = -1;
        for (String inputBlock : inputBlocks) {
            if (next < 0) {
                System.err.println("ERROR! No Blocks Available! Please use a smaller file, or increase the Block Capacity!");
                return false;
            }
            Blocks[next] = inputBlock;
            BlockInfo[next] = getNextAvailableBlock(next);
            last = next;
            next = getNextAvailableBlock(next);
            BlocksAvailable--;
        }
        BlockInfo[last] = -1;
        FileInfo fileInfo = new FileInfo(filename, firstBlock);
        FileInfos[firstBlock] = fileInfo;
        return true;
    }

    String read(String filename) {
        if (!Arrays.asList(FileInfos).contains(new FileInfo(filename, -1))) {
            System.err.println("ERROR! The File " + filename + " does not exist!");
            return null;
        }
        String output = "";
        int fileInfoIndex = Arrays.asList(FileInfos).indexOf(new FileInfo(filename, -1));
        assert fileInfoIndex > 0;
        FileInfo fileInfo = FileInfos[fileInfoIndex];
        String[] blocks = new String[BLOCK_CAPACITY];
        if (!getBlock(fileInfo.blockStart, blocks, 0)) {
            System.err.println("Could not read entire file! Some parts may be corrupted!");
        }
        int i = 0;
        do {
            output += blocks[i];
            i++;
        } while (blocks[i] != null);
        return output;
    }

    boolean drop(String filename) {
        int fileInfoIndex = FilesystemUtilities.fseek(filename, FileInfos);
        if (fileInfoIndex < 0) {
            System.err.println("No file '" + filename + "' found!");
            return false;
        }
        boolean dropped = freeBlock(FileInfos[fileInfoIndex].getBlockStart());
        FileInfos[fileInfoIndex] = null;
        return dropped;
    }

    private boolean freeBlock(int blockNumber) {
        if (blockNumber < 0) return true;
        int next = BlockInfo[blockNumber];
        BlockInfo[blockNumber] = 0;
        BlocksAvailable++;
        return freeBlock(next);
    }

    private int getNextAvailableBlock(int last) {
        for (int i = 0; i < BlockInfo.length; i++) {
            int info = BlockInfo[i];
            if (i == last) continue;
            if (info == -2) {
                System.err.println("CORRUPTED BLOCK AT: " + info);
            } else if (info == 0) {
                return i;
            }
        }
        return -1;
    }

    private boolean getBlock(int blockInfoIndex, String[] blocks, int depth) {
        switch (blockInfoIndex) {
            case -2:
                System.err.println("CORRUPTED BLOCK AT: " + blockInfoIndex);
                return false;
            case -1:
                return true;
            default:
                blocks[depth] = this.Blocks[blockInfoIndex];
                if (this.BlockInfo[blockInfoIndex] < 0) {
                    blocks[depth + 1] = this.Blocks[blockInfoIndex];
                    return true;
                }
                return getBlock(this.BlockInfo[blockInfoIndex], blocks, depth + 1);
        }
    }

    public FileInfo[] getFileInfos() {
        return FileInfos;
    }

    public int[] getBlockInfo() {
        return BlockInfo;
    }

    public String[] getBlocks() {
        return Blocks;
    }

    public int getALLOCATION_UNIT_SIZE() {
        return ALLOCATION_UNIT_SIZE;
    }

    public int getBLOCK_CAPACITY() {
        return BLOCK_CAPACITY;
    }

    public int getBlocksUsed() {
        return ALLOCATION_UNIT_SIZE - BlocksAvailable;
    }

    @Override
    public String toString() {
        return "FATFS{" + "\n\t" + "FileInfos=" + Arrays.toString(FileInfos) + "" +
                ", \n\tBlockInfo=" + Arrays.toString(BlockInfo) +
                ", \n\tBlocks=" + Arrays.toString(Blocks) +
                ", \n\tBlocksAvailable=" + BlocksAvailable +
                "\n}";
    }
}

class FileInfo {
    int blockStart;
    private String filename;

    FileInfo(String filename, int blockStart) {
        this.filename = filename;
        this.blockStart = blockStart;
    }

    @Override
    public String toString() {
        return "FileInfo{" + "filename='" + filename + '\'' +
                ", blockStart=" + blockStart +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return filename.equals(fileInfo.filename);

    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    public int getBlockStart() {
        return blockStart;
    }

    String getFilename() {
        return filename;
    }
}

class FilesystemUtilities {
    static String[] covertToBlocks(int allocation_unit_size, String inputString) {
        int size = (inputString.length() / allocation_unit_size) + (inputString.length() % allocation_unit_size > 0 ? 1 : 0);
        String[] output = new String[size];
        for (int i = 0; i < size; i++) {
            output[i] = inputString.substring((i * allocation_unit_size), (i + 1) * allocation_unit_size < inputString.length() ? (i + 1) * allocation_unit_size : inputString.length());
        }
        //if(!isValidBlockStructure(output)) throw new RuntimeException("Block Structure Invalid!");
        return output;
    }

    @NotNull
    static int fseek(String filename, FileInfo[] fileInfos) {
        for (int i = 0; i < fileInfos.length; i++) {
            if (fileInfos[i] == null) continue;
            if (Objects.equals(fileInfos[i].getFilename(), filename)) return i;
        }
        return -1;
    }

/*    static boolean isValidBlockStructure((int allocation_unit_size, String[] inputBlocks) {
        for (String block :
                inputBlocks) {
            if (block.length() > allocation_unit_size) {
                return false;
            }
        }
        return true;
    }*/
}
