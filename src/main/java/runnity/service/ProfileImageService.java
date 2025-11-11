package runnity.service;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import runnity.domain.ProfileImage;
import runnity.domain.User;
import runnity.repository.ProfileImageRepository;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

  private final ProfileImageRepository profileImageRepository;
  private final S3Service s3;

  public void updateProfileImage(User user, MultipartFile file) throws IOException {
    String key = "profiles/" + user.getUserId() + "/" + UUID.randomUUID();
    String url = s3.upload(file, key);

    ProfileImage img = profileImageRepository.findByUser(user)
        .orElse(new ProfileImage());

    img.setUser(user);
    img.setS3Key(key);
    img.setUrl(url);
    img.setTitle(user.getNickname() + " 의 프로필 이미지.");
    profileImageRepository.save(img);
  }

}
