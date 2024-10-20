package aydin.firebasedemospring2024;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        DemoApp.setRoot("primary");
    }
}
