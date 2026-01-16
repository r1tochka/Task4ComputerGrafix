package modelviewer.controller;

import modelviewer.model.Scene;
import modelviewer.model.Model;
import modelviewer.io.ObjReader;
import modelviewer.io.ObjWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SceneController {
    private Scene scene;
    private ModelController activeModelController;
    private List<ModelController> modelControllers;

    public SceneController() {
        scene = new Scene();
        modelControllers = new ArrayList<>();
    }

    public Model loadModel(String filePath) throws Exception {
        String content = new String(Files.readAllBytes(new File(filePath).toPath()));
        String fileName = new File(filePath).getName();
        Model model = ObjReader.read(content, fileName);
        scene.addModel(model);

        ModelController controller = new ModelController(model);
        modelControllers.add(controller);
        activeModelController = controller;

        return model;
    }

    public void saveActiveModel(String filePath) throws IOException {
        Model model = scene.getActiveModel();
        if (model != null) {
            List<modelviewer.model.Vector3> vertices = convertVertices(model.getVertices());
            List<modelviewer.model.Polygon> polygons = model.getPolygons();
            ObjWriter.write(filePath, vertices, polygons);
        }
    }

    public void addModel(Model model) {
        scene.addModel(model);
        ModelController controller = new ModelController(model);
        modelControllers.add(controller);
        activeModelController = controller;
    }

    public void removeModel(int index) {
        scene.removeModel(index);
        if (index >= 0 && index < modelControllers.size()) {
            modelControllers.remove(index);
        }

        if (!scene.isEmpty()) {
            activeModelController = new ModelController(scene.getActiveModel());
        } else {
            activeModelController = null;
        }
    }

    public void setActiveModel(int index) {
        scene.setActiveModel(index);
        if (index >= 0 && index < modelControllers.size()) {
            activeModelController = modelControllers.get(index);
        } else if (!modelControllers.isEmpty()) {
            activeModelController = modelControllers.get(0);
        } else {
            activeModelController = null;
        }
    }

    public Model getActiveModel() {
        return scene.getActiveModel();
    }

    public List<Model> getAllModels() {
        return scene.getAllModels();
    }

    public ModelController getActiveModelController() {
        return activeModelController;
    }

    public ModelController getModelController(int index) {
        if (index >= 0 && index < modelControllers.size()) {
            return modelControllers.get(index);
        }
        return null;
    }

    public List<ModelController> getAllModelControllers() {
        return new ArrayList<>(modelControllers);
    }

    public int getActiveModelIndex() {
        return scene.getActiveModelIndex();
    }

    private List<modelviewer.model.Vector3> convertVertices(List<modelviewer.model.Vector3f> vertices) {
        List<modelviewer.model.Vector3> result = new ArrayList<>();
        for (modelviewer.model.Vector3f v : vertices) {
            result.add(new modelviewer.model.Vector3(v.x, v.y, v.z));
        }
        return result;
    }
}