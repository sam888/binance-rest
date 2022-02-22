package com.orderbook.rest.util;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Util class to read file as string in class path, e.g. file put in /resources folder.
 */
public final class ResourcesDataUtil {

    private static ClassLoader classLoader =  ResourcesDataUtil.class.getClassLoader();

    public static String getFileAsString(String folderName, String fileName) throws Exception {
        String filePath = getFilePath( folderName, fileName );
        return Files.lines( Paths.get( filePath )).collect( Collectors.joining( System.lineSeparator() ) );
    }

    public static String getFilePath(String folderName, String fileName) throws URISyntaxException {
        File dataFile = new File( classLoader.getResource(folderName + "/" + fileName).toURI());
        return dataFile.getAbsolutePath();
    }
}
