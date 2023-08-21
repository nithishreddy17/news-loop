package com.myapp.news.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.myapp.news.dtos.Comment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentJsonFileHandler {

            @Value("${comments.file.path}")
            private String commentsFilePath;

            private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);


    private final String jsonFilePath = "classpath:comments.json";

    public List<Comment> readComments() throws IOException {
        Resource resource = new ClassPathResource(jsonFilePath);
        File jsonFile = resource.getFile();

        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CollectionType collectionType = typeFactory.constructCollectionType(List.class, Comment.class);

        return objectMapper.readValue(jsonFile, collectionType);
    }

    public void writeComments(List<Comment> comments) throws IOException {
        Resource resource = new ClassPathResource(jsonFilePath);
        File jsonFile = resource.getFile();

        objectMapper.writeValue(jsonFile, comments);
    }

    public List<Comment> readCommentsFromJsonFile() throws IOException {
        Resource resource = new ClassPathResource(commentsFilePath);
        return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Comment>>() {});
    }

    public static List<Comment> filterCommentsByArticleId(List<Comment> comments, long newsArticleId) {
        return comments.stream()
                .filter(comment -> comment.getNewsArticleId() == newsArticleId)
                .collect(Collectors.toList());
    }

}
