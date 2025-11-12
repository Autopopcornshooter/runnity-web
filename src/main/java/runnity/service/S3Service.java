package runnity.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import runnity.domain.ProfileImage;
import runnity.repository.ProfileImageRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

  private final S3Client s3Client;
  private final ProfileImageRepository profileImageRepository;


  @Value("${cloud.aws.s3.bucket-name}")
  private String bucket;

  public String upload(MultipartFile file, String key) throws IOException {

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(file.getContentType())
        .acl("public-read")
        .build();

    s3Client.putObject(putObjectRequest,
        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    return "https://" + bucket + ".s3." + s3Client.serviceClientConfiguration().region()
        + ".amazonaws.com/" + key;
  }

  @Transactional
  public void remove(ProfileImage image) {

    profileImageRepository.delete(image);
    if (profileImageRepository.findById(image.getImageId()).isEmpty()) {
      log.info("프로필 이미지 삭제 성공");
    } else {
      log.error("프로필 이미지 삭제 실패");
    }
    
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(image.getS3Key())
          .build());
    } catch (Exception e) {
      log.error("S3 이미지 삭제 실패- Key: {}", image.getS3Key(), e);
    }
  }

}
