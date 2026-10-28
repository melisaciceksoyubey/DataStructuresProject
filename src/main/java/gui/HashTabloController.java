package gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import logic.SimulationController;

public class HashTabloController {

    @FXML private TextArea icerikArea;

    // Hash tablosu icerigini text area'ya yukler.
    public void init(SimulationController ctrl) {
        icerikArea.setText(ctrl.getHashTabloIcerigi());
    }

    @FXML private void kapat() {
        ((Stage) icerikArea.getScene().getWindow()).close();
    }
}
