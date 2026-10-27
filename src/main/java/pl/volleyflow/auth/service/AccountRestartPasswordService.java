package pl.volleyflow.auth.service;

import java.util.UUID;

public interface AccountRestartPasswordService {

    void restartPasswordRequest(UUID userAccountExternalId, String email, String token);

}
