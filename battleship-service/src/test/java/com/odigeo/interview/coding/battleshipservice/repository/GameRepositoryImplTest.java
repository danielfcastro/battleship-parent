package com.odigeo.interview.coding.battleshipservice.repository;

import com.odigeo.interview.coding.battleshipservice.model.Game;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.time.Instant;
import java.util.Random;

public class GameRepositoryImplTest {

    private GameRepository gameRepository;

    @BeforeMethod
    public void setUp() {
        gameRepository = new GameRepositoryImpl();
    }

    @Test
    public void testNewGame() {
        assertTrue(gameRepository.getGames().size() == 0);
    }


    @Test
    public void testSaveGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        assertTrue(gameRepository.getGames().size() > 0);
        assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
    }

    @Test
    public void testUpdateGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        assertNotNull(gameRepository.getGame(mygame.getId()));

        Game finishedGame = gameRepository.getGame(mygame.getId()).get();
        finishedGame.setWinner("P1");
        finishedGame.setFinishedAt(Instant.now());
        gameRepository.saveOrUpdateGame(finishedGame);
        assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
        assertTrue(gameRepository.getGame(mygame.getId()).get().isFinished());
    }


    @Test
    public void testGetGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        assertTrue(gameRepository.getGames().size() > 0);
        assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
    }


    private Game buildNewGame() {
        Game newGame = new Game();
        newGame.setId(String.valueOf(new Random().nextInt()));
        newGame.setCreatedAt(Instant.now());
        newGame.setPlayerOneId("P1");
        newGame.isVsComputer();
        newGame.setPlayerTurn(1);
        return newGame;
    }
}
