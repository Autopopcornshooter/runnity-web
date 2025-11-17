package runnity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@NoArgsConstructor()
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProfileImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Long imageId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "s3_key", nullable = false)
  private String s3Key;

  @Column(name = "url", nullable = false)
  
  private String url;

//  @Column(name = "original_filename", nullable = false)
//  private String originalFilename;

  @CreatedDate
  @Column(name = "upload_at", nullable = false, updatable = false)
  private LocalDateTime uploadAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

}
