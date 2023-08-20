package com.myapp.news.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private long userId;
    private String avatarUrl;
    private String userProfilePicUrl;
    private String userName;
    private String emailAddress;
}
