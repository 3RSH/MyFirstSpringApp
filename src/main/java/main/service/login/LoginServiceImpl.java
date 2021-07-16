package main.service.login;

import java.security.Principal;
import java.util.Map;
import main.api.response.CheckLoginResponse;
import main.api.response.UserResponse;
import main.model.User;
import main.repository.posts.PostsRepository;
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
  private final PostsRepository postsRepository;
  private final AuthenticationManager authenticationManager;


  public LoginServiceImpl(
      @Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("PostsRepository") PostsRepository postsRepository,
      AuthenticationManager authenticationManager) {
    this.usersRepository = usersRepository;
    this.postsRepository = postsRepository;
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

    user = usersRepository.findFirstByEmail(userDetails.getUsername());

    if (user.getCode() != null) {
      user.setCode(null);
      usersRepository.saveAndFlush(user);
    }

    return
        getCheckLoginResponse(user);
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

    userResponse.setId(user.getId());
    userResponse.setName(user.getName());
    userResponse.setPhoto(user.getPhoto());
    userResponse.setEmail(user.getEmail());

    if (user.getIsModerator() == 1) {
      userResponse.setModeration(true);
      userResponse.setSetting(true);
      userResponse.setModerationCount(postsRepository.getPendingPostsCount());
    }

    CheckLoginResponse checkLoginResponse = new CheckLoginResponse();

    checkLoginResponse.setResult(true);
    checkLoginResponse.setUserResponse(userResponse);

    return checkLoginResponse;
  }

  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}