package scenarii.exporters;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class HtmlExporter {


    private String targetFolder;
    private JadeConfiguration config;
    private JadeTemplate template;

    public HtmlExporter(String targetFolder) {
        this.targetFolder = targetFolder;
        config = new JadeConfiguration();
        config.setPrettyPrint(true);
        config.setMode(Jade4J.Mode.XHTML);
        config.setTemplateLoader(new FileTemplateLoader(
                getClass()
                .getResource("/res/")
                        .toString().replace("file:",""),
                "UTF-8"));

        try {
            template = config.getTemplate("scenario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(Scenario scenario){
        String rendered = config.renderTemplate(template, scenario.getJadeModel());
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(targetFolder+"scenario.html"));
            writer.write(rendered);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
