package org.example.service;

import org.example.pojo.User;

public interface UserService {

    // Look up the user by id
    org.example.pojo.User getUserByUserId(Integer id);

    // Insert the given user
    Integer insertUserId(User user);
}
