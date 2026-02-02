package com.odigeo.interview.coding.battleshipcomputerservice.service;

import javax.inject.Singleton;
import java.util.Random;

@Singleton
public class CoordinateService {

    private static final Random random = new Random();

    public String randomCoordinate() {
        int row = random.nextInt(10) + 1;
        char column = (char) (65 + random.nextInt(10));
        return column + "" + row;
    }

}
