package runnity.dto;


public record LoginResponse(String accessToken, long expireInSeconds) {

}
