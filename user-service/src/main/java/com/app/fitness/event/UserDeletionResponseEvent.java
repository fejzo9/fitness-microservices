package com.app.fitness.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDeletionResponseEvent implements Serializable {
    public enum Status { SUCCESS, FAILURE }
    private Long userId;
    private String service;
    private Status status;
}
