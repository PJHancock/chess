package service.requests;

import chess.ChessGame;

public record JoinGameRequest(ChessGame.TeamColor playerColor, int gameID, String authToken) {}
