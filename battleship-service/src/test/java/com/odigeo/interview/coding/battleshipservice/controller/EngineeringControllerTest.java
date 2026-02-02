package com.odigeo.interview.coding.battleshipservice.controller;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class EngineeringControllerTest {

    EngineeringController engineeringController;

    @Test
    public void testPing() {
        engineeringController = new EngineeringController();
        Assert.assertEquals(engineeringController.ping(), "pong");
    }

}
