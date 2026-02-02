package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.exception.WrongCoordinateException;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CoordinateServiceTest {

    private CoordinateService coordinateService;

    @BeforeMethod
    public void init() {
        coordinateService = new CoordinateService();
    }

    @Test
    public void testDecodeCoordinateA1() {
        Coordinate coordinate = coordinateService.decodeCoordinate("A1");
        assertEquals(coordinate, new Coordinate("A1", 0, 0));
    }

    @Test(expectedExceptions = WrongCoordinateException.class, expectedExceptionsMessageRegExp = "Coordinate AB is wrong")
    public void testDecodeWrongCoordinatePattern() {
        Coordinate coordinate = coordinateService.decodeCoordinate("AB");
    }

}