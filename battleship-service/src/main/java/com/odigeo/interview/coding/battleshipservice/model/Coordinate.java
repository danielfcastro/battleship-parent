package com.odigeo.interview.coding.battleshipservice.model;

import java.util.Objects;

public class Coordinate {

    private String value;
    private int column;
    private int row;

    public Coordinate(String value, int column, int row) {
        this.value = value;
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return column == that.column &&
                row == that.row &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, column, row);
    }
}
