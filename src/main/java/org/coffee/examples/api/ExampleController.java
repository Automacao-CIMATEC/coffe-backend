package org.coffee.examples.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

//@RestController // REMOVA ESSE COMENTÁRIO PARA PERMITIR O USO DESSE CONTROLLER
@RequestMapping("/api/messages")
@Tag(name = "Message API", description = "Simple message management API")
public class ExampleController {

    private final List<ExampleMessage> exampleMessages = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    @GetMapping
    @Operation(summary = "Get all messages", description = "Returns a list of all messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public List<ExampleMessage> getAllMessages() {
        return exampleMessages;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Returns a message by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved message")
    @ApiResponse(responseCode = "404", description = "Message not found")
    public ExampleMessage getMessageById(@PathVariable Long id) {
        return exampleMessages.stream()
                .filter(exampleMessage -> exampleMessage.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @PostMapping
    @Operation(summary = "Create a new message", description = "Creates and returns a new message")
    @ApiResponse(responseCode = "200", description = "Successfully created message")
    public ExampleMessage createMessage(@RequestBody ExampleMessage exampleMessage) {
        exampleMessage.setId(counter.incrementAndGet());
        exampleMessages.add(exampleMessage);
        return exampleMessage;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a message", description = "Deletes a message by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully deleted message")
    @ApiResponse(responseCode = "404", description = "Message not found")
    public void deleteMessage(@PathVariable Long id) {
        exampleMessages.removeIf(exampleMessage -> exampleMessage.getId().equals(id));
    }
}
