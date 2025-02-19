package org.example.common.service.impl;

import org.example.common.pojo.User;
import org.example.common.service.UserService;

import java.util.Random;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    Random random = new Random();

    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("Client looks up user by id: " + id);

        User user = User.builder().userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean())
                .build();

        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println("Successfully inserts user by id: " + user.getId());
        return user.getId();
    }
}
