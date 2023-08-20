package com.myapp.news.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.myapp.news.dtos.Comment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class CommentJsonFileHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
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
}
