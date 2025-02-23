package v1.common.service;

import v1.common.pojo.User;

public interface UserService {

    // Look up the user by id
    User getUserByUserId(Integer id);

    // Insert the given user
    Integer insertUserId(User user);
}
