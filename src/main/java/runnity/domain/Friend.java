package runnity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long userId;

    @Column
    private String nickname;

    @Column
    private String runner_level;

    @Column
    private String address;

//    @Column
//    private String message;

    @Column
    private Integer likecount = 0;
}
