package runnity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRequest {
//    private String prompt;
    private String message;


    // getters / setters
//    public String getPrompt() { return prompt; }
//    public void setPrompt(String prompt) { this.prompt = prompt; }
}
