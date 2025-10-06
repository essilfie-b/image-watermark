package org.images.entities;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String id;

    private String cognitoUserId;

    private String name;

    private String email;
}
