package com.kdm1t.digsig.Tools;

import java.io.FileOutputStream;

public class FileTools {

    public static void writeFile(String absolutePath, byte[] toWrite) {
        try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
            fos.write(toWrite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
