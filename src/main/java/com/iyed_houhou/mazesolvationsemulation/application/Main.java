package com.iyed_houhou.mazesolvationsemulation.application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.Objects;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
            // Load the FXML file
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/iyed_houhou/mazesolvationsemulation/View/main.fxml")));

            Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
