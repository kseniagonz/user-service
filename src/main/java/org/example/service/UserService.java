package org.example.service;

import org.example.dao.UserDao;
import org.example.dao.UserDaoImpl;
import org.example.entity.User;
import org.example.exception.UserServiceException;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao = new UserDaoImpl();

    public User createUser(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);

        User user = new User(name, email, age);
        return userDao.save(user);
    }

    public Optional<User> getUser(Long id) {
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);

        User user = new User(name, email, age);
        user.setId(id);
        return userDao.update(user);
    }

    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new UserServiceException("Имя не может быть пустым");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new UserServiceException("Некорректный email: " + email);
        }
    }
}
