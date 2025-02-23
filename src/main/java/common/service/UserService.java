package common.service;

import common.pojo.User;

public interface UserService {

    // Look up the user by id
    User getUserByUserId(Integer id);

    // Insert the given user
    Integer insertUserId(User user);
}
