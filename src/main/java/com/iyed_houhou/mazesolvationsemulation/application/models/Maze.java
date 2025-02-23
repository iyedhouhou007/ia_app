// Maze.java
package com.iyed_houhou.mazesolvationsemulation.application.models;

import java.util.*;

public class Maze {
    private final int rows;
    private final int cols;
    private Coordinate startingCell;
    private Coordinate endCell;
    private final MazeCell[][] grid;
    private MazePath solutionPath;
    private Node destinationNode; // Added to store destination node

    public Maze(int rows, int cols, Coordinate startingCell, Coordinate endCell) {
        this.rows = rows;
        this.cols = cols;
        this.startingCell = startingCell;
        this.endCell = endCell;
        grid = new MazeCell[rows][cols];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = new MazeCell(row, col, MazeCell.Terrain.EMPTY);
            }
        }
    }

    public void generateMaze() {
        Random rand = new Random();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double p = rand.nextDouble();
                MazeCell.Terrain terrain;

                if (p < 0.15) {
                    terrain = MazeCell.Terrain.WALL;
                } else if (p < 0.35) {
                    terrain = MazeCell.Terrain.WATER;
                } else if (p < 0.55) {
                    terrain = MazeCell.Terrain.SAND;
                } else if (p < 0.75) {
                    terrain = MazeCell.Terrain.GRASS;
                } else {
                    terrain = MazeCell.Terrain.EMPTY;
                }

                grid[row][col].setTerrain(terrain);
            }
        }
    }

    private double heuristic(int row, int  col ){
        return (Math.abs(row - endCell.row()) + Math.abs(col - endCell.col()));
    }

    private double movementCost(int row , int col) {
        MazeCell.Terrain terrain = grid[row][col].getTerrain();
        return switch (terrain) {
            case GRASS -> 2.0;
            case SAND -> 3.0;
            case WATER -> 4.0;
            case EMPTY -> 1.0;
            default -> Double.POSITIVE_INFINITY;
        };
    }

    private int getBatteryConsumption(int row , int col) {
        MazeCell.Terrain terrain = grid[row][col].getTerrain();
        return switch (terrain) {
            case GRASS -> 2;
            case SAND -> 3;
            case WATER -> 5;
            case EMPTY -> 1;
            default -> 10000; // High value for WALL to effectively block path
        };
    }

    public MazePath solveMazeAStar(int initialBattery) { // Added initialBattery parameter

        PriorityQueue<Node> Queue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.f));

        boolean[][] visitedMatrix = new boolean[rows][cols];

        Node startPointNode = new Node(startingCell, 0, heuristic(startingCell.row(), startingCell.col()), null, initialBattery); // Initialize with battery
        Queue.add(startPointNode);

        destinationNode = null; // Initialize destinationNode at the start of each solve
        int[][] moveDirection = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        while (!Queue.isEmpty()) {
            Node currentNode = Queue.poll();
            System.out.println("Processing Node: " + currentNode);

            if (currentNode.cell.row() == endCell.row() && currentNode.cell.col() == endCell.col()) {
                destinationNode = currentNode; // Store destination node here
                System.out.println("Target Destination Acquired: " + destinationNode);
                break;
            }

            visitedMatrix[currentNode.cell.row()][currentNode.cell.col()] = true;

            for (int[] delta : moveDirection) {
                Coordinate neighborCell = new Coordinate(currentNode.cell.row() + delta[0], currentNode.cell.col() + delta[1]);

                int neighborRow = neighborCell.row();
                int neighborCol = neighborCell.col();

                // check boundaries and if visited
                if (!isValidCell(neighborRow, neighborCol, visitedMatrix)) {
                    continue;
                }

                if (isConsecutiveWater(currentNode.cell, neighborCell)) {
                    System.out.printf("Neighbor (%d,%d) Invalid: Avoids Water Consecutively.\n", neighborRow, neighborCol);
                    continue;
                }

                double stepCost = movementCost(neighborRow, neighborCol);
                if (Double.isInfinite(stepCost)) {
                    continue;
                }

                int batteryConsumption = getBatteryConsumption(neighborRow, neighborCol); // Get battery consumption
                if (currentNode.battery - batteryConsumption < 0) { // Check battery level
                    System.out.printf("Neighbor (%d,%d) Invalid: Battery Depleted.\n", neighborRow, neighborCol);
                    continue; // Skip if battery is insufficient
                }


                double heuristicEstimate = heuristic(neighborRow, neighborCol);
                Node neighborNode = new Node(neighborCell, stepCost, heuristicEstimate, currentNode, currentNode.battery - batteryConsumption); // Pass updated battery

                if (isPathRedundant(neighborNode, Queue)) {
                    continue;
                }
                Queue.add(neighborNode);
            }
        }

        MazePath pathResult = new MazePath();
        if (destinationNode == null) {
            System.out.println("Exploration Complete: No Path Found or Battery Depleted!");
            return pathResult;
        }

        List<Coordinate> pathCordsReverse = new ArrayList<>();
        Node traceNode = destinationNode;
        while (traceNode != null) {
            pathCordsReverse.add(new Coordinate(traceNode.cell.row(), traceNode.cell.col()));
            traceNode = traceNode.parent;
        }

        for (Coordinate currentCoord : pathCordsReverse) {
            pathResult.addCoordinateFirst(currentCoord);
        }
        this.solutionPath = pathResult;
        System.out.println("Solution Trajectory Discovered: " + pathResult + " Battery Remaining: " + destinationNode.battery); // Print remaining battery
        return pathResult;
    }

    public boolean isValidCell(int row, int col, boolean[][] visited) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        if (visited[row][col]) {
            return false;
        }
        return grid[row][col].getTerrain() != MazeCell.Terrain.WALL;
    }

    private boolean isConsecutiveWater(Coordinate currentCell, Coordinate neighborCell) {
        return grid[currentCell.row()][currentCell.col()].getTerrain() == MazeCell.Terrain.WATER &&
                grid[neighborCell.row()][neighborCell.col()].getTerrain() == MazeCell.Terrain.WATER;
    }

    private boolean isPathRedundant(Node neighbor, PriorityQueue<Node> openQueue) {
        for (Node queuedNode : openQueue) {
            if (queuedNode.cell.equals(neighbor.cell) && queuedNode.f <= neighbor.f) {
                return true;
            }
        }
        return false;
    }


    public MazeCell[][] getGrid() {
        return grid;
    }

    public MazePath getSolutionPath() {
        return solutionPath;
    }

    public void setStartingCell(Coordinate start) {
        this.startingCell = start;
    }

    public void setEndCell(Coordinate end) {
        this.endCell = end;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }
}