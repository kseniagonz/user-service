package org.example;

import org.example.entity.User;
import org.example.exception.UserServiceException;
import org.example.service.UserService;
import org.example.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService();

    public static void main(String[] args) {
        logger.info("Запуск приложения user-service");
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> findUser();
                    case "3" -> listUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> running = false;
                    default -> System.out.println("Неверный пункт меню, попробуйте ещё раз.");
                }
            } catch (UserServiceException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Непредвиденная ошибка", e);
                System.out.println("Произошла непредвиденная ошибка.");
            }
        }

        HibernateUtil.shutdown();
        System.out.println("Работа приложения завершена.");
    }

    private static void printMenu() {
        System.out.println("""

                ===== USER SERVICE =====
                1. Создать пользователя
                2. Найти пользователя по id
                3. Показать всех пользователей
                4. Обновить пользователя
                5. Удалить пользователя
                0. Выход
                Выберите пункт:""");
    }

    private static void createUser() {
        System.out.print("Имя: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Возраст: ");
        Integer age = readIntOrNull();

        User created = userService.createUser(name, email, age);
        System.out.println("Создан пользователь: " + created);
    }

    private static void findUser() {
        System.out.print("Введите id: ");
        Long id = readLong();
        if (id == null) return;

        Optional<User> user = userService.getUser(id);
        if (user.isPresent()) {
            System.out.println(user.get());
        } else {
            System.out.println("Пользователь с id=" + id + " не найден.");
        }
    }

    private static void listUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("В базе пока нет пользователей.");
            return;
        }
        users.forEach(System.out::println);
    }

    private static void updateUser() {
        System.out.print("id пользователя, которого нужно обновить: ");
        Long id = readLong();
        if (id == null) return;

        System.out.print("Новое имя: ");
        String name = scanner.nextLine().trim();

        System.out.print("Новый email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Новый возраст: ");
        Integer age = readIntOrNull();

        User updated = userService.updateUser(id, name, email, age);
        System.out.println("Обновлено: " + updated);
    }

    private static void deleteUser() {
        System.out.print("id пользователя, которого нужно удалить: ");
        Long id = readLong();
        if (id == null) return;

        userService.deleteUser(id);
        System.out.println("Пользователь с id=" + id + " удалён.");
    }

    private static Long readLong() {
        String input = scanner.nextLine().trim();
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("Некорректный id, ожидалось целое число.");
            return null;
        }
    }

    private static Integer readIntOrNull() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Некорректное число, возраст сохранён как пустой.");
            return null;
        }
    }
}
