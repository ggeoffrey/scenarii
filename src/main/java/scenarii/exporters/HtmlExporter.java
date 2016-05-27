package scenarii.exporters;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import scenarii.controllers.Step;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        mkdir(targetFolder);

        final String name = scenario.getTitle().replaceAll("\\s","_");
        final String path = targetFolder+"/"+name+"/";

        mkdir(path);
        final String resFolder  = path+"sc-images/";
        mkdir(resFolder);


        String rendered = config.renderTemplate(template, scenario.getJadeModel());
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path + name + ".html"));
            writer.write(rendered);

            for (Step s : scenario.getSteps()){
                File image = s.getImage();
                if(image != null){
                    try {
                        Files.copy(
                                Paths.get(image.getPath().replace("file:","")),
                                Paths.get(new File(resFolder+image.getName()).getPath())
                        );
                    }
                    catch (FileAlreadyExistsException alreadyExists){}
                }
            }

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



    private void mkdir(String path){
        mkdir(new File(path), false);
    }

    private void mkdir(String path, boolean overwrite){
        mkdir(new File(path), overwrite);
    }

    private void mkdir(File file, boolean overwrite){
        if(!file.exists()){
            file.mkdir();
        }
    }
}
