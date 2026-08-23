package org.example.dao;

import org.example.entity.User;
import org.example.exception.UserServiceException;
import org.example.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    private final SessionFactory sessionFactory;

    public UserDaoImpl() {
        this(HibernateUtil.getSessionFactory());
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            logger.info("Пользователь создан: {}", user);
            return user;
        } catch (HibernateException e) {
            rollbackSafely(transaction);
            logger.error("Ошибка при создании пользователя {}", user, e);
            throw new UserServiceException("Не удалось создать пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (HibernateException e) {
            logger.error("Ошибка при поиске пользователя с id={}", id, e);
            throw new UserServiceException("Не удалось найти пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User ORDER BY id", User.class).list();
        } catch (HibernateException e) {
            logger.error("Ошибка при получении списка пользователей", e);
            throw new UserServiceException("Не удалось получить список пользователей: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User existing = session.get(User.class, user.getId());
            if (existing == null) {
                throw new UserServiceException("Пользователь с id=" + user.getId() + " не найден");
            }

            existing.setName(user.getName());
            existing.setEmail(user.getEmail());
            existing.setAge(user.getAge());

            transaction.commit();
            logger.info("Пользователь обновлён: {}", existing);
            return existing;
        } catch (HibernateException e) {
            rollbackSafely(transaction);
            logger.error("Ошибка при обновлении пользователя {}", user, e);
            throw new UserServiceException("Не удалось обновить пользователя: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user == null) {
                throw new UserServiceException("Пользователь с id=" + id + " не найден");
            }

            session.remove(user);

            transaction.commit();
            logger.info("Пользователь с id={} удалён", id);
        } catch (HibernateException e) {
            rollbackSafely(transaction);
            logger.error("Ошибка при удалении пользователя с id={}", id, e);
            throw new UserServiceException("Не удалось удалить пользователя: " + e.getMessage(), e);
        }
    }

    private void rollbackSafely(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
            logger.warn("Транзакция откачена (rollback)");
        }
    }
}
