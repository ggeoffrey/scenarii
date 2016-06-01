package scenarii.overlay;

import javafx.application.Platform;
import java.lang.reflect.Method;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jnativehook.mouse.NativeMouseEvent;

import com.sun.javafx.application.PlatformImpl;

import scenarii.geometry.Line;
import scenarii.geometry.Point;

/**
 * Created by geoffrey on 22/05/2016.
 */
public class OverlaySection extends Stage {

	public Method alwaysOnTop = null;
	
    private SectionOrientation displayMode;
    private AnchorPane root;

    private Line leftLine;
    private Line centerLine;
    private Line rightLine;

    private Arc arc;

    public OverlaySection(SectionOrientation orientation) {
        super(StageStyle.TRANSPARENT);
        this.initModality(Modality.APPLICATION_MODAL);
        
        try{
			alwaysOnTop = OverlaySection.class.getMethod("setAlwaysOnTop", boolean.class);
			 if(alwaysOnTop != null){
				 alwaysOnTop.invoke(this, true);		        	
		     }
        }
		catch(Exception e){}
       
        

        this.displayMode = orientation;

        root = new AnchorPane();
        makeOverlay(root);
        root.setStyle("-fx-background-color: TRANSPARENT;");

        Scene scene = new Scene(root, 150, 75);
        scene.setFill(null);
        this.setScene(scene);

        final Stage _this = this;
        //super.setAlwaysOnTop(true);
        this.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1,
					final Boolean arg2) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(!arg2){
							System.out.println("requesting");
							_this.requestFocus();
							_this.toFront();
						}
					}
				});
				
			}
		});
        
    }

    public void setPosition(NativeMouseEvent event){
        setPosition(event.getX(), event.getY());
    }

    public void setPosition(double x, double y){
        this.setX(x - this.getWidth()/2);
        if(displayMode == SectionOrientation.BOTTOM)
            this.setY(y + 2);
        else
            this.setY(y - this.getHeight() - 2);
    }

    public Point getPosition(){
        return new Point(
                (int) getX(),
                (int) getY()
        );
    }

    private void makeOverlay(AnchorPane root){
        ReadOnlyDoubleProperty w = root.widthProperty();
        ReadOnlyDoubleProperty h = root.heightProperty();

        arc = new Arc();
        arc.setCenterX(w.doubleValue()/2);
        w.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                arc.setCenterX(newValue.doubleValue()/2);
            }
        });
        arc.setCenterY(0);
        arc.setStartAngle(190);
        arc.setLength(160);
        arc.setPickOnBounds(false);
        arc.setMouseTransparent(true);
        if(displayMode == SectionOrientation.TOP){
            arc.setCenterY(h.doubleValue());
            h.addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    arc.setCenterY(newValue.doubleValue());
                }
            });
            arc.setStartAngle(10);
            arc.setLength(160);
        }
        arc.setRadiusX(20);
        arc.setRadiusY(20);

        arc.setStroke(Color.RED);
        arc.setStrokeWidth(2);
        arc.setFill(Color.TRANSPARENT);

        leftLine  = new Line();
        leftLine.bindEndY(h);

        rightLine = new Line();
        rightLine.bindStartX(w);
        rightLine.bindEndX(w);
        rightLine.bindEndY(h);

        centerLine = new Line();
        if(displayMode == SectionOrientation.TOP){
            centerLine.bindEndX(w);
        }
        else{
            centerLine.bindStartY(h);
            centerLine.bindEndX(w);
            centerLine.bindEndY(h);
        }

        root.getChildren().addAll(leftLine,centerLine,rightLine,arc);

    }



    public void showForDistort(){
        leftLine.setVisible(true);
        rightLine.setVisible(true);
        centerLine.setVisible(true);
    }

    public void hideForDistort(){
        leftLine.setVisible(false);
        rightLine.setVisible(false);
        centerLine.setVisible(false);
    }
   
    
}
