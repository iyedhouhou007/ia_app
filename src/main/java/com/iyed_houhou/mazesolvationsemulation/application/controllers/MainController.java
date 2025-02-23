// MainController.java
package com.iyed_houhou.mazesolvationsemulation.application.controllers;

import com.iyed_houhou.mazesolvationsemulation.application.models.Coordinate;
import com.iyed_houhou.mazesolvationsemulation.application.models.Maze;
import com.iyed_houhou.mazesolvationsemulation.application.models.MazeCell;
import com.iyed_houhou.mazesolvationsemulation.application.models.MazePath;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class MainController {

    @FXML
    private GridPane gridPane;

    @FXML
    private ComboBox<Integer> rowsComboBox;

    @FXML
    private ComboBox<Integer> colsComboBox;

    @FXML
    private ComboBox<Integer> batteryComboBox;

    @FXML
    private Label batteryLevelLabel; // Added Battery Level Label

    @FXML
    private Button refreshButton;

    @FXML
    private Button resetSelectionsButton; // Reset Selections Button

    // Default cell sizes (for a 3x3 grid)
    private int cellSize_row = 1000 / 3;
    private int cellSize_col = 1000 / 3;

    // To store the user-selected start and goal
    private Coordinate startSelected = null;
    private Coordinate goalSelected = null;

    // The Maze instance that is used for both drawing and solving.
    private Maze currentMaze = null;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupBatteryComboBox();
        updateGrid(rowsComboBox.getValue(), colsComboBox.getValue());

        refreshButton.setOnAction(_ -> refreshMaze());
        resetSelectionsButton.setOnAction(_ -> resetSelectionsAndPath()); // Reset Selections Button Action
    }

    private void setupBatteryComboBox() {
        batteryComboBox.getItems().addAll(10, 20, 30, 40, 50, 60, 70, 90 , 100); // Example battery levels
        batteryComboBox.setValue(100); // Default battery level
        updateBatteryLabel(batteryComboBox.getValue()); // Initialize label with default value
        batteryComboBox.setOnAction(e -> updateBatteryLabel(batteryComboBox.getValue())); // Update label on combo box change
    }

    private void updateBatteryLabel(int batteryValue) {
        batteryLevelLabel.setText("Initial Battery: " + batteryValue);
    }
    private void updateBatteryLabelFinal(int finalBattery) {
        batteryLevelLabel.setText("Final Battery Remaining: " + finalBattery);
    }


    private void refreshMaze() {
        // Simply update the grid using current rows/cols from the combo boxes.
        updateGrid(rowsComboBox.getValue(), colsComboBox.getValue());
    }

    private void resetSelectionsAndPath() {
        startSelected = null;
        goalSelected = null;
        batteryLevelLabel.setText(""); // Clear battery label on reset

        // Remove all "S" and "G" labels
        for (Node node : gridPane.getChildren()) {
            if (node instanceof StackPane cellPane) {
                cellPane.getChildren().removeIf(child -> {
                    if (child instanceof Label label) {
                        return "S".equals(label.getText()) || "G".equals(label.getText());
                    }
                    return false;
                });
            }
        }

        // Remove the path pane if present
        gridPane.getChildren().removeIf(node -> node instanceof Pane && node.getStyleClass().contains("path"));
    }


    private void setupComboBoxes() {
        for (int i = 1; i <= 10; i++) {
            rowsComboBox.getItems().add(i);
            colsComboBox.getItems().add(i);
        }
        rowsComboBox.setValue(3);
        colsComboBox.setValue(3);
        rowsComboBox.setOnAction(_ -> updateGrid(rowsComboBox.getValue(), rowsComboBox.getValue()));
        colsComboBox.setOnAction(_ -> updateGrid(colsComboBox.getValue(), colsComboBox.getValue()));

    }

    /**
     * Updates the grid. Here we create a Maze instance (stored in currentMaze)
     * that is used for both painting the grid and later for solving.
     */
    private void updateGrid(int rows, int cols) {
        // Reset selections when grid is updated.
        resetSelectionsAndPath();
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();
        updateCellSize(rows, cols);

        // Set column and row constraints.
        for (int i = 0; i < cols; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(cellSize_col));
        }
        for (int i = 0; i < rows; i++) {
            gridPane.getRowConstraints().add(new RowConstraints(cellSize_row));
        }

        // Create one Maze instance and generate the maze layout.
        // (We initially pass dummy start/end values; we will update them later based on user selection.)
        currentMaze = new Maze(rows, cols, new Coordinate(0, 0), new Coordinate(rows - 1, cols - 1));
        currentMaze.generateMaze();
        MazeCell[][] mazeGrid = currentMaze.getGrid();

        // Create and add Panes for each cell.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                StackPane cellPane = createCellPane(row, col, mazeGrid[row][col]);
                gridPane.add(cellPane, col, row);
            }
        }
    }

    /**
     * Creates a StackPane for a given cell. The background color is set based on the cell's terrain.
     * Also attaches a mouse click handler to record start and goal selections.
     */
    private StackPane createCellPane(int row, int col, MazeCell cell) {
        StackPane cellPane = new StackPane();
        cellPane.getStyleClass().add("cell-pane"); // Base style for all cells

        switch (cell.getTerrain()) {
            case WALL -> cellPane.getStyleClass().add("cell-wall");
            case WATER -> cellPane.getStyleClass().add("cell-water");
            case SAND -> cellPane.getStyleClass().add("cell-sand");
            case GRASS -> cellPane.getStyleClass().add("cell-grass");
            default -> { /* Default style is already applied with "cell-pane" */ }
        }
        cellPane.setPrefSize(cellSize_col, cellSize_row);

        cellPane.setOnMouseClicked(_ -> {
            // Check if both are already selected; if so, reset
            if (startSelected != null && goalSelected != null) {
                resetSelectionsAndPath();
            }

            if (startSelected == null) {
                Coordinate tmp = new Coordinate(row, col);
                if (cell.getTerrain() == MazeCell.Terrain.WALL) {
                    showDialog("Error", "You can't start in a wall", Alert.AlertType.ERROR);
                } else {
                    startSelected = tmp;
                    addCenteredLabel(cellPane, "S");
                }
            } else if (goalSelected == null && !(startSelected.row() == row && startSelected.col() == col)) {
                Coordinate tmp = new Coordinate(row, col);
                if (cell.getTerrain() == MazeCell.Terrain.WALL) {
                    showDialog("Error", "You can't end in a wall", Alert.AlertType.ERROR);
                } else {
                    goalSelected = tmp;
                    addCenteredLabel(cellPane, "G");
                    runMazeSolving();
                }
            }
        });
        return cellPane;
    }


    private void showDialog(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Adds a centered label with the given text ("S" or "G") to the provided cell StackPane.
     */
    private void addCenteredLabel(StackPane cellPane, String text) {
        Label label = new Label(text);
        if ("S".equals(text)) {
            label.getStyleClass().add("start-label");
        } else if ("G".equals(text)) {
            label.getStyleClass().add("goal-label");
        }
        cellPane.getChildren().add(label);
    }

    /**
     * Recomputes the cell size based on the number of rows and columns.
     */
    private void updateCellSize(int rows, int cols) {
        cellSize_col = 1000 / cols;
        cellSize_row = 750 / rows;
    }

    /**
     * Once start and goal are chosen, update the Maze instance with these points,
     * then solve the maze and draw the animated solution path.
     */
    private void runMazeSolving() {
        // Update the currentMaze's starting and ending cells.
        currentMaze.setStartingCell(startSelected);
        currentMaze.setEndCell(goalSelected);

        int initialBattery = batteryComboBox.getValue(); // Get battery value from ComboBox

        batteryLevelLabel.setText("Solving Maze..."); // Indicate solving process

        // IMPORTANT: Do NOT call generateMaze() again! We want to use the existing layout.
        MazePath solution = currentMaze.solveMazeAStar(initialBattery); // Pass battery to solveMazeAStar


        if (solution.getCoordinates().isEmpty()) {
            batteryLevelLabel.setText("No Path Found or Battery Depleted"); // Update label if no path
            showDialog("No Path found", "there is no path from S to G in this maze or no path with the given battery", Alert.AlertType.INFORMATION);
        } else {
            drawAnimatedPath(solution);
            MazePath solvedPath = currentMaze.getSolutionPath(); // Get the solution path to access destination node
            if (solvedPath != null && !solvedPath.getCoordinates().isEmpty()) {
                // Solution found, update battery label with remaining battery from the last node in path
                com.iyed_houhou.mazesolvationsemulation.application.models.Node destinationNode = currentMaze.getDestinationNode(); // Assume getter added in Maze.java
                if (destinationNode != null) {
                    updateBatteryLabelFinal(destinationNode.battery); // Update with final battery
                } else {
                    batteryLevelLabel.setText("Solution Found, Battery Unknown"); // Fallback if no destination node
                }

            } else {
                batteryLevelLabel.setText("Solution Found, Battery Info N/A"); // Fallback if no solution path

            }
        }
    }

    /**
     * Creates a simple Rectangle to represent the robot and centers it within its cell.
     */
    private Node createRobot() {
        double robotSizeFraction = 0.3; // Robot size relative to cell size (adjust as needed)
        double robotWidth = cellSize_col * robotSizeFraction;
        double robotHeight = cellSize_row * robotSizeFraction;

        Rectangle robot = new Rectangle(robotWidth, robotHeight);
        robot.setFill(Color.RED);
        // Center the robot based on its size
        robot.setTranslateX(-robotWidth / 2.0);
        robot.setTranslateY(-robotHeight / 2.0);
        robot.getStyleClass().add("robot");
        return robot;
    }


    /**
     * Draws an animated continuous path using a robot that walks through the centers of the cells specified in the MazePath.
     * The robot is animated to move from cell to cell, and optionally rotated to face the direction of movement.
     * Now also draws a path BEHIND the robot as it moves.
     */
    private void drawAnimatedPath(MazePath mazePath) {
        if (mazePath == null || mazePath.getCoordinates().isEmpty()) {
            return;
        }

        Pane pathPane = new Pane();
        pathPane.getStyleClass().add("path");
        int totalCols = gridPane.getColumnConstraints().size();
        int totalRows = gridPane.getRowConstraints().size();
        pathPane.setPrefWidth(totalCols * cellSize_col);
        pathPane.setPrefHeight(totalRows * cellSize_row);
        pathPane.setMouseTransparent(true);

        Node robot = createRobot(); // Create the robot node
        pathPane.getChildren().add(robot); // Add robot to pathPane

        Path trailPath = new Path(); // Create a path for the trail
        trailPath.getStyleClass().add("trail-line"); // Style class for the trail line
        pathPane.getChildren().add(trailPath); // Add trail path to pathPane


        Timeline timeline = new Timeline();
        var coordinates = mazePath.getCoordinates();
        Coordinate firstCoord = coordinates.getFirst();
        double startX = firstCoord.col() * cellSize_col + cellSize_col / 2.0;
        double startY = firstCoord.row() * cellSize_row + cellSize_row / 2.0;

        robot.setTranslateX(startX); // Initial robot position - center of the first cell
        robot.setTranslateY(startY);
        trailPath.getElements().add(new MoveTo(startX, startY)); // Start the trail path


        double totalDurationSeconds = (double) mazePath.getCoordinates().size() / 13 * 2;
        double cumulativeTime = 0;
        Coordinate prevCoord = firstCoord;


        for (int i = 1; i < coordinates.size(); i++) { // Start from the second coordinate
            Coordinate currentCoord = coordinates.get(i);
            double targetX = currentCoord.col() * cellSize_col + cellSize_col / 2.0;
            double targetY = currentCoord.row() * cellSize_row + cellSize_row / 2.0;

            cumulativeTime += totalDurationSeconds / coordinates.size(); // Distribute time evenly

            //--- Orientation (Rotation) ---
            double angle = 0;
            if (currentCoord.col() > prevCoord.col()) { // Moving Right
                angle = 0;
            } else if (currentCoord.col() < prevCoord.col()) { // Moving Left
                angle = 180;
            } else if (currentCoord.row() > prevCoord.row()) { // Moving Down
                angle = 90;
            } else if (currentCoord.row() < prevCoord.row()) { // Moving Up
                angle = -90;
            }

            KeyValue rotateKV = new KeyValue(robot.rotateProperty(), angle);
            KeyFrame rotateKeyFrame = new KeyFrame(Duration.seconds(cumulativeTime), rotateKV);
            timeline.getKeyFrames().add(rotateKeyFrame);


            //--- Position Animation ---
            KeyValue translateX_KV = new KeyValue(robot.translateXProperty(), targetX);
            KeyValue translateY_KV = new KeyValue(robot.translateYProperty(), targetY);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(cumulativeTime), e -> { // Using Action Event here!
                // Add line to trail *before* robot moves to the new position
                trailPath.getElements().add(new LineTo(targetX, targetY));
            }, translateX_KV, translateY_KV); // Now keyFrame includes an event handler

            timeline.getKeyFrames().add(keyFrame);

            prevCoord = currentCoord; // Update previous coordinate
        }


        timeline.play();
        pathPane.getStyleClass().add("path");
        gridPane.add(pathPane, 0, 0, totalCols, totalRows);
    }
}