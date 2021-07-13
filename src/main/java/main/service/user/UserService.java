package main.service.user;

import java.util.Map;
import main.api.response.RegisterResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {

  RegisterResponse addUser(Map<String, String> registerRequest);

  ResponseEntity<?> editUser(Map<String, String> editRequest);
}