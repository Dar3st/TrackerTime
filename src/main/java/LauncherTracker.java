import java.util.Scanner;

public class LauncherTracker {
    private final TrackerTime trackerTime;
    private Action typeAction;
    private boolean isWork;
    private final Scanner scr = new Scanner(System.in);

    public LauncherTracker(){
        this.isWork = true;
        this.trackerTime = new TrackerTime();
        this.trackerTime.autoLoadIfEnabled();
    }

    private void menu(){
        System.out.println("""
                "=== Трекер времени ==="
                1. Показать учёт времени
                2. Добавить учёт времени
                3. Сохранить список учёта
                4. Загрузить список учёта
                5. Показать статистику времени
                6. Настройки программы
                7. Выйти
                """);
    }

    private void inputAction() {
        String response = scr.nextLine();
        switch (response){
            case "1" -> btnShowTracker();
            case "2" -> btnAddTracker();
            case "3" -> btnSaveList();
            case "4" -> btnLoadList();
            case "5" -> btnShowAnalise();
            case "6" -> btnSettings();
            case "7" -> btnExit();
            default -> {
                System.out.println("Неверный ввод. Попробуйте снова.");
                menu();
            }
        }
    }

    private void btnShowTracker(){
        String title = "=== Учёт времени ===";
        System.out.println(title);
        trackerTime.printTracker();
        System.out.println("=".repeat(title.length()));
        backMenu();
    }

    private void addTrackerById() {
        System.out.print("Укажите ID времени: ");
        int id = scr.nextInt();
        scr.nextLine();

        System.out.println("Доступные действия:");
        for (Action action : Action.getAllActions()) {
            System.out.println("- " + action.getDisplayName());
        }

        System.out.print("Укажите тип действий: ");
        String typeInput = scr.nextLine();

        Action action = getActionFromInput(typeInput);

        trackerTime.setMapAction(id, action);
        System.out.println("Вы установили - "
                + action.getDisplayName()
                + " на "
                + trackerTime.getMapTime(id)
                + "\n");
    }

    private Action getActionFromInput(String typeInput) {

        Action action = Action.findActionName(typeInput);

        if (action == null) {
            switch (typeInput.trim().toUpperCase()) {
                case "СОН" -> action = Action.SLEEP;
                case "РАБОТА" -> action = Action.WORK;
                case "УЧЁБА", "УЧЁБА JAVA" -> action = Action.LEARNING;
                case "ТРЕНИРОВКА" -> action = Action.TRAINING;
                default -> action = Action.NULL;
            }
        }

        return action;
    }

    private void addTrackerByRangeId(){
        System.out.print("Укажите начальный ID времени: ");
        int startTime = scr.nextInt();

        System.out.print("Укажите конечный ID времени: ");
        int endTime = scr.nextInt();

        scr.nextLine();

        System.out.print("Укажите тип действий (Сон, работа, учёба, тренировка): ");
        String typeInput = scr.nextLine();

        switch (typeInput.trim().toUpperCase()){
            case "СОН" -> typeAction = Action.SLEEP;
            case "РАБОТА" ->  typeAction = Action.WORK;
            case "УЧЁБА" -> typeAction = Action.LEARNING;
            case "ТРЕНИРОВКА" -> typeAction = Action.TRAINING;
            default -> typeAction = Action.NULL;
        }

        trackerTime.setMapAction(typeAction, startTime, endTime);

        System.out.println("Вы установили - "
                + typeInput
                + " в диапозоне: от "
                + trackerTime.getMapTime(startTime)
                + " до "
                + trackerTime.getMapTime(endTime)
                + "(включительно)\n");
        backMenu();
    }

    private void questionsAddTracker(){
        System.out.println("""
                1. Установить по одному ID
                2. Установить по диапозону ID""");
        String response = scr.nextLine();
        switch (response){
            case "1" -> addTrackerById();
            case "2" -> addTrackerByRangeId();
            default -> {
                System.out.println("Неверный выбор.");
                questionsAddTracker();
            }
        }
    }

    private void btnAddTracker(){
        String title = "=== Добавление времени к учёту ===";
        System.out.println(title);
        questionsAddTracker();
        System.out.println("=".repeat(title.length()));
        backMenu();
    }

    private void btnSaveList() {
        trackerTime.saveTracker();
        menu();
    }

    private void btnLoadList(){
        trackerTime.loadTracker();
        menu();
    }

    private void btnShowAnalise(){
        String title = "=== Статистика времени ===";
        System.out.println(title);
        trackerTime.showAnalise();
        System.out.println("=".repeat(title.length()));
        backMenu();
    }

    private void btnSettings(){
        String title = "=== Настройки программы ===";
        System.out.println(title);
        String listMenu = """
                1. Уставить разделитель
                2. Добавить свой тип действия
                3. Загружать автоматически сохранённый учёт времени
                4. Сохранить пользовательские типы "Дествия"
                """;
        System.out.println(listMenu);
        System.out.print("Выберите действие");
        trackerTime.setSettings();
        System.out.println("=".repeat(title.length()));
        backMenu();
    }

    private void btnExit(){
        System.out.println("Выход из программы...");
        this.isWork = false;
    }

    private void backMenu(){
        System.out.print("Выберите дейсвтие (0 - выход):");
        scr.nextLine();
        menu();
    }

    public void start(){
        menu();
        while(isWork){
            inputAction();
        }
        System.out.println("Программа завершена.");
        scr.close();
    }
}
