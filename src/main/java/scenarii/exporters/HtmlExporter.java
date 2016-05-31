package scenarii.exporters;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import javafx.scene.control.ProgressIndicator;
import scenarii.exporters.jade4j.FileTemplateLoader;
import scenarii.model.Step;
import scenarii.model.Scenario;

import java.io.*;
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
        String path = FileUtils.getCleanFilePath("/res/");
        //String cssPath = FileUtils.getCleanFilePath("/res/scenario.css");
        //String jsPath = FileUtils.getCleanFilePath("/res/scenario.js");

        //String template = new StringReader(new File(path));
        /*config.setTemplateLoader(new FileTemplateLoader(
                getClass()
                .getResource("/res/")
                        .toString().replace("file:","")
                                    .replace("jar:",""),
                "UTF-8"));

        */
        config.setTemplateLoader(new FileTemplateLoader("/res/", "UTF-8"));
        try {
            //String templateText = FileUtils.readFileAsString(path);
            //String styleText = FileUtils.readFileAsString(cssPath);
            //String jsText = FileUtils.readFileAsString(jsPath);

            //TemplateLoader loader = new ReaderTemplateLoader(new StringReader(templateText), "scenario.jade");

            //config.setTemplateLoader(loader);
            this.template = config.getTemplate("scenario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void export(Scenario scenario, ProgressIndicator progressIndicator) {

        int steps = 3 + 1 + scenario.getSteps().size();
        progressIndicator.setProgress(percent(0,steps));

        mkdir(targetFolder);

        progressIndicator.setProgress(percent(1,steps));

        final String name = scenario.getTitle().replaceAll("\\s","_");
        final String path = targetFolder+"/"+name+"/";

        mkdir(path);
        progressIndicator.setProgress(percent(2,steps));

        final String resFolder  = path+"sc-images/";
        mkdir(resFolder);
        progressIndicator.setProgress(percent(3,steps));


        String rendered = config.renderTemplate(template, scenario.getJadeModel());
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path + name + ".html"));
            writer.write(rendered);

            progressIndicator.setProgress(percent(4,steps));
            int currentStep = 4;
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
                currentStep++;
                progressIndicator.setProgress(percent(currentStep,steps));
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



    private double percent(int pos, int goal){
        return (double) ((pos / goal) * pos);
    }
}
