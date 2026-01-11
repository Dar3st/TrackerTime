import java.util.Scanner;

public class LauncherTracker {
    private final TrackerTime trackerTime;
    private boolean isWorking;
    private final Scanner scanner;

    public LauncherTracker() {
        this.isWorking = true;
        this.trackerTime = new TrackerTime();
        this.scanner = new Scanner(System.in);
    }

    private void displayMenu() {
        System.out.println("""
                === Трекер времени ===
                1. Показать учёт времени
                2. Добавить учёт времени
                3. Добавить своё действие
                4. Сохранить список учёта
                5. Загрузить список учёта
                6. Показать статистику времени
                7. Настройки программы
                8. Выйти
                """);
    }

    private void handleUserInput() {
        System.out.print("Выберите действие: ");
        String response = scanner.nextLine().trim();

        switch (response) {
            case "1" -> showTracker();
            case "2" -> addTracker();
            case "3" -> addCustomAction();
            case "4" -> saveList();
            case "5" -> loadList();
            case "6" -> showAnalysis();
            case "7" -> showSettings();
            case "8" -> exit();
            default -> {
                System.out.println("Неверный ввод. Попробуйте снова.");
                displayMenu();
            }
        }
    }

    private void showTracker() {
        String title = "=== Учёт времени ===";
        System.out.println(title);
        trackerTime.printTracker();
        System.out.println("=".repeat(title.length()));
        returnToMenu();
    }

    private void addTracker() {
        String title = "=== Добавление времени к учёту ===";
        System.out.println(title);
        askAddMethod();
        System.out.println("=".repeat(title.length()));
        returnToMenu();
    }

    private void askAddMethod() {
        System.out.println("""
                1. Установить по одному ID
                2. Установить по диапазону ID""");
        System.out.print("Ваш выбор: ");
        String response = scanner.nextLine().trim();

        switch (response) {
            case "1" -> addTrackerById();
            case "2" -> addTrackerByRangeId();
            default -> {
                System.out.println("Неверный выбор.");
                askAddMethod();
            }
        }
    }

    private void addTrackerById() {
        System.out.print("Укажите ID времени (0-47): ");

        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Доступные действия:");
            for (Action action : Action.getAllActions()) {
                System.out.println("- " + action.getDisplayName());
            }

            System.out.print("Укажите действие: ");
            String actionInput = scanner.nextLine().trim();

            Action action = Action.findActionName(actionInput);
            if (action == null) {
                action = Action.NULL;
                System.out.println("Действие не найдено. Установлено 'Свободно'");
            }

            trackerTime.setMapAction(id, action);
            System.out.printf("Вы установили '%s' на %s%n%n",
                    action.getDisplayName(),
                    trackerTime.getMapTime(id));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число от 0 до 47");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void addTrackerByRangeId() {
        try {
            System.out.print("Укажите начальный ID времени (0-47): ");
            int startTime = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Укажите конечный ID времени (0-47): ");
            int endTime = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Доступные действия:");
            for (Action action : Action.getAllActions()) {
                System.out.println("- " + action.getDisplayName());
            }

            System.out.print("Укажите действие: ");
            String actionInput = scanner.nextLine().trim();

            Action action = Action.findActionName(actionInput);
            if (action == null) {
                action = Action.NULL;
                System.out.println("Действие не найдено. Установлено 'Свободно'");
            }

            trackerTime.setMapAction(action, startTime, endTime);
            System.out.printf("Вы установили '%s' в диапазоне: от %s до %s (включительно)%n%n",
                    action.getDisplayName(),
                    trackerTime.getMapTime(startTime),
                    trackerTime.getMapTime(endTime));
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число от 0 до 47");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void addCustomAction() {
        trackerTime.addCustomAction(scanner);
        returnToMenu();
    }

    private void saveList() {
        trackerTime.saveTracker();
        displayMenu();
    }

    private void loadList() {
        trackerTime.loadTracker();
        displayMenu();
    }

    private void showAnalysis() {
        String title = "=== Статистика времени ===";
        System.out.println(title);
        trackerTime.showAnalise();
        System.out.println("=".repeat(title.length()));
        returnToMenu();
    }

    private void showSettings() {
        trackerTime.showSettingsMenu();
        displayMenu();
    }

    private void exit() {
        System.out.println("Выход из программы...");
        this.isWorking = false;
    }

    private void returnToMenu() {
        System.out.print("Нажмите Enter для возврата в меню...");
        scanner.nextLine();
        displayMenu();
    }

    public void start() {
        displayMenu();
        while (isWorking) {
            handleUserInput();
        }
        System.out.println("Программа завершена.");
        scanner.close();
    }
}