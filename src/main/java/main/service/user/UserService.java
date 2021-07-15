package main.service.user;

import java.util.Map;
import main.api.response.RegisterResponse;
import main.api.response.RestoreResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {

  RegisterResponse addUser(Map<String, String> registerRequest);

  ResponseEntity<?> editUser(Map<String, String> editRequest);

  RestoreResponse restoreUser(String email);
}