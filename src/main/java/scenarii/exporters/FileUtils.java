package scenarii.exporters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by geoffrey on 26/05/2016.
 */
public class FileUtils {
    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        if(file.contains("/"))
            return false;
        try {
            String s = f.getCanonicalPath();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }


    public static String readFileAsString(String path) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String getCleanFilePath(String relativePath){
        return FileUtils.class.getResource(relativePath)
                .toString()
                .replaceAll("file:","")
                .replaceAll("jar:","")
                .replaceAll("!/", "/")
                ;
    }

}
