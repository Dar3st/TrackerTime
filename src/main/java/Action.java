import java.io.*;
import java.util.*;

public class Action implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String displayName;
    private final boolean isCustom;

    public static final Action NULL = new Action("NULL", "Свободно", false);
    public static final Action SLEEP = new Action("SLEEP", "Сон", false);
    public static final Action WORK = new Action("WORK", "Работа", false);
    public static final Action TRAINING = new Action("TRAINING", "Тренировка", false);
    public static final Action LEARNING = new Action("LEARNING", "Учёба", false);

    private static final Map<String, Action> defaultActions = new HashMap<>();
    private static final Map<String, Action> customActions = new HashMap<>();

    static {
        defaultActions.put(NULL.id, NULL);
        defaultActions.put(SLEEP.id, SLEEP);
        defaultActions.put(WORK.id, WORK);
        defaultActions.put(TRAINING.id, TRAINING);
        defaultActions.put(LEARNING.id, LEARNING);

        loadCustomActions();
    }

    private Action(String id, String displayName, boolean isCustom) {
        this.id = id;
        this.displayName = displayName;
        this.isCustom = isCustom;
    }

    public static Action createCustomAction(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Название действия не может быть пустым");
        }

        String id = "USER_" + displayName.trim().toUpperCase()
                .replace(" ", "_")
                .replaceAll("[^A-ZА-Я0-9_]", "");

        if (defaultActions.containsKey(id) || customActions.containsKey(id)) {
            throw new IllegalArgumentException("Действие с таким именем уже существует");
        }

        if (findActionName(displayName.trim()) != null) {
            throw new IllegalArgumentException("Действие с таким отображаемым именем уже существует");
        }

        Action action = new Action(id, displayName.trim(), true);
        customActions.put(id, action);
        return action;
    }

    public static Action getAction(String actionId) {
        Action action = defaultActions.get(actionId);
        if (action == null) {
            action = customActions.get(actionId);
        }
        return action;
    }

    public static Action findActionName(String displayName) {
        String searchName = displayName.trim();

        for (Action action : defaultActions.values()) {
            if (action.getDisplayName().equalsIgnoreCase(searchName)) {
                return action;
            }
        }

        for (Action action : customActions.values()) {
            if (action.getDisplayName().equalsIgnoreCase(searchName)) {
                return action;
            }
        }

        return null;
    }

    public static List<Action> getAllActions() {
        List<Action> actionList = new ArrayList<>(defaultActions.values());
        actionList.addAll(customActions.values());
        return Collections.unmodifiableList(actionList);
    }

    public static void loadCustomActions() {
        File file = new File("custom_actions.dat");
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Action> loadedActions = (List<Action>) ois.readObject();
            customActions.clear();

            for (Action action : loadedActions) {
                if (!defaultActions.containsKey(action.getId())) {
                    customActions.put(action.getId(), action);
                }
            }

            System.out.println("Загружено: " + loadedActions.size() + " пользовательских действий");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Не удалось загрузить пользовательские действия: " + e.getMessage());
        }
    }

    public static void saveCustomActions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("custom_actions.dat"))) {

            List<Action> customActionsList = new ArrayList<>(customActions.values());
            oos.writeObject(customActionsList);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить пользовательские действия: " + e.getMessage());
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return id;
    }

    public boolean isCustom() {
        return isCustom;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Action action = (Action) obj;
        return id.equals(action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return displayName;
    }
}