package com.myapp.news.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class NewsArticle {
    long newsArticleId;
    String title;
    String content;
    Date postedOn;
    Date lastModified;
    User author;
}
