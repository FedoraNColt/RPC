package org.example.pojo;

import java.io.Serializable;

public class User implements Serializable {
    // Shared by both server and client
    private Integer id;
    private String userName;
    private Boolean gender;

    public User() {}

    public User(Integer id, String userName, Boolean gender) {
        this.id = id;
        this.userName = userName;
        this.gender = gender;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", gender=" + gender +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return java.util.Objects.equals(id, user.id) &&
                java.util.Objects.equals(userName, user.userName) &&
                java.util.Objects.equals(gender, user.gender);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, userName, gender);
    }

    public static class UserBuilder {
        private Integer id;
        private String userName;
        private Boolean gender;

        UserBuilder() {}

        public UserBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public UserBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public UserBuilder gender(Boolean gender) {
            this.gender = gender;
            return this;
        }

        public User build() {
            return new User(id, userName, gender);
        }
    }
}
