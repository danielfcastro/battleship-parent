package com.odigeo.interview.coding.battleshipservice.controller;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("java:S1192") // Simple health check test duplicated across microservices
public class EngineeringControllerTest {

    EngineeringController engineeringController;

    @Test
    public void testPing() {
        engineeringController = new EngineeringController();
        Assert.assertEquals(engineeringController.ping(), "pong");
    }

}
