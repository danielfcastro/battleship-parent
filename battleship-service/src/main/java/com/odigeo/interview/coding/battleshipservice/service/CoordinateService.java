package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.exception.WrongCoordinateException;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;

import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class CoordinateService {

    private static final String COORDINATE_REGEX = "^([A-Z])(\\d+)$";
    private static final Pattern PATTERN = Pattern.compile(COORDINATE_REGEX);

    public Coordinate decodeCoordinate(String coordinate) {
        Matcher matcher = PATTERN.matcher(coordinate);

        if(!matcher.matches()) {
            throw new WrongCoordinateException(coordinate);
        }

        String column = matcher.group(1);
        String row = matcher.group(2);

        int columnIndex = decodeColumn(column.charAt(0));
        int rowIndex = decodeRow(row);
        return new Coordinate(coordinate, columnIndex, rowIndex);
    }

    private int decodeColumn(char c) {
        return c - 'A';
    }

    private int decodeRow(String value) {
        return Integer.parseInt(value) - 1;
    }

}
