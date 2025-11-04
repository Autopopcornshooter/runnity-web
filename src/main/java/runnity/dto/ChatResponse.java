package runnity.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private Action action;
    private String content;

    @Data
    public static class Action {
        private String name;
        private String speak;
    }
}
