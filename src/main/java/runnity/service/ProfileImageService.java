package runnity.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import runnity.domain.ProfileImage;
import runnity.domain.User;
import runnity.repository.ProfileImageRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageService {

  private final ProfileImageRepository profileImageRepository;
  private final S3Service s3;
  private final S3Client s3Client;

  public void updateProfileImage(User user, MultipartFile file) throws IOException {

    String key = "profiles/" + user.getUserId() + "/" + UUID.randomUUID();
    String url = s3.upload(file, key);
    log.info("업로드된 이미지 url: {}", url);
    ProfileImage img = profileImageRepository.findByUser(user)
        .orElse(new ProfileImage());

    img.setUser(user);
    img.setS3Key(key);
    img.setUrl(url);
    img.setUploadAt(LocalDateTime.now());
    img.setTitle(user.getNickname() + " 의 프로필 이미지.");
    profileImageRepository.save(img);
  }

  
  public void removeProfileImage(User user) {
    user.setProfileImage(null);
    profileImageRepository.findByUser(user).ifPresent(s3::remove);
  }

}
