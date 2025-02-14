package org.example.part1.common.service;

import org.example.part1.common.pojo.User;

public interface UserService {

    User getUserByUserId(Integer id);

    Integer insertUserId(User user);
}
