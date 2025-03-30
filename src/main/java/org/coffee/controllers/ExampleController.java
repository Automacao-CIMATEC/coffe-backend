package org.coffee.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.coffee.domain.models.Message;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message API", description = "Simple message management API")
public class ExampleController {

    private final List<Message> messages = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    @GetMapping
    @Operation(summary = "Get all messages", description = "Returns a list of all messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public List<Message> getAllMessages() {
        return messages;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Returns a message by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved message")
    @ApiResponse(responseCode = "404", description = "Message not found")
    public Message getMessageById(@PathVariable Long id) {
        return messages.stream()
                .filter(message -> message.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @PostMapping
    @Operation(summary = "Create a new message", description = "Creates and returns a new message")
    @ApiResponse(responseCode = "200", description = "Successfully created message")
    public Message createMessage(@RequestBody Message message) {
        message.setId(counter.incrementAndGet());
        messages.add(message);
        return message;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a message", description = "Deletes a message by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully deleted message")
    @ApiResponse(responseCode = "404", description = "Message not found")
    public void deleteMessage(@PathVariable Long id) {
        messages.removeIf(message -> message.getId().equals(id));
    }
}