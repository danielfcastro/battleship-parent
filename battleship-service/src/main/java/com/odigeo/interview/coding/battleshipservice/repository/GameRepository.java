package com.odigeo.interview.coding.battleshipservice.repository;

import com.odigeo.interview.coding.battleshipservice.model.Game;

import java.util.List;
import java.util.Optional;


public interface GameRepository {

    public void saveOrUpdateGame(Game game);

    public Optional<Game> getGame(String id);

    public List<Game> getGames();

}
