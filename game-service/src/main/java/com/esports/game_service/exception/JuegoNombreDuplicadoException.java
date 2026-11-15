package com.esports.game_service.exception;

public class JuegoNombreDuplicadoException extends RuntimeException {
    public JuegoNombreDuplicadoException() {
        super("Ya existe un juego con ese nombre.");
    }
}
