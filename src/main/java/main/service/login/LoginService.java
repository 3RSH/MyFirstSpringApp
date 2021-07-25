package main.service.login;

import java.security.Principal;
import main.api.request.LoginRequest;
import main.api.response.CheckLoginResponse;

public interface LoginService {

  CheckLoginResponse getLoginResponse(LoginRequest request);

  CheckLoginResponse getCheckResponse(Principal principal);
}