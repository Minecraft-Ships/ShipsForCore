package org.ships.exceptions.move;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.movement.MovementContext;

import java.io.IOException;

public class MoveException extends IOException {

    private final @NotNull AText errorMessage;
    private final @NotNull MovementContext context;
    private final @NotNull Message<?> message;
    private final @NotNull Object data;

    public <T> MoveException(@NotNull MovementContext context, @NotNull Message<T> message, @NotNull T data) {
        this(context, message.process(data), message, data);
    }

    public <T> MoveException(@NotNull MovementContext context,
                             @NotNull AText errorMessage,
                             @NotNull Message<T> message,
                             @NotNull T data) {
        super(errorMessage.toPlain());
        this.context = context;
        this.errorMessage = errorMessage;
        this.message = message;
        this.data = data;
    }

    public @NotNull AText getErrorMessageText() {
        return this.errorMessage;
    }

    public @NotNull MovementContext getContext() {
        return this.context;
    }

    public @NotNull Message<?> getDisplayMessage() {
        return this.message;
    }

    public @NotNull Object getData() {
        return this.data;
    }
}
