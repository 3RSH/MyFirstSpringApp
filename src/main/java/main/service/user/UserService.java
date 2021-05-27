package main.service.user;

import java.util.Map;
import main.api.response.RegisterResponse;

public interface UserService {

  RegisterResponse addUser(Map<String, String> registerRequest);
}