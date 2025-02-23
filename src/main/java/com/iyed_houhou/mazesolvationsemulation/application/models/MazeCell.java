package com.iyed_houhou.mazesolvationsemulation.application.models;

public class MazeCell {
    public enum Terrain {
        WALL,    // Impassable
        WATER,   // Costly/dangerous: penalty +4 in heuristic
        SAND,    // Costly: penalty +3 in heuristic
        GRASS,   // Slight penalty: penalty +2 in heuristic
        EMPTY    // No extra cost
    }

    private int row;
    private int col;
    private Terrain terrain;

    public MazeCell(int row, int col, Terrain terrain) {
        this.row = row;
        this.col = col;
        this.terrain = terrain;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    // You might also include helper methods, e.g., isPassable().
    public boolean isPassable() {
        return terrain != Terrain.WALL;
    }
}
