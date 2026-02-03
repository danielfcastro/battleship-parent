package com.odigeo.interview.coding.battleshipservice.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.testng.Assert.*;

public class GameTest {

    private Game game;
    private Cell[][] field1;
    private Cell[][] field2;

    @BeforeMethod
    public void setUp() {
        game = new Game();
        field1 = new Cell[10][10];
        field2 = new Cell[10][10];
    }

    @Test
    public void testGetSetId() {
        game.setId("game-123");
        assertEquals(game.getId(), "game-123");
    }

    @Test
    public void testGetSetPlayerOneId() {
        game.setPlayerOneId("player1");
        assertEquals(game.getPlayerOneId(), "player1");
    }

    @Test
    public void testGetSetPlayerTwoId() {
        game.setPlayerTwoId("player2");
        assertEquals(game.getPlayerTwoId(), "player2");
    }

    @Test
    public void testGetSetVsComputer() {
        game.setVsComputer(true);
        assertTrue(game.isVsComputer());

        game.setVsComputer(false);
        assertFalse(game.isVsComputer());
    }

    @Test
    public void testGetSetPlayerTurn() {
        game.setPlayerTurn(1);
        assertEquals(game.getPlayerTurn(), Integer.valueOf(1));
    }

    @Test
    public void testGetSetPlayerOneField() {
        game.setPlayerOneField(field1);
        assertEquals(game.getPlayerOneField(), field1);
    }

    @Test
    public void testGetSetPlayerTwoField() {
        game.setPlayerTwoField(field2);
        assertEquals(game.getPlayerTwoField(), field2);
    }

    @Test
    public void testGetSetCreatedAt() {
        Instant now = Instant.now();
        game.setCreatedAt(now);
        assertEquals(game.getCreatedAt(), now);
    }

    @Test
    public void testGetSetStartedAt() {
        Instant now = Instant.now();
        game.setStartedAt(now);
        assertEquals(game.getStartedAt(), now);
    }

    @Test
    public void testGetSetFinishedAt() {
        Instant now = Instant.now();
        game.setFinishedAt(now);
        assertEquals(game.getFinishedAt(), now);
    }

    @Test
    public void testGetSetWinner() {
        game.setWinner("player1");
        assertEquals(game.getWinner(), "player1");
    }

    @Test
    public void testSetPlayerFieldForPlayerOne() {
        game.setPlayerOneId("player1");
        game.setPlayerField("player1", field1);
        assertEquals(game.getPlayerOneField(), field1);
    }

