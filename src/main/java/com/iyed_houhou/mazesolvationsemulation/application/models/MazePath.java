package com.iyed_houhou.mazesolvationsemulation.application.models;

import java.util.ArrayList;
import java.util.List;

public class MazePath {
    private final List<Coordinate> coordinates;

    public MazePath() {
        this.coordinates = new ArrayList<>();
    }


    public void addCoordinateFirst(Coordinate coordinate) {
        coordinates.addFirst(coordinate);  // Insert at beginning of list
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }
}