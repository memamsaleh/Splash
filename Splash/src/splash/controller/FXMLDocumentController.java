/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splash.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import splash.model.Helper;
import splash.model.Layer;
import splash.model.Object2D;
import splash.model.ObjectLayer;
import splash.model.Property;
import splash.model.RawLayer;
import splash.model.ResourceManager;
import splash.model.ShortcutManager;
import splash.model.SimpleLayer;
import splash.model.Tool;

/**
 * FXML Controller class
 *
 * @author MEmam
 */
public class FXMLDocumentController implements Initializable {

    Layer[] layers;

    ArrayList<SimpleLayer> spl;

    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ListView<String> layersList;
    @FXML
    private ListView<String> toolsList;
    @FXML
    private Button newLayBtn;
    @FXML
    private Button delLayBtn;
    @FXML
    private Canvas drawingCanvas;
    @FXML
    private MenuItem saveMenItem;
    @FXML
    private TableView<?> propTable;
    @FXML
    private TableColumn<Map.Entry<String, Property>, String> propCol;
    @FXML
    private TableColumn<Map.Entry<String, Property>, String> valCol;

    private Tool selectedTool;
    private ArrayList<Tool> tools;
    ArrayList<KeyCode> pressedkeys = new ArrayList<>();

    boolean iskeyPressed(KeyCode code) {
        return pressedkeys.contains(code);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GUIMgr.init(this);
        pw = drawingCanvas.getGraphicsContext2D().getPixelWriter();
        ObservableList<String> items = FXCollections.observableArrayList();
        // Tools are loaded in Splash.java
        tools = ResourceManager.getTools();
        for (Tool tool : tools) {
            items.add(tool.getId());
        }
        toolsList.setItems(items);
        colorPicker.setValue(Color.BLACK);
        toolsList.getSelectionModel().select(0);
        selectedTool = tools.get(0);
        selectedTool.select();

        // events
        drawingCanvas.setOnMousePressed(this::canvasMousePressed);
        drawingCanvas.setOnMouseMoved(this::canvasMouseMoved);
    }

    public void CanvasSize(int width, int height) {
        drawingCanvas.setWidth(width);
        drawingCanvas.setHeight(height);
    }

    public Tool toolSelected() {
        return selectedTool;
    }

    public int layerSelected() {
        return Integer.parseInt(layersList.getSelectionModel().getSelectedItem());
    }

    private void canvasMousePressed(MouseEvent ev) {
        if (ev.getButton() == MouseButton.PRIMARY) {
            GUIMgr.getWorkSpace().primaryKey((int) ev.getX(), (int) ev.getY(), GUIMgr.getSelectedTool(), colorPicker.getValue());
        } else if (ev.getButton() == MouseButton.SECONDARY) {
            GUIMgr.getWorkSpace().secKey();
        }
    }

    private void canvasMouseMoved(MouseEvent ev) {
        int xp = (int) ev.getX(), yp = (int) ev.getY();
        if (iskeyPressed(KeyCode.SHIFT)) {
            double p = Math.max(xp, yp);
            p = Math.min(drawingCanvas.getWidth(), p);
            p = Math.min(drawingCanvas.getHeight(), p);
            GUIMgr.getWorkSpace().mouseMoved((int) p, (int) p);
        } else {
            GUIMgr.getWorkSpace().mouseMoved(xp, yp);
        }
    }

    Color getPixel(int x, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    PixelWriter pw;

    void setPixel(int x, int y, Color col) {
        pw.setArgb(x, y, Helper.getARGB(col));
    }

    @FXML
    private void addLayer(ActionEvent e) {
        GUIMgr.newLayer(new RawLayer());
    }

    @FXML
    private void removeLayer(ActionEvent e) {
        String id = layersList.getSelectionModel().getSelectedItem();
        GUIMgr.removeLayer(Integer.parseInt(id));
    }

    public void refreshLayers() {
        ObservableList<String> items = FXCollections.observableArrayList();
        Layer[] layers = GUIMgr.getWorkSpace().getLayers();
        for (Layer layer : layers) {
            items.add(String.valueOf(layer.getId()));
        }
        layersList.setItems(items);

    }

    @FXML
    private void selectTool(MouseEvent event) {
        String id = toolsList.getSelectionModel().getSelectedItem();
        for (Tool tool : tools) {
            if (tool.getId().equals(id)) {
                selectedTool = tool;
            }
        }
        selectedTool.select();
    }

    @FXML
    private void selectLayer(MouseEvent event) {
        GUIMgr.getWorkSpace().selectLayer(layerSelected());
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        pressedkeys.add(event.getCode());
        ShortcutManager.checkComb(pressedkeys);
    }

    @FXML
    private void keyReleased(KeyEvent event) {
        pressedkeys.remove(event.getCode());
    }

    @FXML
    private void NewProject(ActionEvent event) {
        GUIMgr.CreateNewProject();
    }

    @FXML
    private void Save(ActionEvent event) {
        GUIMgr.Save();
    }

    @FXML
    private void SaveAs(ActionEvent event) {
        GUIMgr.SaveAs();
        if (GUIMgr.getCurrentFile() != null) {
            saveMenItem.setDisable(false);
        }
    }

    /*private void fillTable()
    {
        Layer selectedLayer = GUIMgr.getWorkSpace().getSelectedLayer();
        if(selectedLayer instanceof ObjectLayer)
        {
            Object2D obj = ((ObjectLayer) selectedLayer).getObject();
            Map<String, Property> map = obj.getEditableList();
        }
        propCol = new TableColumn<>("Key");
        propCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, Property>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, Property>, String> p) {
                return new SimpleStringProperty(p.getValue().getKey());
            }
        });
        valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, Property>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, Property>, String> p) {
                return new SimpleStringProperty(p.getValue().getValue().toString());
            }
        });

        ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(map.entrySet());
        final TableView<Map.Entry<String,String>> table = new TableView<>(items);

        table.getColumns().setAll(propCol, valCol);
    }*/
    @FXML
    private void LoadProject(ActionEvent event) {
        GUIMgr.loadProject("C:\\Users\\Hesham\\Documents\\test");
    }
}
