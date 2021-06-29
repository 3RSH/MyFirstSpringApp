package main.service.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import main.api.ImageErrors;
import main.api.response.ImageErrorsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

  private static final String[] symbols = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

  private ImageErrorsResponse errorsResponse;


  @Override
  public ResponseEntity<?> addImage(MultipartFile file) {
    return isValidImage(file)
        ? new ResponseEntity<>(createFile(file, getDir(file)), HttpStatus.OK)
        : new ResponseEntity<>(errorsResponse, HttpStatus.BAD_REQUEST);
  }


  private boolean isValidImage(MultipartFile file) {
    ImageErrors errors = new ImageErrors();
    String contentType = file.getContentType();

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

    if (file.getSize() > 1_000_000) {
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

    return new File("target/classes/static/upload/" +
        symbols[Short.parseShort(hash.substring(1, 2))] +
        symbols[Short.parseShort(hash.substring(2, 3))] +
        "/" + symbols[Short.parseShort(hash.substring(3, 4))] +
        symbols[Short.parseShort(hash.substring(4, 5))] +
        "/" + symbols[Short.parseShort(hash.substring(5, 6))] +
        symbols[Short.parseShort(hash.substring(6, 7))]);
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

    return convertFile.getPath().split("static")[1];
  }
}