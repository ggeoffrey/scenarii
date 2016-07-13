package scenarii.exporters.jade4j;

/**
 * Created by geoffrey on 31/05/2016.
 * Can load a Jade Template from within a jar/bundled application.
 */
//
// Source code recreated from a .class file by Intellij IDEA
// (powered by Fernflower decompiler)
//

import java.io.*;

import de.neuland.jade4j.template.TemplateLoader;

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
        InputStream templateSource = getClass().getResourceAsStream(this.getFile(name));
        return new InputStreamReader(templateSource, this.encoding);
    }

    private String getFile(String name) {
        return this.basePath + name;
    }

}

