package scenarii.exporters;

import java.io.File;
import java.io.IOException;

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
}
