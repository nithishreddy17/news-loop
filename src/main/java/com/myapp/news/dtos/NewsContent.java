package com.myapp.news.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NewsContent {
    String news_id;
    String headline;
    String content;
}
