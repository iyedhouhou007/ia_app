// Node.java
package com.iyed_houhou.mazesolvationsemulation.application.models;

public class Node {
    final Coordinate cell;
    final double h; // heuristic cost to goal
    final double f; // total cost - MODIFIED to f = h + moveCost;
    final Node parent;
    public int battery; // Add battery field

    // Modified Constructor - clearer parameter name: 'moveCost'
    Node(Coordinate cell, double moveCost, double h, Node parent, int battery) { // Add battery parameter
        this.cell = cell;
        this.h = h;
        this.f = moveCost + h; // f is now correctly calculated as moveCost + h
        this.parent = parent;
        this.battery = battery; // Initialize battery
    }

    @Override
    public String toString() {
        return String.format("Node(%d,%d) [f=%.2f, h=%.2f, moveCost=%.2f, battery=%d]", cell.row(), cell.col(), f, h, (f - h), battery); // Display battery
    }
}