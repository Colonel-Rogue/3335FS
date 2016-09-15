/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
public class FATFS {
    public static final int ALLOCATION_UNIT_SIZE = 256;
    public static final int BLOCK_CAPACITY = 256;

    private FileInfo[] FileInfos = new FileInfo[BLOCK_CAPACITY];
    private int[] BlockInfo = new int[BLOCK_CAPACITY];
    private String[] Blocks = new String[BLOCK_CAPACITY];
    private int BlocksAvailable = BLOCK_CAPACITY;

    boolean write(String filename, String inputString) {
        String[] inputBlocks= FileParsingUtilities.covertToBlocks(inputString);
        int next = getNextAvailableBlock(-1);
        int firstBlock = next;
        for (int i = 0; i < inputBlocks.length; i++) {
             if(next < 0) {
                System.err.println("ERROR! No Blocks Available! Please use a smaller file, or increase the Block Capacity!");
                return false;
            }
            Blocks[next] = inputBlocks[i];
            BlockInfo[next] = getNextAvailableBlock(next);
            next = getNextAvailableBlock(next);
            BlocksAvailable--;
        }
        BlockInfo[next] = -1;
        //TODO For some reason the last block isn't writing
        FileInfo fileInfo = new FileInfo(filename, firstBlock);
        FileInfos[firstBlock] = fileInfo;
        return true;
    }

    String read(String filename) {
        if (!Arrays.asList(FileInfos).contains(new FileInfo(filename, -1))) {
            System.err.println("ERROR! The File "+filename+" does not exist!");
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
        switch (blockInfoIndex){
            case -2:
                System.err.println("CORRUPTED BLOCK AT: "+blockInfoIndex);
                return false;
            case -1:
                return true;
            default:
                blocks[depth] = this.Blocks[blockInfoIndex];
                if (this.BlockInfo[blockInfoIndex]<0) {
                    blocks[depth+1] = this.Blocks[blockInfoIndex];
                    return true;
                }
                return getBlock(this.BlockInfo[blockInfoIndex], blocks, depth+1);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FATFS{"+"\n\t");
        sb.append("FileInfos=").append(Arrays.toString(FileInfos)+"");
        sb.append(", \n\tBlockInfo=").append(Arrays.toString(BlockInfo));
        sb.append(", \n\tBlocks=").append(Arrays.toString(Blocks));
        sb.append(", \n\tBlocksAvailable=").append(BlocksAvailable);
        sb.append("\n}");
        return sb.toString();
    }
}

class FileInfo {
    String filename;
    int blockStart;

    public FileInfo(String filename, int blockStart) {
        this.filename = filename;
        this.blockStart = blockStart;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileInfo{");
        sb.append("filename='").append(filename).append('\'');
        sb.append(", blockStart=").append(blockStart);
        sb.append('}');
        return sb.toString();
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
}
