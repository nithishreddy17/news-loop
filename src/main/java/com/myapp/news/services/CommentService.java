package com.myapp.news.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.myapp.news.dtos.Comment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final ObjectMapper objectMapper;

    public CommentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Comment> getCommentsByArticleId(long newsArticleId, int page, int pageSize) throws IOException {
        // Load comments from the JSON file in resources
        List<Comment> allComments = objectMapper.readValue(
                new ClassPathResource("comments.json").getFile(),
                new TypeReference<List<Comment>>() {});

        // Filter comments by newsArticleId
        List<Comment> filteredComments = allComments.stream()
                .filter(comment -> comment.getNewsArticleId() == newsArticleId)
                .collect(Collectors.toList());

        // Calculate pagination boundaries
        int startIndex = (page - 1) * pageSize;
        int endIndex = page * pageSize;

        // Ensure that startIndex and endIndex are within valid bounds
        int totalComments = filteredComments.size();
        startIndex = Math.min(startIndex, totalComments);
        endIndex = Math.min(endIndex, totalComments);

        // Ensure that startIndex is less than or equal to endIndex
        if (startIndex > endIndex) {
            return Collections.emptyList(); // Return an empty list or handle the error as needed
        }

        // Apply sorting by commentedOn date
        filteredComments.sort((c1, c2) -> c1.getCommentedOn().compareTo(c2.getCommentedOn()));

        // Paginate and return the comments
        return filteredComments.subList(startIndex, endIndex);
    }

}
