package scenarii.exporters;

import org.pegdown.PegDownProcessor;
import scenarii.controllers.Step;

import java.util.*;

/**
 * Created by geoffrey on 24/05/2016.
 */
public class Scenario {

    private PegDownProcessor parser;

    private String title;
    private String author;
    private String description;
    private String data;
    private List<Map<String,Object>> steps;


    public Scenario(String title, String author, String description, String data, ArrayList<Step> steps) {
        parser = new PegDownProcessor();

        this.title = title;
        this.author = author;
        this.description = description;
        this.data = data;
        this.steps = stepsToJadeModel(steps);
    }


    public Map<String, Object> getJadeModel(){
        Map<String, Object> model = new HashMap<>();

        model.put("title",title);

        model.put("author", author);
        model.put("description", parser.markdownToHtml(description));
        model.put("data", parser.markdownToHtml(data));
        model.put("steps", steps);

        return model;
    }


    private LinkedList<Map<String,Object>> stepsToJadeModel(List<Step> steps){
        LinkedList<Map<String,Object>> models = new LinkedList<>();
        for (Step s : steps){
            models.add(s.toJadeModel(parser));
        }
        return models;
    }
}
