package com.iyed_houhou.mazesolvationsemulation.application.models;

public record Coordinate(int row, int col) {

    @Override
    public boolean equals(Object obj) {
        // Check if the object is compared with itself
        if (this == obj) {
            return true;
        }
        // Check if the object is an instance of Coordinate
        if (!(obj instanceof Coordinate(int row1, int col1))) {
            return false;
        }
        // Compare the data members for equality
        return this.row == row1 && this.col == col1;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
