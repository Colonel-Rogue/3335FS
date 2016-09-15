/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
public class FileParsingUtilities {
    static String[] covertToBlocks(String inputString) {
        int size = (inputString.length()/FATFS.ALLOCATION_UNIT_SIZE)+(inputString.length()%FATFS.ALLOCATION_UNIT_SIZE > 0 ? 1: 0);
        String[] output = new String[size];
        for (int i = 0; i < size; i++) {
            output[i] = inputString.substring((i*FATFS.ALLOCATION_UNIT_SIZE),(i+1)*FATFS.ALLOCATION_UNIT_SIZE < inputString.length() ? (i+1)*FATFS.ALLOCATION_UNIT_SIZE : inputString.length());
        }
        //if(!isValidBlockStructure(output)) throw new RuntimeException("Block Structure Invalid!");
        return output;
    }

    static boolean isValidBlockStructure(String[] inputBlocks) {
        for (String block :
                inputBlocks) {
            if (block.length() > FATFS.ALLOCATION_UNIT_SIZE) {
                return false;
            }
        }
        return true;
    }
}
