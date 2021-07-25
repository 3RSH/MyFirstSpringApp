package main.service.image;

import main.api.response.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

  ImageResponse addImage(MultipartFile file);

  ImageResponse addAvatar(MultipartFile file);
}