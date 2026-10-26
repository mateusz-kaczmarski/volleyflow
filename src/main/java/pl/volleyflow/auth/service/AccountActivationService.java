package pl.volleyflow.auth.service;

import java.util.UUID;

public interface AccountActivationService {

    void requestActivation(UUID userExternalId, String email, String name, String lastName);

}
