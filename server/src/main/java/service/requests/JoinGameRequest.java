package service.requests;

import chess.ChessGame;

public record JoinGameRequest(ChessGame.TeamColor playerColor, String gameID, String authToken) {}
