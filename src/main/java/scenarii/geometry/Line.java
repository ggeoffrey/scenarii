package scenarii.geometry;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.paint.Color;

/**
 * Created by geoffrey on 22/05/2016.
 * A simple line with some extra Properties that we can bind ond.
 */
public class Line extends javafx.scene.shape.Line {
    public Line() {
        super();
        setStroke(Color.RED);
        setStrokeWidth(2);
    }

    public void bindStartX(ReadOnlyDoubleProperty property){
        startXProperty().bind(property);
    }

    public void bindStartY(ReadOnlyDoubleProperty property){
        startYProperty().bind(property);
    }

    public void bindEndX(ReadOnlyDoubleProperty property){
        endXProperty().bind(property);
    }

    public void bindEndY(ReadOnlyDoubleProperty property){
        endYProperty().bind(property);
    }
}
