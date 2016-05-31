package scenarii.exporters.jade4j;

/**
 * Created by geoffrey on 31/05/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.*;

import de.neuland.jade4j.template.TemplateLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileTemplateLoader implements TemplateLoader {
    private String encoding = "UTF-8";
    private String basePath = "";

    public FileTemplateLoader(String basePath, String encoding) {
        this.basePath = basePath;
        this.encoding = encoding;
    }

    public long getLastModified(String name) {
        File templateSource = new File(this.getFile(name));
        return templateSource.lastModified();
    }

    public Reader getReader(String name) throws IOException {
        //File templateSource = this.getFile(name);
        InputStream templateSource = getClass().getResourceAsStream(this.getFile(name));
        return new InputStreamReader(templateSource, this.encoding);
    }

    private String getFile(String name) {
        return this.basePath + name;
    }

    /*
    private File getFile(String name) {
        return new File(getClass().getResource(this.basePath + name));
    }
    */
}

