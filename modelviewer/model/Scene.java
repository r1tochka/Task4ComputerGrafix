package modelviewer.model;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models;
    private int activeModelIndex;

    public Scene() {
        models = new ArrayList<>();
        activeModelIndex = -1;
    }

    public void addModel(Model model) {
        models.add(model);
        if (activeModelIndex == -1) {
            activeModelIndex = 0;
        }
    }

    public void removeModel(int index) {
        if (index >= 0 && index < models.size()) {
            models.remove(index);
            if (models.isEmpty()) {
                activeModelIndex = -1;
            } else if (activeModelIndex >= models.size()) {
                activeModelIndex = models.size() - 1;
            }
        }
    }

    public Model getModel(int index) {
        if (index >= 0 && index < models.size()) {
            return models.get(index);
        }
        return null;
    }

    public Model getActiveModel() {
        if (activeModelIndex >= 0 && activeModelIndex < models.size()) {
            return models.get(activeModelIndex);
        }
        return null;
    }

    public void setActiveModel(int index) {
        if (index >= 0 && index < models.size()) {
            activeModelIndex = index;
        }
    }

    public List<Model> getAllModels() {
        return new ArrayList<>(models);
    }

    public int getActiveModelIndex() {
        return activeModelIndex;
    }

    public int getModelCount() {
        return models.size();
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }
}
