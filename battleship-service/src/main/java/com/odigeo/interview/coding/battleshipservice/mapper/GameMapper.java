package com.odigeo.interview.coding.battleshipservice.mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.repository.entity.GameEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Type;
import java.util.Iterator;

@Mapper
public interface GameMapper {

    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameEntity map(Game source);
    Game map(GameEntity source);

    default String serializeField(Cell[][] source) {
        return new Gson().toJson(source);
    }

    default Cell[][] deserializeField(String source) {
        return new GsonBuilder()
                .registerTypeAdapter(Ship.class, new ShipJsonDeserializer())
                .create().fromJson(source, Cell[][].class);
    }

    class ShipJsonDeserializer implements JsonDeserializer<Ship> {

        @Override
        public Ship deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            final String shipType = jsonObject.get("shipType").getAsString();
            Ship ship = ShipType.valueOf(shipType).newInstance();
            Iterator<JsonElement> coordinatesIt = jsonElement.getAsJsonObject().get("coordinates").getAsJsonArray().iterator();
            while (coordinatesIt.hasNext()) {
                JsonObject coordinate = (JsonObject) coordinatesIt.next();
                ship.getCoordinates().add(new Coordinate(coordinate.get("value").getAsString(), coordinate.get("column").getAsInt(), coordinate.get("row").getAsInt()));
            }
            return ship;
        }

    }
}
