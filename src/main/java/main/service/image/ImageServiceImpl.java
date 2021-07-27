package main.service.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import main.api.ImageErrors;
import main.api.response.ImageResponse;
import main.model.User;
import main.repository.users.UsersRepository;
import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

  private static final int MAX_IMAGE_SIZE = 1_000_000;
  private static final int MAX_AVATAR_EXTENSION = 360;
  private static final int INCREMENT_INDEX = 1;
  private static final int FILE_LEVELS_COUNT = 3;
  private static final String FILE_EXTENSION_SEPARATOR = ".";
  private static final String SLASH_REGEX = "/";
  private static final String BACKSLASH_REGEX = "\\\\";
  private static final String STATIC_REGEX = "static";
  private static final String STATIC_PATH = "target/classes/static";
  private static final String AVATARS_PATH = "target/classes/static/avatars/";
  private static final String UPLOAD_PATH = "target/classes/static/upload/";
  private static final String JPG_CONTENT_TYPE = "image/jpeg";
  private static final String PNG_CONTENT_TYPE = "image/png";
  private static final String EMPTY_FILE_ERROR = "Файл пустой";
  private static final String FORMAT_FILE_ERROR = "Недопустимый формат файла";
  private static final String SIZE_FILE_ERROR = "Размер файла превышает допустимый размер";

  private static final String[] SYMBOLS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

  private final UsersRepository usersRepository;


  public ImageServiceImpl(@Qualifier("UsersRepository") UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }


  @Override
  public ImageResponse addImage(MultipartFile file) {
    ImageResponse response = new ImageResponse();

    if (isValidImage(file, response)) {
      createFile(file, getDir(file), response);
    }

    return response;
  }

  @Override
  public ImageResponse addAvatar(MultipartFile file) {
    ImageResponse response = new ImageResponse();

    if (!isValidImage(file, response)) {
      return response;
    }

    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    if (user.getPhoto() != null) {
      File oldAvatar = new File(STATIC_PATH +
          user.getPhoto().replaceAll(BACKSLASH_REGEX, SLASH_REGEX));

      try {
        FileUtils.deleteDirectory(oldAvatar.getParentFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

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

      File dir = new File(AVATARS_PATH +
          user.getId());

      File image = new File(dir.getPath() + SLASH_REGEX + file.getOriginalFilename());

      if (dir.mkdirs()) {
        if (image.createNewFile()) {
          String format = Objects.requireNonNull(file.getOriginalFilename()).
              substring(file.getOriginalFilename().lastIndexOf(FILE_EXTENSION_SEPARATOR)
                  + INCREMENT_INDEX);

          ImageIO.write(newImage, format, image);
        }
      }

      response.setImagePath(image.getPath().split(STATIC_REGEX)[INCREMENT_INDEX]);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return response;
  }


  private boolean isValidImage(MultipartFile file, ImageResponse response) {
    ImageErrors errors = new ImageErrors();
    String contentType = file.getContentType();

    if (file.isEmpty()) {
      errors.setSize(EMPTY_FILE_ERROR);
      response.setImageErrors(errors);
      return false;
    }

    if (contentType == null ||
        (!contentType.equals(JPG_CONTENT_TYPE) && !contentType.equals(PNG_CONTENT_TYPE))) {
      errors.setImage(FORMAT_FILE_ERROR);
      response.setImageErrors(errors);
      return false;
    }

    if (file.getSize() > MAX_IMAGE_SIZE) {
      errors.setSize(SIZE_FILE_ERROR);
      response.setImageErrors(errors);
      return false;
    }

    response.setResult(true);
    return true;
  }

  private File getDir(MultipartFile file) {
    String hash = String.valueOf(
        String.valueOf(Math.random()).hashCode() +
            Objects.requireNonNull(file.getOriginalFilename()).hashCode());

    int index = INCREMENT_INDEX;

    StringBuilder pathFile = new StringBuilder().append(UPLOAD_PATH);

    for (int i = 0; i < FILE_LEVELS_COUNT; i++) {
      pathFile
          .append(SYMBOLS[Short.parseShort(hash.substring(index, ++index))])
          .append(SYMBOLS[Short.parseShort(hash.substring(index, ++index))])
          .append(i == FILE_LEVELS_COUNT - 1 ? "" : SLASH_REGEX);
    }

    return new File(pathFile.toString());
  }

  private void createFile(MultipartFile file, File dir, ImageResponse response) {
    File convertFile = new File(dir.getPath() + SLASH_REGEX + file.getOriginalFilename());

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

    response.setResult(true);
    response.setImagePath(convertFile.getPath().split(STATIC_REGEX)[INCREMENT_INDEX]);
  }
}