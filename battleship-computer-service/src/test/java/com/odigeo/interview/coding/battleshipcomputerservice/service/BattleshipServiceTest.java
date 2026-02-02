package com.odigeo.interview.coding.battleshipcomputerservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BattleshipServiceTest {

    @Mock
    private GameFireResponse gameFireResponse;
    @Mock
    private CoordinateService coordinateService;
    @Mock
    private BattleshipClient battleshipClient;
    @InjectMocks
    private BattleshipService battleshipService;

    @BeforeMethod
    public void init() {
        initMocks(this);
    }

    @Test
    public void testJoinGame() {
        battleshipService.joinGame("12345");
        verify(battleshipClient, times(1)).joinGame(any(), any());
    }

    @Test
    public void testDeployShips() {
        battleshipService.deployShips("12345");
        verify(battleshipClient, times(1)).deployShips(any(), any());
    }

    @Test
    public void testFire() {
        when(gameFireResponse.isGameWon()).thenReturn(true);
        when(battleshipClient.fire(any(), any())).thenReturn(gameFireResponse);
        battleshipService.fire("12345");
        verify(battleshipClient, times(1)).fire(any(), any());
    }
}