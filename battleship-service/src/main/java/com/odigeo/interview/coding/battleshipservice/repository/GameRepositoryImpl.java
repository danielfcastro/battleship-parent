package com.odigeo.interview.coding.battleshipservice.repository;

import com.odigeo.interview.coding.battleshipservice.mapper.GameMapper;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.repository.entity.GameEntity;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

@Singleton
public class GameRepositoryImpl implements GameRepository {

    private Map<String, GameEntity> dataSource = new ConcurrentHashMap<>();

    public void saveOrUpdateGame(Game game) {
        GameEntity gameEntity = GameMapper.INSTANCE.map(game);
        dataSource.put(gameEntity.getId(), gameEntity);
    }

    public Optional<Game> getGame(String id) {
        GameEntity gameEntity = dataSource.get(id);
        Game game = GameMapper.INSTANCE.map(gameEntity);
        return Optional.ofNullable(game);
    }

    public List<Game> getGames() {
        Collection<GameEntity> gameEntities = dataSource.values();
        return gameEntities.stream().map(GameMapper.INSTANCE::map).collect(toList());
    }

}
