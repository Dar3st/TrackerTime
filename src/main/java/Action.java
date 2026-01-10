import java.io.Serializable;
import java.util.*;

public class Action implements Serializable {
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
    }

    private Action(String id, String displayName, boolean isCustom){
        this.id = id;
        this.displayName = displayName;
        this.isCustom = isCustom;
    }

    public static Action createCustomActions(String displayName){
        String id = "USER_" + displayName.toUpperCase().replace(" ", "_");

        if(defaultActions.containsKey(id) || customActions.containsKey(id)){
            throw new IllegalArgumentException("Действие с таким именем уже существует");
        }

        Action action = new Action(id, displayName, true);
        customActions.put(id, action);
        return action;
    }

    public static Action getAction(String actionId){
        Action action = defaultActions.get(actionId);
        if(action == null){
            action = customActions.get(actionId);
        }
        return action;
    }

    public static Action findActionName(String displayName){
        for(Action action : defaultActions.values()){
            if(action.getDisplayName().equalsIgnoreCase(displayName)){
                return action;
            }
        }

        for (Action action : customActions.values()){
            if(action.getDisplayName().equalsIgnoreCase(displayName)){
                return action;
            }
        }

        return null;
    }

    public static List<Action> getAllActions(){
        List<Action> actionList = new ArrayList<>(defaultActions.values());
        actionList.addAll(customActions.values());
        return actionList;
    }

    public String getDisplayName(){
        return displayName;
    }
    public String getId(){
        return id;
    }
    public boolean isCustom(){
        return isCustom;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        Action action = (Action) obj;
        return id.equals(action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
