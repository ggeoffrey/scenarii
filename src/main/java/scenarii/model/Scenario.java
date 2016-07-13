package scenarii.model;

import org.pegdown.PegDownProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by geoffrey on 24/05/2016.
 * Describe a scenario.
 */
public class Scenario {

    // Markdown processor
    private final PegDownProcessor parser;

    private String title;
    private String author;
    private String description;
    private String data;
    private ArrayList<Step> rawSteps;

    // Jade format
    private final List<Map<String,Object>> steps;


    public Scenario(String title, String author, String description, String data, ArrayList<Step> steps) {
        parser = new PegDownProcessor();

        this.title = title;
        this.author = author;
        this.description = description;
        this.data = data;
        this.rawSteps = steps;
        this.steps = stepsToJadeModel(steps);
    }

    /**
     * Transform a Scenario to a jade compatible model.
     * @return a generic map
     */
    public Map<String, Object> getJadeModel(){
        Map<String, Object> model = new HashMap<>();

        model.put("title",title);

        model.put("author", author);
        model.put("rawDescription", description.replaceAll("\\n","\\$br"));
        model.put("description", parser.markdownToHtml(description));
        model.put("rawData", data.replaceAll("\\n","\\$br"));
        model.put("data", parser.markdownToHtml(data));
        model.put("steps", steps);

        return model;
    }


    /**
     * Transforms steps to a jade model
     * @param steps
     * @return a generic list
     */
    private LinkedList<Map<String,Object>> stepsToJadeModel(List<Step> steps){
        return steps.stream()
                .map(s -> s.toJadeModel(parser))
                .collect(Collectors.toCollection(LinkedList::new));
    }


    public String getTitle() {
        return title;
    }


    // -------


    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ArrayList<Step> getSteps() {
        return rawSteps;
    }

    public void setSteps(ArrayList<Step> rawSteps) {
        this.rawSteps = rawSteps;
    }
}
