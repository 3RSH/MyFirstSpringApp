package main.service.login;

import java.security.Principal;
import java.util.Map;
import main.api.response.CheckLoginResponse;

public interface LoginService {

  CheckLoginResponse getLoginResponse(Map<String, String> loginRequest);

  CheckLoginResponse getCheckResponse(Principal principal);
}