package org.example.dao;

import org.example.entity.User;
import org.example.exception.UserServiceException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static SessionFactory sessionFactory;

    UserDaoImpl userDao;

    @BeforeAll
    static void setUpSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.show_sql", "false");
        configuration.addAnnotatedClass(User.class);

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        sessionFactory = configuration.buildSessionFactory(registry);
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl(sessionFactory);
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("delete from User").executeUpdate();
            tx.commit();
        }
    }

    @Test
    void save_persistsUserWithGeneratedIdAndTimestamp() {
        User user = new User("Иван", "ivan@mail.com", 25);

        User saved = userDao.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findById_existingUser_returnsUser() {
        User saved = userDao.save(new User("Мария", "maria@mail.com", 30));

        Optional<User> found = userDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Мария", found.get().getName());
    }

    @Test
    void findById_missingUser_returnsEmpty() {
        Optional<User> found = userDao.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        userDao.save(new User("Иван", "ivan@mail.com", 25));
        userDao.save(new User("Мария", "maria@mail.com", 30));

        List<User> all = userDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void update_existingUser_changesFields() {
        User saved = userDao.save(new User("Иван", "ivan@mail.com", 25));

        User toUpdate = new User("Иван Петров", "ivan.petrov@mail.com", 26);
        toUpdate.setId(saved.getId());
        User updated = userDao.update(toUpdate);

        assertEquals("Иван Петров", updated.getName());
        assertEquals("ivan.petrov@mail.com", updated.getEmail());
        assertEquals(26, updated.getAge());
    }

    @Test
    void update_missingUser_throwsException() {
        User toUpdate = new User("Призрак", "ghost@mail.com", 0);
        toUpdate.setId(999L);

        assertThrows(UserServiceException.class, () -> userDao.update(toUpdate));
    }

    @Test
    void deleteById_existingUser_removesUser() {
        User saved = userDao.save(new User("Иван", "ivan@mail.com", 25));

        userDao.deleteById(saved.getId());

        assertTrue(userDao.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteById_missingUser_throwsException() {
        assertThrows(UserServiceException.class, () -> userDao.deleteById(999L));
    }
}
