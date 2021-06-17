package main.service.login;

import java.security.Principal;
import java.util.Map;
import main.api.response.CheckLoginResponse;
import main.api.response.UserResponse;
import main.model.User;
import main.repository.users.UsersRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

  private final UsersRepository usersRepository;
  private final AuthenticationManager authenticationManager;


  public LoginServiceImpl(
      @Qualifier("UsersRepository") UsersRepository usersRepository,
      AuthenticationManager authenticationManager) {
    this.usersRepository = usersRepository;
    this.authenticationManager = authenticationManager;
  }


  @Override
  public CheckLoginResponse getLoginResponse(Map<String, String> loginRequest) {
    String email = loginRequest.get("e_mail");
    String password = loginRequest.get("password");

    User user = usersRepository.findFirstByEmail(email);

    if (user == null || !passwordEncoder().matches(password, user.getPassword())) {
      return new CheckLoginResponse();
    }

    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password));

    SecurityContextHolder.getContext().setAuthentication(auth);

    UserDetails userDetails = (UserDetails) auth.getPrincipal();

    return
        getCheckLoginResponse(usersRepository.findFirstByEmail(userDetails.getUsername()));
  }

  @Override
  public CheckLoginResponse getCheckResponse(Principal principal) {
    if (principal == null) {
      return new CheckLoginResponse();
    }

    return
        getCheckLoginResponse(usersRepository.findFirstByEmail(principal.getName()));
  }


  private CheckLoginResponse getCheckLoginResponse(User user) {
    UserResponse userResponse = new UserResponse();

    userResponse.setEmail(user.getEmail());
    userResponse.setId(user.getId());
    userResponse.setModeration(user.getIsModerator() == 1);

    CheckLoginResponse checkLoginResponse = new CheckLoginResponse();

    checkLoginResponse.setResult(true);
    checkLoginResponse.setUserResponse(userResponse);

    return checkLoginResponse;
  }

  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}