package com.odigeo.interview.coding.battleshipservice.controller.provider;

import com.odigeo.interview.coding.battleshipapi.contract.ErrorResponse;
import com.odigeo.interview.coding.battleshipservice.exception.BattleshipException;
import com.odigeo.interview.coding.battleshipservice.exception.GameFinishedException;
import com.odigeo.interview.coding.battleshipservice.exception.GameJoinException;
import com.odigeo.interview.coding.battleshipservice.exception.GameNotFoundException;
import com.odigeo.interview.coding.battleshipservice.exception.GameStartException;
import com.odigeo.interview.coding.battleshipservice.exception.KafkaProducerException;
import com.odigeo.interview.coding.battleshipservice.exception.NotYourTurnException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.exception.ShipsAlreadyDeployedException;
import com.odigeo.interview.coding.battleshipservice.exception.WrongCoordinateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Provider
public class BattleshipExceptionMapper implements ExceptionMapper<BattleshipException> {

    private static final Logger logger = LoggerFactory.getLogger(BattleshipExceptionMapper.class);

    private static final Map<Class<? extends BattleshipException>, Response.Status> exceptionHttpStatusMap;

    static {
        exceptionHttpStatusMap = new HashMap<>();
        exceptionHttpStatusMap.put(GameNotFoundException.class, Response.Status.NOT_FOUND);
        exceptionHttpStatusMap.put(GameStartException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(GameJoinException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(GameFinishedException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(NotYourTurnException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(ShipDeploymentException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(ShipsAlreadyDeployedException.class, Response.Status.BAD_REQUEST);
        exceptionHttpStatusMap.put(WrongCoordinateException.class, Response.Status.BAD_REQUEST);
    }

    @Override
    public Response toResponse(BattleshipException e) {
        logger.error("Battleship error: {}", e.getMessage());
        Class<? extends BattleshipException> exceptionClass = e.getClass();
        ErrorResponse error = new ErrorResponse(exceptionClass.getSimpleName(), e.getMessage());
        Response.Status httpStatus = Optional.ofNullable(exceptionHttpStatusMap.get(exceptionClass)).orElse(Response.Status.INTERNAL_SERVER_ERROR);
        return Response.status(httpStatus).entity(error).build();
    }

}
