package com.odigeo.interview.coding.battleshipservice.util;

import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

public class ShipDeploymentValidatorTest {

    private ShipDeploymentValidator shipDeploymentValidator;

    @BeforeMethod
    public void init() {
        initMocks(this);
        shipDeploymentValidator = new ShipDeploymentValidator();
    }

    @Test
    public void testValidateWithValidDeployment() {
        shipDeploymentValidator.validate(ShipDeploymentBuilder.buildValidDeployment());
    }

    @Test(expectedExceptions = ShipDeploymentException.class)
    public void testValidateWithDuplicatedShipType() {
        List<Ship> shipsDeployment = ShipDeploymentBuilder.buildDuplicatedShipsDeployment();
        shipDeploymentValidator.validate(shipsDeployment);
    }

    @Test(expectedExceptions = ShipDeploymentException.class, expectedExceptionsMessageRegExp = "Wrong number of deployed ships: expected 5, got 4")
    public void testValidateWithIncorrectShipsNumberDeployed() {
        List<Ship> shipsDeployment = ShipDeploymentBuilder.buildWrongNumberOfShipsDeployment();
        shipDeploymentValidator.validate(shipsDeployment);
    }

    @Test(expectedExceptions = ShipDeploymentException.class)
    public void testValidateWithOverlappingShips() {
        List<Ship> shipsDeployment = ShipDeploymentBuilder.buildOverlappingShipsDeployment();
        shipDeploymentValidator.validate(shipsDeployment);
    }

    @Test(expectedExceptions = ShipDeploymentException.class)
    public void testValidateWithWrongShipLength() {
        List<Ship> shipsDeployment = ShipDeploymentBuilder.buildWrongShipLengthDeployment();
        shipDeploymentValidator.validate(shipsDeployment);
    }

    @Test(expectedExceptions = ShipDeploymentException.class)
    public void testValidateWithWrongShipOutOfGrid() {
        List<Ship> shipsDeployment = ShipDeploymentBuilder.buildShipOutOfGridDeployment();
        shipDeploymentValidator.validate(shipsDeployment);
    }

}
