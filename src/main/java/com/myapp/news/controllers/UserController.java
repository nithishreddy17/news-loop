package com.myapp.news.controllers;

import com.myapp.news.dtos.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final List<User> userList = new ArrayList<>();
    private long userIdCounter = 1;

    // Create a new user
    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user and assign a unique userId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid user object"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<User> createUser(@RequestBody User newUser) {
        newUser.setUserId(userIdCounter++);
        userList.add(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // Retrieve all users
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users.")
    @ApiResponse(responseCode = "200", description = "List of users", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = List.class, type = "User"))
    })
    @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userList);
    }

    // Retrieve a user by ID
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their userId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<User> getUserById(@PathVariable long userId) {
        Optional<User> user = userList.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();

        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a user by ID
    @PutMapping("/{userId}")
    @Operation(summary = "Update user by ID", description = "Update an existing user's details by their userId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid user object"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<User> updateUser(@PathVariable long userId, @RequestBody User updatedUser) {
        Optional<User> userToUpdate = userList.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();

        if (userToUpdate.isPresent()) {
            User existingUser = userToUpdate.get();
            existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
            existingUser.setUserProfilePicUrl(updatedUser.getUserProfilePicUrl());
            existingUser.setUserName(updatedUser.getUserName());
            existingUser.setEmailAddress(updatedUser.getEmailAddress());
            return ResponseEntity.ok(existingUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a user by ID
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user by ID", description = "Delete a user by their userId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        Optional<User> userToDelete = userList.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst();

        if (userToDelete.isPresent()) {
            userList.remove(userToDelete.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
