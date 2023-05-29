package com.richrb97.users1.document;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "users")
@Data
public class User {
    private String id;
    private String name;
    private int age;
    private String email;
    private String address;
    private String rol;
}
