package main.service.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.DatatypeConverter;
import main.api.RegisterErrors;
import main.api.RestoreErrors;
import main.api.request.EditProfileRequest;
import main.api.request.GetRestoreRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.EditProfileResponse;
import main.api.response.RegisterResponse;
import main.api.response.RestoreResponse;
import main.model.User;
import main.repository.captcha.CaptchaRepository;
import main.repository.users.UsersRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private static final String USERNAME_REGEX = "[A-Za-zА-Яа-яё0-9\\-]{2,20}\\s*"
      + "[A-Za-zА-Яа-яё0-9\\-]{0,20}\\s*[A-Za-zА-Яа-яё0-9\\-]{0,20}";
  private static final String EMAIL_REGEX = "^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]"
      + "+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$";
  private static final String EMPTY_STRING = "";
  private static final String DIGEST_TYPE = "MD5";
  private static final String RESTORE_EMAIL_TITLE = "Восстановление пароля";
  private static final String RESTORE_EMAIL_TEXT = "Для смены пароля перейдите по ссылке: "
      + "<a href=\"%s\">%s</a>";
  private static final String RESTORE_EMAIL_TYPE = "text/html; charset=UTF-8";
  private static final String CAPTCHA_ERROR_MESSAGE = "Код с картинки введён неверно";
  private static final String PASSWORD_ERROR_MESSAGE = "Пароль короче 6-ти символов";
  private static final String EMAIL_ERROR_MESSAGE = "Почта указана неверно";
  private static final String USERNAME_ERROR_MESSAGE = "Имя указано неверно";
  private static final String DOUBLE_EMAIL_ERROR_MESSAGE = "Этот e-mail уже зарегистрирован";
  private static final String LINK_ERROR_MESSAGE = "Ссылка для восстановления пароля устарела. "
      + "<a href=\"/login/restore-password\">Запросить ссылку снова</a>";
  private static final String CLOUDINARY_HOME = "devBlog";

  private static final String SENDER_EMAIL = "dev_blog@mail.ru";
  private static final String SENDER_HOST = "smtp.mail.ru";
  private static final String SENDER_NAME = "dev_blog@mail.ru";

  private static final String AUTH_PROPERTY = "mail.smtp.auth";
  private static final String STARTTLS_PROPERTY = "mail.smtp.starttls.enable";
  private static final String HOST_PROPERTY = "mail.smtp.host";
  private static final String PORT_PROPERTY = "mail.smtp.port";
  private static final String SSL_PROPERTY = "mail.smtp.ssl.enable";
  private static final String TRUE_PROPERTY_VALUE = "true";
  private static final String PORT_PROPERTY_VALUE = "465";

  private static final short NOT_MODERATOR_MARKER = -1;
  private static final int MIN_PASSWORD_SIZE = 6;
  private static final Cloudinary imageCloud = new Cloudinary();

  private final UsersRepository usersRepository;
  private final CaptchaRepository captchaRepository;

  @Value("${blog.mail.password}")
  private String senderPassword;

  @Value("${blog.mail.restoreUrl}")
  private String restoreEmailLink;


  public UserServiceImpl(@Qualifier("UsersRepository") UsersRepository usersRepository,
      @Qualifier("CaptchaRepository") CaptchaRepository captchaRepository) {

    this.usersRepository = usersRepository;
    this.captchaRepository = captchaRepository;
  }


  @Override
  public synchronized RegisterResponse addUser(RegisterRequest request) {
    RegisterResponse response = getRegisterResponse(
        request.getPassword(),
        request.getName(),
        request.getEmail(),
        request.getCaptcha(),
        request.getCaptchaSecret());

    if (!response.isResult()) {
      return response;
    }

    User user = new User();

    user.setIsModerator(NOT_MODERATOR_MARKER);
    user.setRegTime(Timestamp.valueOf(LocalDateTime.now()));
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder().encode(request.getPassword()));

    usersRepository.saveAndFlush(user);

    return response;
  }

  @Override
  public EditProfileResponse editUser(EditProfileRequest request) {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    EditProfileResponse response = new EditProfileResponse();
    RegisterErrors errors = new RegisterErrors();

    response.setResult(isCorrectUserData(
        request.getName(),
        request.getEmail(),
        request.getPassword(),
        errors));

    if (!user.getEmail().equals(request.getEmail()) &&
        usersRepository.findFirstByEmail(request.getEmail()) != null) {

      errors.setEmail(DOUBLE_EMAIL_ERROR_MESSAGE);
      response.setResult(false);
    }

    if (!response.isResult()) {
      response.setErrors(errors);

      return response;
    }

    if (!user.getName().equals(request.getName())) {
      user.setName(request.getName());
    }

    user.setEmail(request.getEmail());

    if (request.getPassword() != null) {
      user.setPassword(passwordEncoder().encode(request.getPassword()));
    }

    if (request.getRemovePhoto() == 1) {
      if (user.getPhoto() != null) {
        String imageId = user.getPhoto();

        imageId = imageId.substring(
            imageId.indexOf(CLOUDINARY_HOME), imageId.lastIndexOf("."));

        try {
          imageCloud.uploader().destroy(imageId, ObjectUtils.emptyMap());

        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      user.setPhoto(null);

    } else {
      if (request.getPhoto() != null) {
        user.setPhoto(request.getPhoto());
      }
    }

    usersRepository.saveAndFlush(user);
    SecurityContextHolder.clearContext();

    return response;
  }

  @Override
  public RestoreResponse sendRestoreRequestUser(GetRestoreRequest request) {
    RestoreResponse response = new RestoreResponse();
    User user = usersRepository.findFirstByEmail(request.getEmail());

    if (user == null) {
      return response;
    }

    String hash = getHash(user);
    user.setCode(hash);

    usersRepository.saveAndFlush(user);

    sendRestoreEmail(request.getEmail(), hash);
    response.setResult(true);

    return response;
  }

  @Override
  public RestoreResponse restoreUser(RestoreRequest request) {
    RestoreResponse response = new RestoreResponse();
    RestoreErrors errors = new RestoreErrors();

    User user = usersRepository.findFirstByCode(request.getCode());

    if (user == null) {
      errors.setCode(LINK_ERROR_MESSAGE);
      response.setResult(false);
    }

    String password = request.getPassword();

    if (password != null && password.length() < MIN_PASSWORD_SIZE) {
      errors.setPassword(PASSWORD_ERROR_MESSAGE);
      response.setResult(false);
    }

    String captcha = request.getCaptcha();
    String captchaSecret = request.getCaptchaSecret();

    if (!captcha.equals(captchaRepository.findFirstBySecretCode(captchaSecret).getCode())) {
      errors.setCaptcha(CAPTCHA_ERROR_MESSAGE);
      response.setResult(false);
    }

    if (!response.isResult()) {
      response.setErrors(errors);
      return response;
    }

    user.setCode(null);
    user.setPassword(passwordEncoder().encode(password));
    usersRepository.saveAndFlush(user);

    return response;
  }


  private void sendRestoreEmail(String email, String hash) {
    Properties properties = System.getProperties();
    properties.put(AUTH_PROPERTY, TRUE_PROPERTY_VALUE);
    properties.put(STARTTLS_PROPERTY, TRUE_PROPERTY_VALUE);
    properties.put(HOST_PROPERTY, SENDER_HOST);
    properties.put(PORT_PROPERTY, PORT_PROPERTY_VALUE);
    properties.put(SSL_PROPERTY, TRUE_PROPERTY_VALUE);

    Session session = Session.getDefaultInstance(properties,
        new javax.mail.Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SENDER_NAME, senderPassword);
          }
        });

    try {
      MimeMessage message = new MimeMessage(session);

      message.setFrom(new InternetAddress(SENDER_EMAIL));
      message.addRecipient(RecipientType.TO, new InternetAddress(email));
      message.setSubject(RESTORE_EMAIL_TITLE);

      String link = restoreEmailLink + hash;

      String text = String.format(RESTORE_EMAIL_TEXT, link, link);

      MimeBodyPart mimeBodyPart = new MimeBodyPart();
      mimeBodyPart.setContent(text, RESTORE_EMAIL_TYPE);

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(mimeBodyPart);

      message.setContent(multipart);

      Transport.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  private String getHash(User user) {
    String string = user.getPassword() + user.getEmail();
    String hash = EMPTY_STRING;

    try {
      MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_TYPE);

      messageDigest.update(string.getBytes());
      byte[] digest = messageDigest.digest();
      hash = DatatypeConverter.printHexBinary(digest).toLowerCase();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return hash;
  }

  private RegisterResponse getRegisterResponse(
      String password, String name, String email, String captcha, String captchaSecret) {

    RegisterResponse response = new RegisterResponse();
    RegisterErrors errors = new RegisterErrors();

    if (!captcha.equals(captchaRepository.findFirstBySecretCode(captchaSecret).getCode())) {
      errors.setCaptcha(CAPTCHA_ERROR_MESSAGE);
      response.setResult(false);
    }

    if (!isCorrectUserData(name, email, password, errors)) {
      response.setResult(false);
    }

    if (usersRepository.findFirstByEmail(email) != null) {
      errors.setEmail(DOUBLE_EMAIL_ERROR_MESSAGE);
      response.setResult(false);
    }

    if (!response.isResult()) {
      response.setErrors(errors);
    }

    return response;
  }

  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  private boolean isCorrectUserData(
      String name, String email, String password, RegisterErrors errors) {

    boolean result = true;

    if (!name.matches(USERNAME_REGEX)) {
      errors.setName(USERNAME_ERROR_MESSAGE);
      result = false;
    }

    if (!email.matches(EMAIL_REGEX)) {
      errors.setEmail(EMAIL_ERROR_MESSAGE);
      result = false;
    }

    if (password != null && password.length() < MIN_PASSWORD_SIZE) {
      errors.setPassword(PASSWORD_ERROR_MESSAGE);
      result = false;
    }

    return result;
  }
}