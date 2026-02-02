package com.odigeo.interview.coding.battleshipcomputerservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.GameFireResponse;
import com.odigeo.interview.coding.battleshipcomputerservice.exception.ClientException;
import com.odigeo.interview.coding.battleshipcomputerservice.util.BattleshipClientCommandBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.client.jaxrs.internal.ClientWebTarget;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertNotNull;

public class BattleshipClientTest {

    @Mock
    private ClientWebTarget webTarget;
    @Mock
    private ClientInvocationBuilder clientInvocationBuilder;
    @Mock
    private ClientResponse clientResponse;

    private BattleshipClient battleshipClient;

    @BeforeMethod
    public void init() {
        initMocks(this);
        battleshipClient = new BattleshipClient(webTarget);
        when(webTarget.path(any(String.class))).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(clientInvocationBuilder);
        when(clientInvocationBuilder.post(any())).thenReturn(clientResponse);
    }

    @Test
    public void testNewInstance() {
        BattleshipClient battleshipClient = new BattleshipClient();
        assertNotNull(battleshipClient);
    }

    @Test
    public void testJoinGame() {
        when(clientResponse.getStatus()).thenReturn(204);
        battleshipClient.joinGame("12345", BattleshipClientCommandBuilder.buildGameJoinCommand());
    }

    @Test(expectedExceptions = ClientException.class)
    public void testJoinGameOnBadRequest() {
        when(clientResponse.getStatus()).thenReturn(404);
        battleshipClient.joinGame("12345", BattleshipClientCommandBuilder.buildGameJoinCommand());
    }

    @Test
    public void testDeployShips() {
        when(clientResponse.getStatus()).thenReturn(204);
        battleshipClient.deployShips("12345", BattleshipClientCommandBuilder.buildDeployShipsCommand());
    }

    @Test(expectedExceptions = ClientException.class)
    public void testDeployShipsOnBadRequest() {
        when(clientResponse.getStatus()).thenReturn(404);
        battleshipClient.deployShips("12345", BattleshipClientCommandBuilder.buildDeployShipsCommand());
    }

    @Test
    public void testFire() {
        when(clientResponse.getStatus()).thenReturn(200);
        when(clientResponse.readEntity(GameFireResponse.class)).thenReturn(new GameFireResponse(GameFireResponse.FireOutcome.HIT));
        GameFireResponse fireResponse = battleshipClient.fire("12345", BattleshipClientCommandBuilder.buildGameFireCommand("A1"));
        assertNotNull(fireResponse);
    }

    @Test(expectedExceptions = ClientException.class)
    public void testFireOnBadRequest() {
        when(clientResponse.getStatus()).thenReturn(404);
        battleshipClient.fire("12345", BattleshipClientCommandBuilder.buildGameFireCommand("Z1"));
    }

}