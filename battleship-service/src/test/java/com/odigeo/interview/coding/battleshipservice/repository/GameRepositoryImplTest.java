package com.odigeo.interview.coding.battleshipservice.repository;

import com.odigeo.interview.coding.battleshipservice.model.Game;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
        Assert.assertTrue(gameRepository.getGames().size() == 0);
    }


    @Test
    public void testSaveGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        Assert.assertTrue(gameRepository.getGames().size() > 0);
        Assert.assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
    }

    @Test
    public void testUpdateGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        Assert.assertNotNull(gameRepository.getGame(mygame.getId()));

        Game finishedGame = gameRepository.getGame(mygame.getId()).get();
        finishedGame.setWinner("P1");
        finishedGame.setFinishedAt(Instant.now());
        gameRepository.saveOrUpdateGame(finishedGame);
        Assert.assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
        Assert.assertTrue(gameRepository.getGame(mygame.getId()).get().isFinished());
    }


    @Test
    public void testGetGame() {
        Game mygame = buildNewGame();
        gameRepository.saveOrUpdateGame(mygame);
        Assert.assertTrue(gameRepository.getGames().size() > 0);
        Assert.assertTrue(gameRepository.getGame(mygame.getId()).isPresent());
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
