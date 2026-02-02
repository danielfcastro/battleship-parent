package com.odigeo.interview.coding.battleshipcomputerservice.service;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CoordinateServiceTest {

    private CoordinateService coordinateService;

    @BeforeMethod
    public void init() {
        coordinateService = new CoordinateService();
    }

    @Test
    public void testRandomCoordinate() {
        String coordinate = coordinateService.randomCoordinate();
        assertNotNull(coordinate);
        assertTrue(coordinate.matches("^([A-Z])(\\d+)$"));
    }

}