    @Test
    public void testSetPlayerFieldForPlayerTwo() {
        game.setPlayerTwoId("player2");
        game.setPlayerField("player2", field2);
        assertEquals(game.getPlayerTwoField(), field2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Player player3 does not exist in the game.*")
    public void testSetPlayerFieldThrowsExceptionForInvalidPlayer() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerField("player3", field1);
    }

    @Test
    public void testGetPlayerFieldForPlayerOne() {
        game.setPlayerOneId("player1");
        game.setPlayerOneField(field1);
        assertEquals(game.getPlayerField("player1"), field1);
    }

    @Test
    public void testGetPlayerFieldForPlayerTwo() {
        game.setPlayerTwoId("player2");
        game.setPlayerTwoField(field2);
        assertEquals(game.getPlayerField("player2"), field2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Player player3 does not exist in the game.*")
    public void testGetPlayerFieldThrowsExceptionForInvalidPlayer() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.getPlayerField("player3");
    }

    @Test
    public void testGetOpponentFieldForPlayerOne() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerTwoField(field2);
        assertEquals(game.getOpponentField("player1"), field2);
    }

    @Test
    public void testGetOpponentFieldForPlayerTwo() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerOneField(field1);
        assertEquals(game.getOpponentField("player2"), field1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Player player3 does not exist in the game.*")
    public void testGetOpponentFieldThrowsExceptionForInvalidPlayer() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.getOpponentField("player3");
    }

    @Test
    public void testSetNextPlayerTurnFromOne() {
        game.setPlayerTurn(1);
        game.setNextPlayerTurn();
        assertEquals(game.getPlayerTurn(), Integer.valueOf(2));
    }

    @Test
    public void testSetNextPlayerTurnFromTwo() {
        game.setPlayerTurn(2);
        game.setNextPlayerTurn();
        assertEquals(game.getPlayerTurn(), Integer.valueOf(1));
    }

    @Test
    public void testIsPlayerTurnWithPlayerIdForPlayerOne() {
        game.setPlayerOneId("player1");
        game.setPlayerTurn(1);
        assertTrue(game.isPlayerTurn("player1"));
    }

    @Test
    public void testIsPlayerTurnWithPlayerIdForPlayerTwo() {
        game.setPlayerTwoId("player2");
        game.setPlayerTurn(2);
        assertTrue(game.isPlayerTurn("player2"));
    }

    @Test
    public void testIsPlayerTurnWithPlayerIdReturnsFalse() {
        game.setPlayerOneId("player1");
        game.setPlayerTurn(2);
        assertFalse(game.isPlayerTurn("player1"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Player player3 does not exist in the game.*")
    public void testIsPlayerTurnWithPlayerIdThrowsExceptionForInvalidPlayer() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.isPlayerTurn("player3");
    }

    @Test
    public void testIsPlayerTurnWithPlayerNumberReturnsTrue() {
        game.setPlayerTurn(1);
        assertTrue(game.isPlayerTurn(1));
    }

    @Test
    public void testIsPlayerTurnWithPlayerNumberReturnsFalse() {
        game.setPlayerTurn(1);
        assertFalse(game.isPlayerTurn(2));
    }

    @Test
    public void testIsPlayerTurnWithNullPlayerTurn() {
        game.setPlayerTurn(null);
        assertFalse(game.isPlayerTurn(1));
    }

    @Test
    public void testIsFinishedReturnsTrue() {
        game.setFinishedAt(Instant.now());
        assertTrue(game.isFinished());
    }

    @Test
    public void testIsFinishedReturnsFalse() {
        game.setFinishedAt(null);
        assertFalse(game.isFinished());
    }

    @Test
    public void testPlayersReadyReturnsTrue() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerOneField(field1);
        game.setPlayerTwoField(field2);
        assertTrue(game.playersReady());
    }

    @Test
    public void testPlayersReadyReturnsFalseWhenPlayerOneIdIsNull() {
        game.setPlayerOneId(null);
        game.setPlayerTwoId("player2");
        game.setPlayerOneField(field1);
        game.setPlayerTwoField(field2);
        assertFalse(game.playersReady());
    }

    @Test
    public void testPlayersReadyReturnsFalseWhenPlayerTwoIdIsNull() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId(null);
        game.setPlayerOneField(field1);
        game.setPlayerTwoField(field2);
        assertFalse(game.playersReady());
    }

    @Test
    public void testPlayersReadyReturnsFalseWhenPlayerOneFieldIsNull() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerOneField(null);
        game.setPlayerTwoField(field2);
        assertFalse(game.playersReady());
    }

    @Test
    public void testPlayersReadyReturnsFalseWhenPlayerTwoFieldIsNull() {
        game.setPlayerOneId("player1");
        game.setPlayerTwoId("player2");
        game.setPlayerOneField(field1);
        game.setPlayerTwoField(null);
        assertFalse(game.playersReady());
    }

    @Test
    public void testPlayerReadyReturnsTrue() {
        game.setPlayerOneId("player1");
        game.setPlayerOneField(field1);
        assertTrue(game.playerReady("player1"));
    }

    @Test
    public void testPlayerReadyReturnsFalse() {
        game.setPlayerOneId("player1");
        game.setPlayerOneField(null);
        assertFalse(game.playerReady("player1"));
    }
}
