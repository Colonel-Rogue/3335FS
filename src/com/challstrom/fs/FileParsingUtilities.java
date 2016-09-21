/*
 * Copyright (c) 2016. Challstrom. All Rights Reserved.
 */

package com.challstrom.fs;

/**
 * Created by tchallst on 9/14/2016.
 * 3335FS / com.challstrom.fs
 */
class FileParsingUtilities {
    static String[] covertToBlocks(int allocation_unit_size, String inputString) {
        int size = (inputString.length() / allocation_unit_size) + (inputString.length() % allocation_unit_size > 0 ? 1 : 0);
        String[] output = new String[size];
        for (int i = 0; i < size; i++) {
            output[i] = inputString.substring((i * allocation_unit_size), (i + 1) * allocation_unit_size < inputString.length() ? (i + 1) * allocation_unit_size : inputString.length());
        }
        //if(!isValidBlockStructure(output)) throw new RuntimeException("Block Structure Invalid!");
        return output;
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
