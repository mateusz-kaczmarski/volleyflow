package pl.volleyflow.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public final class UserRegisteredEvent extends ApplicationEvent {
    private final UUID userExternalId;
    private final String userEmail;
    private final String userName;
    private final String userLastName;

    public UserRegisteredEvent(Object source,
                               UUID userExternalId,
                               String userEmail,
                               String userName,
                               String userLastName) {
        super(source);
        this.userExternalId = userExternalId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userLastName = userLastName;
    }

}
