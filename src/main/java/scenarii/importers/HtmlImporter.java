package scenarii.importers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scenarii.model.Step;
import scenarii.model.Scenario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by geoffrey on 26/05/2016.
 * Create a scenario form an HTML file.
 */
public class HtmlImporter {

    public static Scenario load(File htmlFile){
        Scenario sc = null;
        try {
            Document dom = Jsoup.parse(htmlFile,"UTF-8");

            // get title text
            String title = dom.select(".title").first().text();

            // get author text
            String author = dom.select(".author").first().text();

            // get description text and reconstruct line-breaks
            String description = dom
                    .select(".description-wrapper .raw-description")
                    .first()
                    .text()
                    .replaceAll("\\$br","\n");

            // get data text and reconstruct line-breaks
            String data = dom.select(".raw-data").first().text().replaceAll("\\$br","\n");

            // Reconstruct steps
            ArrayList<Step> steps = new ArrayList<>();
            Elements domSteps = dom.select(".step");
            int i = 1;
            for(Element e : domSteps){
                Step s = new Step();
                s.setPosition(i);
                s.setImage(htmlFile.getParentFile().getPath()+"/"+e.select(".gif").attr("src"));
                String stepDescription = e.select(".raw-description").first().text().replaceAll("\\$br","\n");
                s.setDescription(stepDescription);
                steps.add(s);
                i++;
            }

            // Prepare a new Scenario to return.
            sc = new Scenario(title,author,description,data,steps);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sc;
    }
}
