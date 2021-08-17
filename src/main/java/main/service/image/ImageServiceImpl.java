package main.service.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import org.apache.maven.shared.utils.io.FileUtils;
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
  private static final String FILE_EXTENSION_SEPARATOR = ".";
  private static final String JPG_CONTENT_TYPE = "image/jpeg";
  private static final String PNG_CONTENT_TYPE = "image/png";
  private static final String EMPTY_FILE_ERROR = "Файл пустой";
  private static final String FORMAT_FILE_ERROR = "Недопустимый формат файла";
  private static final String SIZE_FILE_ERROR = "Размер файла превышает допустимый размер";
  private static final String CLOUDINARY_HOME = "devBlog";
  private static final String UPLOADER_FOLDER_PROP = "folder";
  private static final String AVATARS_FOLDER = "devBlog/avatars";
  private static final String TEMP_FOLDER = "devBlog/temp/";
  private static final String URL_TYPE = "secure_url";

  private static final Cloudinary imageCloud = new Cloudinary();

  private final UsersRepository usersRepository;


  public ImageServiceImpl(@Qualifier("UsersRepository") UsersRepository usersRepository) {
    this.usersRepository = usersRepository;
  }


  @Override
  public ImageResponse addImage(MultipartFile file) {
    ImageResponse response = new ImageResponse();

    if (isValidImage(file, response)) {
      try {
        createFile(file, response);

      } catch (Exception e) {
        e.printStackTrace();
      }
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
      String imageId = user.getPhoto();
      imageId = imageId.substring(
          imageId.indexOf(CLOUDINARY_HOME), imageId.lastIndexOf("."));

      try {
        imageCloud.uploader().destroy(imageId, ObjectUtils.emptyMap());

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

      String filename = file.getOriginalFilename();

      if (filename != null) {
        File imageFile = new File(filename);

        String format = filename.substring(
            file.getOriginalFilename().lastIndexOf(FILE_EXTENSION_SEPARATOR)
                + INCREMENT_INDEX);

        ImageIO.write(newImage, format, imageFile);

        response.setImagePath(
            imageCloud.uploader()
                .upload(
                    imageFile,
                    ObjectUtils.asMap(UPLOADER_FOLDER_PROP, AVATARS_FOLDER))
                .get(URL_TYPE)
                .toString());

        FileUtils.delete(imageFile);
      }
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

  private void createFile(MultipartFile file, ImageResponse response) throws Exception {
    File imageFile = new File(Objects.requireNonNull(file.getOriginalFilename()));

    try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
      outputStream.write(file.getBytes());
    }

    SecurityContext currentContext = SecurityContextHolder.getContext();
    User user = usersRepository.findFirstByEmail(
        currentContext.getAuthentication().getName());

    response.setImagePath(
        imageCloud.uploader()
            .upload(
                imageFile,
                ObjectUtils.asMap(UPLOADER_FOLDER_PROP, TEMP_FOLDER + user.getId()))
            .get(URL_TYPE)
            .toString());

    FileUtils.delete(imageFile);
    response.setResult(true);
  }
}