package ocr;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ViewOcr {
	
	private StackPane pane = new StackPane();
	private VBox vb = new VBox(5);
	private StackPane paneIn = new StackPane();
	private ImageView iv = new ImageView();
	private HBox hbMenu = new HBox(5);
	public ToggleButton btnStart = new ToggleButton("Start");
	
	public ViewOcr() {		
		iv.setFitWidth(300);
		iv.setFitHeight(200);
	
		paneIn.getChildren().add(iv);
		hbMenu.getChildren().add(btnStart);
		hbMenu.setAlignment(Pos.CENTER);
		hbMenu.setPadding(new Insets(5,5,5,5));
		vb.getChildren().addAll(paneIn, hbMenu);
		VBox.setVgrow(paneIn, Priority.ALWAYS);
		vb.setPadding(new Insets(5,5,5,5));
		vb.setStyle("-fx-border-weight: 1; -fx-border-color: gray; -fx-border-radius: 5;");
		
		pane.getChildren().add(vb);
		pane.setPadding(new Insets(5,5,5,5));
	}
	
	public Pane getPane() {
		return pane;
	}
	
	public ImageView getImage() {
		return iv;
	}
}
