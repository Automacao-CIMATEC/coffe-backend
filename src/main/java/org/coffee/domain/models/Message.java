package org.coffee.domain.models;

public class Message {
    private Long id;
    private String content;

    // Constructors
    public Message() {}

    public Message(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}