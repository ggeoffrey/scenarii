package scenarii.overlay;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.mouse.NativeMouseEvent;
import scenarii.geometry.Line;

import java.util.function.Consumer;

/**
 * Created by geoffrey on 22/05/2016.
 */
public class OverlaySection extends Stage {

    private SectionOrientation displayMode;
    private AnchorPane root;

    public OverlaySection(SectionOrientation orientation) {
        super(StageStyle.TRANSPARENT);


        this.displayMode = orientation;



        root = new AnchorPane();
        makeOverlay(root);
        root.setBackground(null);



        Scene scene = new Scene(root, 150, 75);
        scene.setFill(null);
        this.setScene(scene);

        this.setAlwaysOnTop(true);
        this.initModality(Modality.APPLICATION_MODAL);

    }

    public void setPosition(NativeMouseEvent event){
        setPosition(event.getX(), event.getY());
    }

    public void setPosition(double x, double y){
        this.setX(x - this.getWidth()/2);
        if(displayMode == SectionOrientation.BOTTOM)
            this.setY(y + 0.5);
        else
            this.setY(y - this.getHeight() - 0.5);
    }

    private void makeOverlay(AnchorPane root){
        ReadOnlyDoubleProperty w = root.widthProperty();
        ReadOnlyDoubleProperty h = root.heightProperty();

        Line left  = new Line();
        left.bindEndY(h);

        Line right = new Line();
        right.bindStartX(w);
        right.bindEndX(w);
        right.bindEndY(h);

        Line center = new Line();
        if(displayMode == SectionOrientation.TOP){
            center.bindEndX(w);
        }
        else{
            center.bindStartY(h);
            center.bindEndX(w);
            center.bindEndY(h);
        }

        root.getChildren().addAll(left,right,center);
    }


    public void alterWidth(double value){
        double delta = this.getWidth() + value;
        if(delta >= 60){
            this.setWidth(delta);
        }
    }

    public void alterHeight(double value){
        double delta = this.getHeight() + value;
        if(delta >= 30){
            this.setHeight(delta);
        }
    }

}
