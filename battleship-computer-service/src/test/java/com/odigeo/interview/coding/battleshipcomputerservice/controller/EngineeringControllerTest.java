package com.odigeo.interview.coding.battleshipcomputerservice.controller;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EngineeringControllerTest {

    EngineeringController engineeringController;

    @Test
    public void testPing() {
        engineeringController = new EngineeringController();
        Assert.assertEquals(engineeringController.ping(), "pong");
    }

}