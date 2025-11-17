package runnity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendInfo {
    private Long userId;
    private String nickname;
    private String runner_level;
    private String address;
    private String message;
    private Integer likecount;

    public FriendInfo(Long userId, String nickname, String runner_level, String address, Integer likecount) {
        this.userId = userId;
        this.nickname = nickname;
        this.runner_level = runner_level;
        this.address = address;
        this.likecount = likecount;
    }
}
