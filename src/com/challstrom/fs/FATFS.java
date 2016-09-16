/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

import java.util.Arrays;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
class FATFS {
    static final int ALLOCATION_UNIT_SIZE = 256;
    private static final int BLOCK_CAPACITY = 2048;

    private FileInfo[] FileInfos = new FileInfo[BLOCK_CAPACITY];
    private int[] BlockInfo = new int[BLOCK_CAPACITY];
    private String[] Blocks = new String[BLOCK_CAPACITY];
    private int BlocksAvailable = BLOCK_CAPACITY;

    boolean write(String filename, String inputString) {
        String[] inputBlocks = FileParsingUtilities.covertToBlocks(inputString);
        int next = getNextAvailableBlock(-1);
        int firstBlock = next;
        for (String inputBlock : inputBlocks) {
            if (next < 0) {
                System.err.println("ERROR! No Blocks Available! Please use a smaller file, or increase the Block Capacity!");
                return false;
            }
            Blocks[next] = inputBlock;
            BlockInfo[next] = getNextAvailableBlock(next);
            next = getNextAvailableBlock(next);
            BlocksAvailable--;
        }
        BlockInfo[next] = -1;
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
}
