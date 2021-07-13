package main.service.image;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

  ResponseEntity<?> addImage(MultipartFile file);

  ResponseEntity<?> addAvatar(MultipartFile file);
}