package com.myapp.news.dtos;


import lombok.Getter;
import lombok.Setter;

import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Comment {
    private long commentId;
    private long newsArticleId;
    private User author;
    private String text;
    private Date commentedOn;
    private Map<String, Long> reactions; // [heart : 20, thumbsup : 10]
    //private Comment parent;
    //private List<Comment> replies;
}
