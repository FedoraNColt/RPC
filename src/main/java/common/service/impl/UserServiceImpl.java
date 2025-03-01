package common.service.impl;

import common.pojo.User;
import common.service.UserService;

import java.util.Random;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final Random random = new Random();

    @Override
    public User getUserByUserId(Integer id) {
        System.out.println("Client looks up the user with id: " + id);
        User user = User.builder()
                .userName(UUID.randomUUID().toString())
                .id(id)
                .sex(random.nextBoolean()) // Randomly generates the gender of the user
                .build();
        return user;
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println("User: " + user.getUserName() + " is inserted successfully");
        return user.getId();
    }
}
