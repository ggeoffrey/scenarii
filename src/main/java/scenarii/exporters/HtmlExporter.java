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
 * Export a scenario to an HTML file.
 */
public class HtmlExporter {

    // Where to export
    private final String targetFolder;

    // How to export
    private final JadeConfiguration config;

    // What should it look like
    private JadeTemplate template;

    public HtmlExporter(String targetFolder) {
        this.targetFolder = targetFolder;
        config = new JadeConfiguration();
        config.setPrettyPrint(true);
        config.setMode(Jade4J.Mode.XHTML);

        config.setTemplateLoader(new FileTemplateLoader("/res/", "UTF-8"));
        try {
            this.template = config.getTemplate("scenario");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Export a scenario to the given folder/name
     * @param scenario Scenario to export
     * @param progressIndicator Gui element to animate.
     */
    public void export(Scenario scenario, ProgressIndicator progressIndicator) {

        // How many steps does an export take ?
        int steps = 3 + 1 + scenario.getSteps().size();
        progressIndicator.setProgress(percent(0,steps));

        // ensure directory exists
        mkdir(targetFolder);

        // first step done
        progressIndicator.setProgress(percent(1,steps));

        // Create the target folder name
        final String name = scenario.getTitle().replaceAll("\\s","_");
        final String path = targetFolder+"/"+name+"/";

        // make it
        mkdir(path);

        // step 2 done
        progressIndicator.setProgress(percent(2,steps));

        // Prepare images export
        final String resFolder = path + "sc-images/";
        mkdir(resFolder);

        // step 3 done
        progressIndicator.setProgress(percent(3,steps));


        String rendered = config.renderTemplate(template, scenario.getJadeModel());
        BufferedWriter writer = null;
        try {
            // Create target file
            writer = new BufferedWriter(new FileWriter(path + name + ".html"));
            // Write html into it.
            writer.write(rendered);

            // Step 4 done.
            progressIndicator.setProgress(percent(4,steps));

            int currentStep = 4;
            for (Step s : scenario.getSteps()){
                File image = s.getImage();
                if(image != null){
                    try {
                        // Copy temps image to target/resources folder.
                        Files.copy(
                                Paths.get(image.getPath().replace("file:","")),
                                Paths.get(new File(resFolder+image.getName()).getPath())
                        );
                    }
                    catch (FileAlreadyExistsException alreadyExists){
                        System.err.println("File already exist. Ignoring.");
                    }
                }
                currentStep++;
                progressIndicator.setProgress(percent(currentStep,steps));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                // Cleanly close the writer if possible.
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
