package org.example.service;

import org.example.dao.UserDao;
import org.example.entity.User;
import org.example.exception.UserServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void createUser_validData_savesUserThroughDao() {
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser("Иван", "ivan@mail.com", 25);

        assertEquals("Иван", result.getName());
        assertEquals("ivan@mail.com", result.getEmail());
        assertEquals(25, result.getAge());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).save(captor.capture());
        assertEquals("Иван", captor.getValue().getName());
    }

    @Test
    void createUser_blankName_throwsExceptionAndDoesNotCallDao() {
        assertThrows(UserServiceException.class,
                () -> userService.createUser("  ", "ivan@mail.com", 25));

        verify(userDao, never()).save(any());
    }

    @Test
    void createUser_invalidEmail_throwsExceptionAndDoesNotCallDao() {
        assertThrows(UserServiceException.class,
                () -> userService.createUser("Иван", "not-an-email", 25));

        verify(userDao, never()).save(any());
    }

    @Test
    void getUser_existingId_returnsUser() {
        User user = new User("Мария", "maria@mail.com", 30);
        user.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUser(1L);

        assertTrue(result.isPresent());
        assertEquals("Мария", result.get().getName());
    }

    @Test
    void getUser_missingId_returnsEmptyOptional() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUser(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUsers_delegatesToDao() {
        User first = new User("Иван", "ivan@mail.com", 25);
        User second = new User("Мария", "maria@mail.com", 30);
        when(userDao.findAll()).thenReturn(List.of(first, second));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void updateUser_invalidEmail_throwsExceptionAndDoesNotCallDao() {
        assertThrows(UserServiceException.class,
                () -> userService.updateUser(1L, "Иван", "bad-email", 25));

        verify(userDao, never()).update(any());
    }

    @Test
    void updateUser_validData_callsDaoWithCorrectId() {
        User updated = new User("Иван Обновлённый", "ivan@mail.com", 26);
        updated.setId(1L);
        when(userDao.update(any(User.class))).thenReturn(updated);

        User result = userService.updateUser(1L, "Иван Обновлённый", "ivan@mail.com", 26);

        assertEquals(1L, result.getId());
        assertEquals("Иван Обновлённый", result.getName());
    }

    @Test
    void deleteUser_delegatesToDao() {
        userService.deleteUser(1L);

        verify(userDao, times(1)).deleteById(1L);
    }
}
