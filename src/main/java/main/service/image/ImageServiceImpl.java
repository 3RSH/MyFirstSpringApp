package main.service.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import main.api.ImageErrors;
import main.api.response.ImageErrorsResponse;
import main.model.User;
import main.repository.users.UsersRepository;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

  private static final int MAX_IMAGE_SIZE = 1_000_000;
  private static final int MAX_AVATAR_EXTENSION = 360;
  private static final int INCREMENT_INDEX = 1;

  private static final String[] SYMBOLS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

  private final UsersRepository usersRepository;
  private ImageErrorsResponse errorsResponse;


  public ImageServiceImpl(@Qualifier("UsersRepository") UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }


  @Override
  public ResponseEntity<?> addImage(MultipartFile file) {
    return isValidImage(file)
        ? new ResponseEntity<>(createFile(file, getDir(file)), HttpStatus.OK)
        : new ResponseEntity<>(errorsResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  public ResponseEntity<?> addAvatar(MultipartFile file) {
    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    if (user.getPhoto() != null) {
      File oldAvatar = new File("target/classes/static" +
          user.getPhoto().replaceAll("\\\\", "/"));

      try {
        FileUtils.deleteDirectory(oldAvatar.getParentFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String path = "";

    try {
      BufferedImage originalImage;
      BufferedImage newImage;

      originalImage = ImageIO.read(file.getInputStream());

      if ((originalImage.getWidth() > MAX_AVATAR_EXTENSION)
          || (originalImage.getHeight() > MAX_AVATAR_EXTENSION)) {

        newImage = Scalr.resize(originalImage, MAX_AVATAR_EXTENSION);
      } else {
        newImage = originalImage;
      }

      File dir = new File("target/classes/static/avatars/" +
          user.getId());

      File image = new File(dir.getPath() + "/" + file.getOriginalFilename());

      if (dir.mkdirs()) {
        if (image.createNewFile()) {
          String format = Objects.requireNonNull(file.getOriginalFilename()).
              substring(file.getOriginalFilename().lastIndexOf(".")
                  + INCREMENT_INDEX);

          ImageIO.write(newImage, format, image);
        }
      }

      path = image.getPath().split("static")[INCREMENT_INDEX];
    } catch (IOException e) {
      e.printStackTrace();
    }

    return isValidImage(file)
        ? new ResponseEntity<>(path, HttpStatus.OK)
        : new ResponseEntity<>(errorsResponse, HttpStatus.BAD_REQUEST);
  }


  private boolean isValidImage(MultipartFile file) {
    ImageErrors errors = new ImageErrors();
    String contentType = file.getContentType();
    errorsResponse = new ImageErrorsResponse();

    if (file.isEmpty()) {
      errors.setSize("Файл пустой");
      errorsResponse.setImageErrors(errors);
      return false;
    }

    if (contentType == null ||
        (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
      errors.setImage("Недопустимый формат файла");
      errorsResponse.setImageErrors(errors);
      return false;
    }

    if (file.getSize() > MAX_IMAGE_SIZE) {
      errors.setSize("Размер файла превышает допустимый размер");
      errorsResponse.setImageErrors(errors);
      return false;
    }

    return true;
  }

  private File getDir(MultipartFile file) {
    String hash = String.valueOf(
        String.valueOf(Math.random()).hashCode() +
            Objects.requireNonNull(file.getOriginalFilename()).hashCode());

    int index = INCREMENT_INDEX;

    return new File("target/classes/static/upload/" +
        SYMBOLS[Short.parseShort(hash.substring(index, ++index))] +
        SYMBOLS[Short.parseShort(hash.substring(index, ++index))] +
        "/" + SYMBOLS[Short.parseShort(hash.substring(index, ++index))] +
        SYMBOLS[Short.parseShort(hash.substring(index, ++index))] +
        "/" + SYMBOLS[Short.parseShort(hash.substring(index, ++index))] +
        SYMBOLS[Short.parseShort(hash.substring(index, ++index))]);
  }

  private String createFile(MultipartFile file, File dir) {
    File convertFile = new File(dir.getPath() + "/" + file.getOriginalFilename());

    if (dir.mkdirs()) {
      try {

        if (convertFile.createNewFile()) {
          FileOutputStream outputStream = new FileOutputStream(convertFile);
          outputStream.write(file.getBytes());
          outputStream.close();
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return convertFile.getPath().split("static")[INCREMENT_INDEX];
  }
}