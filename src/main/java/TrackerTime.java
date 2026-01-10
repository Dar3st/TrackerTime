import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class TrackerTime {
    private char splitter;
    private boolean autoLoadEnabled = false;

    private final Map<Integer, LocalTime> mapTime;
    private final Map<Integer, Action> mapAction;

    public TrackerTime(){
        this.mapTime = new TreeMap<>();
        this.mapAction = new TreeMap<>();
        this.splitter = ',';
        setMapTime();
    }

    private void setMapTime() {
        for (int count = 0; count < 48; count++) {
            int hour = count / 2;
            int minute = (count % 2) * 30;
            mapTime.put(count, LocalTime.of(hour, minute));
        }
    }

    public void printTracker(){
        System.out.println("ID" + "\t|\t" + "Часы" + "\t|\t" + "Действия\t|");
        for(int i = 0; i < mapTime.size(); i++){
            String result = String.valueOf(mapTime.get(i));
            String type = mapAction.get(i) != null ?
                    mapAction.get(i).getDisplayName() : Action.NULL.getDisplayName();
            System.out.println(i + "\t|\t" + result + "\t|\t" + type + "\t|");
        }
    }

    public void saveTracker() {
        try(PrintWriter writer = new PrintWriter(new FileWriter("TrackerList.txt"))) {
            writer.println("=== Учёт времени ===");
            writer.printf("ID%cЧасы%cДействия%cActionID%n", splitter, splitter, splitter);

            for(int i = 0; i < mapTime.size(); i++) {
                String time = String.valueOf(mapTime.get(i));
                Action action = mapAction.get(i) != null ?
                        mapAction.get(i) : Action.NULL;

                writer.printf("%d%c%s%c%s%c%s%n",
                        i, splitter,
                        time, splitter,
                        action.getDisplayName(), splitter,
                        action.getId());
            }
            System.out.println("=== Данные успешно сохранены в TrackerList.txt ===");
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public void loadTracker() {
        try (BufferedReader br = new BufferedReader(new FileReader("TrackerList.txt"))) {
            String line;
            br.readLine();
            br.readLine();

            while((line = br.readLine()) != null) {
                String[] parts = line.split(String.valueOf(splitter));

                if(parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String actionId = parts[3].trim();

                        Action action = Action.getAction(actionId);
                        if (action == null) {
                            action = Action.NULL;
                        }

                        mapAction.put(id, action);
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
            System.out.println("=== Данные успешно загружены из TrackerList.txt ===");
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден. Будет создан новый при сохранении.");
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке файла: " + e.getMessage());
        }
    }

    public void setMapAction(int id, Action typeAction){
        if(id < 0 || id > mapTime.size()-1)
            throw new IllegalArgumentException("Время должно быть в диапозоне 0-" + (mapTime.size()-1));
        mapAction.put(id, typeAction);
    }

    public void setMapAction(Action typeAction, int startTime, int endTime){
        if(startTime < 0 || endTime < 0 || startTime >= mapTime.size() || endTime >= mapTime.size()){
            throw new IllegalArgumentException("Время должно быть в диапозоне 0-" + (mapTime.size()-1));
        }

        if(startTime <= endTime){
            for (int i = startTime; i <= endTime; i++){
                mapAction.put(i, typeAction);
            }
        }else{
            int maxId = mapTime.size();
            for(int i = startTime; i < maxId; i++){
                mapAction.put(i, typeAction);
            }
            for(int i = 0; i <= endTime; i++){
                mapAction.put(i, typeAction);
            }
        }
    }

    public String getMapTime(int id){
        String result;
        result = String.valueOf(mapTime.get(id));
        return result;
    }

    public void showAnalise() {
        System.out.println("=== Статистика по действиям ===");
        int count = 1;

        for (Action action : Action.getAllActions()) {
            long totalMinutes = calculateTotalTimesForAction(action);

            if (totalMinutes > 0) {
                System.out.printf("%d. %-15s | Всего: %d час. %d мин.%n",
                        count++,
                        action.getDisplayName(),
                        totalMinutes / 60,
                        totalMinutes % 60);

                showIntervalsForAction(action);
            }
        }
    }

    private void showIntervalsForAction(Action action) {
        boolean inInterval = false;
        int intervalStart = -1;

        for (int i = 0; i < mapTime.size(); i++) {
            Action current = mapAction.get(i) != null ? mapAction.get(i) : Action.NULL;

            if (current.equals(action)) {
                if (!inInterval) {
                    intervalStart = i;
                    inInterval = true;
                }
            } else {
                if (inInterval) {
                    System.out.printf("   %s - %s%n",
                            mapTime.get(intervalStart),
                            mapTime.get(i - 1));
                    inInterval = false;
                }
            }
        }

        if (inInterval) {
            System.out.printf("   %s - %s%n",
                    mapTime.get(intervalStart),
                    mapTime.get(mapTime.size() - 1));
        }
    }

    private long calculateTotalTimesForAction(Action action) {
        long totalSlots = 0;

        for(int i = 0; i < mapTime.size(); i++) {
            Action current = mapAction.get(i) != null ? mapAction.get(i) : Action.NULL;
            if(current.equals(action)) {
                totalSlots++;
            }
        }

        return totalSlots * 30;
    }

    public void setSettings(){
        Scanner scr = new Scanner(System.in);
        String input = scr.nextLine();
        switch (input){
            case "1" -> {
                String inputChar = scr.nextLine().strip();
                char splitterInput = inputChar.charAt(0);
                setSplitter(splitterInput);
            }
            case "2" -> addCustomAction(scr);
            case "3" -> {
                System.out.print("Хотите установить автозагрузку ваших данных (да/нет): ");
                String response = scr.nextLine();
                boolean flag = false;
                switch (response){
                    case "да" -> {
                        flag = true;
                        System.out.println("Вы успешно установили значение автозагрузки - да");
                    }
                    case "нет" -> {
                        flag = false;
                        System.out.println("Вы успешно установили значение автозагрузки - да");
                    }
                    default -> {
                        System.out.println("Выбрать да или нет.");
                    }
                }
                setFlagAutoload(flag);
            }
            case "4" -> saveCustomActions();
        }
    }

    private void setSplitter(char newSplitter) {
        switch (newSplitter) {
            case ',', ';', '\'', '|', ':' -> {
                this.splitter = newSplitter;
                System.out.println("Вы успешно установили разделитель - '" + newSplitter + "'");
            }
            default -> {
                System.out.println("Неверный знак разделителя. Установлен по умолчанию ','");
                this.splitter = ',';
            }
        }
    }

    private void addCustomAction(Scanner scr){
        System.out.print("Введите название нового действия: ");
        String actionName = scr.nextLine();
        try{
            Action newAction = Action.createCustomActions(actionName);
            System.out.println("Действие " + actionName + " было успешно добавлено");
        } catch (IllegalArgumentException e){
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void setFlagAutoload(boolean flag) {
        this.autoLoadEnabled = flag;
        System.out.println("Автозагрузка " + (flag ? "включена" : "выключена"));
    }

    public void autoLoadIfEnabled() {
        if (autoLoadEnabled) {
            File file = new File("TrackerList.txt");
            if (file.exists()) {
                loadTracker();
            }
        }
    }

    public void saveCustomActions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("custom_actions.dat"))) {

            List<Action> customActions = Action.getAllActions().stream()
                    .filter(Action::isCustom)
                    .collect(Collectors.toList());
            oos.writeObject(customActions);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить пользовательские действия");
        }
    }
}
