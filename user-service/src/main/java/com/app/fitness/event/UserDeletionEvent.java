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
public class UserDeletionEvent implements Serializable {
    public enum Type { START, ROLLBACK, FINALIZE }
    private Long userId;
    private Type type;
}
