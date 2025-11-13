package runnity.domain;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "likecount")
    private Integer likecount = 0;

    // id, likecount를 제외한 생성자
    public Friend(Long userId, String nickname, String runner_level, String address, Integer likecount) {
        this.userId = userId;
        this.nickname = nickname;
        this.runner_level = runner_level;
        this.address = address;
        this.likecount = likecount;
    }
}
