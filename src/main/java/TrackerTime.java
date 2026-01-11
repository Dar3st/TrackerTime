import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TrackerTime {
    private static final int TOTAL_SLOTS = 48;
    private static final int MINUTES_PER_SLOT = 30;
    private static final String TRACKER_FILE = "TrackerList.txt";
    private static final String SETTINGS_FILE = "settings.dat";

    private AppSettings settings;
    private final Map<Integer, LocalTime> mapTime;
    private final Map<Integer, Action> mapAction;

    public TrackerTime() {
        this.mapTime = new TreeMap<>();
        this.mapAction = new TreeMap<>();
        this.settings = loadSettings();
        initializeTimeSlots();
        autoLoadIfEnabled();
    }

    public AppSettings getSettings() {
        return settings;
    }

    private void initializeTimeSlots() {
        for (int count = 0; count < TOTAL_SLOTS; count++) {
            int hour = count / 2;
            int minute = (count % 2) * MINUTES_PER_SLOT;
            mapTime.put(count, LocalTime.of(hour, minute));
        }
    }

    public void printTracker() {
        String header = "ID\t|\tЧасы\t|\tДействия\t|";
        System.out.println(header);

        for (int i = 0; i < mapTime.size(); i++) {
            String time = mapTime.get(i).toString();
            String actionName = getActionAtSlot(i).getDisplayName();
            System.out.println(i + "\t|\t" + time + "\t|\t" + actionName + "\t|");
        }
    }

    private Action getActionAtSlot(int slot) {
        return mapAction.getOrDefault(slot, Action.NULL);
    }

    public void saveTracker() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRACKER_FILE))) {
            writer.println("=== Учёт времени ===");
            writer.printf("ID%cЧасы%cДействия%cActionID%n",
                    settings.getSplitter(),
                    settings.getSplitter(),
                    settings.getSplitter());

            for (int i = 0; i < mapTime.size(); i++) {
                String time = mapTime.get(i).toString();
                Action action = getActionAtSlot(i);

                writer.printf("%d%c%s%c%s%c%s%n",
                        i, settings.getSplitter(),
                        time, settings.getSplitter(),
                        action.getDisplayName(), settings.getSplitter(),
                        action.getId());
            }

            System.out.println("=== Данные успешно сохранены в " + TRACKER_FILE + " ===");
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public void loadTracker() {
        File file = new File(TRACKER_FILE);
        if (!file.exists()) {
            System.out.println("Файл " + TRACKER_FILE + " не найден.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            br.readLine();

            String line;
            int loadedCount = 0;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(String.valueOf(settings.getSplitter()));

                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String actionId = parts[3].trim();

                        if (id >= 0 && id < TOTAL_SLOTS) {
                            Action action = Action.getAction(actionId);
                            if (action != null) {
                                mapAction.put(id, action);
                                loadedCount++;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            System.out.println("=== Загружено " + loadedCount + " записей из " + TRACKER_FILE + " ===");
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    public void setMapAction(int id, Action typeAction) {
        validateSlotId(id);
        mapAction.put(id, typeAction);
    }

    public void setMapAction(Action typeAction, int startTime, int endTime) {
        validateSlotId(startTime);
        validateSlotId(endTime);

        if (startTime <= endTime) {
            for (int i = startTime; i <= endTime; i++) {
                mapAction.put(i, typeAction);
            }
        } else {
            for (int i = startTime; i < TOTAL_SLOTS; i++) {
                mapAction.put(i, typeAction);
            }
            for (int i = 0; i <= endTime; i++) {
                mapAction.put(i, typeAction);
            }
        }
    }

    private void validateSlotId(int id) {
        if (id < 0 || id >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("ID должен быть в диапазоне 0-" + (TOTAL_SLOTS - 1));
        }
    }

    public String getMapTime(int id) {
        validateSlotId(id);
        return mapTime.get(id).toString();
    }

    public void showAnalise() {
        System.out.println("=== Статистика по действиям ===");
        int count = 1;

        for (Action action : Action.getAllActions()) {
            List<TimeInterval> intervals = getIntervalsForAction(action);

            if (!intervals.isEmpty()) {
                long totalMinutes = intervals.stream()
                        .mapToLong(TimeInterval::getDurationInMinutes)
                        .sum();

                System.out.printf("%d. %-15s | Всего: %d час. %d мин.%n",
                        count++,
                        action.getDisplayName(),
                        totalMinutes / 60,
                        totalMinutes % 60);

                intervals.forEach(interval ->
                        System.out.printf("   %s - %s%n",
                                interval.getStartTime(),
                                interval.getEndTime()));
            }
        }
    }

    private List<TimeInterval> getIntervalsForAction(Action action) {
        List<TimeInterval> intervals = new ArrayList<>();
        boolean inInterval = false;
        int intervalStart = -1;

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            Action current = getActionAtSlot(i);

            if (current.equals(action)) {
                if (!inInterval) {
                    intervalStart = i;
                    inInterval = true;
                }
            } else {
                if (inInterval) {
                    intervals.add(new TimeInterval(
                            mapTime.get(intervalStart),
                            mapTime.get(i - 1)
                    ));
                    inInterval = false;
                }
            }
        }

        if (inInterval) {
            intervals.add(new TimeInterval(
                    mapTime.get(intervalStart),
                    mapTime.get(TOTAL_SLOTS - 1)
            ));
        }

        return intervals;
    }

    private AppSettings loadSettings() {
        File settingsFile = new File(SETTINGS_FILE);

        if (settingsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(settingsFile))) {
                AppSettings loadedSettings = (AppSettings) ois.readObject();
                System.out.println("Настройки успешно загружены");
                return loadedSettings;
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Ошибка загрузки настроек. Загружены настройки по умолчанию");
            }
        } else {
            System.out.println("Файл настроек не найден. Создан новый файл настроек по умолчанию");
        }

        return new AppSettings();
    }

    public void showSettingsMenu() {
        Scanner scr = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== НАСТРОЙКИ ПРОГРАММЫ ===");
            System.out.println("Текущие настройки:");
            System.out.println("1. Разделитель CSV: '" + settings.getSplitter() + "'");
            System.out.println("2. Автозагрузка данных: " + (settings.isAutoLoadEnabled() ? "ВКЛ" : "ВЫКЛ"));
            System.out.println("3. Формат даты: " + settings.getDateFormat());
            System.out.println("\nДействия:");
            System.out.println("4. Изменить разделитель");
            System.out.println("5. Включить/выключить автозагрузку");
            System.out.println("6. Изменить формат даты");
            System.out.println("7. Сохранить настройки");
            System.out.println("8. Сбросить к настройкам по умолчанию");
            System.out.println("0. Выйти в главное меню");
            System.out.print("\nВыберите действие: ");

            String choice = scr.nextLine().trim();

            switch (choice) {
                case "4" -> changeSplitter(scr);
                case "5" -> toggleAutoLoad();
                case "6" -> changeDateFormat(scr);
                case "7" -> saveSettings();
                case "8" -> resetToDefaultSettings();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void changeSplitter(Scanner scr) {
        System.out.print("Укажите новый разделитель (один символ): ");
        String input = scr.nextLine().trim();

        if (input.length() == 1) {
            settings.setSplitter(input.charAt(0));
            System.out.println("Разделитель изменен на: '" + input.charAt(0) + "'");
        } else {
            System.out.println("Ошибка: нужно указать один символ");
        }
    }

    public void addCustomAction(Scanner scr) {
        System.out.print("Введите название нового действия: ");
        String actionName = scr.nextLine();

        try {
            Action newAction = Action.createCustomAction(actionName);
            Action.saveCustomActions();
            System.out.println("Действие '" + actionName + "' было успешно добавлено");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void toggleAutoLoad() {
        boolean currentFlag = settings.isAutoLoadEnabled();
        settings.setAutoLoadEnabled(!currentFlag);
        System.out.println("Автозагрузка данных теперь: " +
                (settings.isAutoLoadEnabled() ? "Включена" : "Выключена"));
    }

    private void changeDateFormat(Scanner scr) {
        System.out.println("Доступные форматы даты:");
        System.out.println("1. dd.MM.yyyy (день.месяц.год)");
        System.out.println("2. yyyy-MM-dd (год-месяц-день)");
        System.out.println("3. MM/dd/yyyy (месяц/день/год)");
        System.out.print("Выберите формат: ");

        String choice = scr.nextLine().trim();
        switch (choice) {
            case "1" -> {
                settings.setDateFormat("dd.MM.yyyy");
                System.out.println("Формат даты изменен на: dd.MM.yyyy");
            }
            case "2" -> {
                settings.setDateFormat("yyyy-MM-dd");
                System.out.println("Формат даты изменен на: yyyy-MM-dd");
            }
            case "3" -> {
                settings.setDateFormat("MM/dd/yyyy");
                System.out.println("Формат даты изменен на: MM/dd/yyyy");
            }
            default -> System.out.println("Неверный выбор. Формат не изменен.");
        }
    }

    public void autoLoadIfEnabled() {
        if (settings.isAutoLoadEnabled()) {
            loadTracker();
        }
    }

    private void saveSettings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SETTINGS_FILE))) {
            oos.writeObject(settings);
            System.out.println("Настройки успешно сохранены");
        } catch (IOException e) {
            System.out.println("Ошибка сохранения настроек: " + e.getMessage());
        }
    }

    private void resetToDefaultSettings() {
        this.settings = new AppSettings();
        saveSettings();
        System.out.println("Настройки сброшены к значениям по умолчанию");
    }

    private static class TimeInterval {
        private final LocalTime start;
        private final LocalTime end;

        TimeInterval(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        LocalTime getStartTime() {
            return start;
        }

        LocalTime getEndTime() {
            return end;
        }

        long getDurationInMinutes() {
            return (end.getHour() * 60L + end.getMinute()) -
                    (start.getHour() * 60L + start.getMinute()) + MINUTES_PER_SLOT;
        }
    }
}