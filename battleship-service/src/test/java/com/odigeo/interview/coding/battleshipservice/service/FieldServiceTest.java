package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FieldServiceTest {

    @Mock
    private CoordinateService coordinateService;

    @InjectMocks
    private FieldService fieldService;

    private List<Ship> shipsDeployment;
    private Cell[][] field;

    @BeforeMethod
    public void init() {
        initMocks(this);
        when(coordinateService.decodeCoordinate(any())).thenCallRealMethod();
        shipsDeployment = ShipDeploymentBuilder.buildValidDeployment();
        field = fieldService.buildField(shipsDeployment);
    }

    @Test
    public void testShipSunkPartialHit() {
        List<Coordinate> coordinates = shipsDeployment.get(0).getCoordinates();
        field[coordinates.get(0).getRow()][coordinates.get(0).getColumn()].hit();
        boolean isShipSunk = fieldService.isShipSunk(field, shipsDeployment.get(0));
        assertFalse(isShipSunk);
    }

    @Test
    public void testShipSunkNoHit() {
        List<Coordinate> coordinates = shipsDeployment.get(0).getCoordinates();
        boolean isShipSunk = fieldService.isShipSunk(field, shipsDeployment.get(0));
        assertFalse(isShipSunk);
    }

    @Test
    public void testBuildFieldShipsDeployment() {
        shipsDeployment.forEach(ship -> {
            List<Coordinate> coordinates = ship.getCoordinates();
            coordinates.forEach(coordinate -> {
                Cell cell = field[coordinate.getRow()][coordinate.getColumn()];
                assertNotNull(cell);
                assertFalse(cell.isWater());
                assertFalse(cell.isHit());
                assertEquals(cell.getShip().getShipType(), ship.getShipType());
            });
        });
    }

    @Test
    public void testBuildWater() {
        Set<Coordinate> shipsCoordinates = shipsDeployment.stream()
                .map(Ship::getCoordinates)
                .flatMap(Collection::stream)
                .collect(toSet());

        int height = field.length;
        int width = field[0].length;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                String coordinate = ((char) (65 + col)) + "" + (row + 1);
                if(!shipsCoordinates.contains(new Coordinate(coordinate, col, row))) {
                    boolean isWater = field[row][col].isWater();
                    assertTrue(isWater);
                }
            }
        }
    }

}