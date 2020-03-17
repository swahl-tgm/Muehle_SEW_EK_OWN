package client.GUI.Start;

import client.Client;
import client.GUI.ClientController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import jdk.jfr.BooleanFlag;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

/**
 * Ist der Controller für das erste Fenster. In diesem kann man die Größe und Schweirigkeit einstellen
 */
public class ClientConnect implements Initializable {


    @FXML
    public Label commandline;
    @FXML
    private GridPane root;
    @FXML
    private TextField host;
    @FXML
    private TextField port;
    @FXML
    private Button createGame;
    @FXML
    private TextField name;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Ruft die 2 GUI auf, leitet den User vom "Startfenster" auf das "Spielfenster" weiter
     */
    private void moveToField() {
        commandline.setText("");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../client.fxml"));
            Parent root = null;
            root = loader.load();
            Scene scene = new Scene(root);


            ClientController controller = loader.getController();
            try {
                String port = this.port.getText();
                if ( port.length() == 0 ) {
                    port = "5050";
                }
                String host  = this.host.getText();
                if ( host.length() == 0 ) {
                    host = "localhost";
                }
                System.out.println(port + ", "+ host);
                Client c = new Client(host, Integer.parseInt(port), controller);
                c.setName(name.getText());
                // run client
                executorService.submit(c);

                controller.init(c);
                Stage stage = (Stage) createGame.getScene().getWindow();
                // onclose handler
                stage.setOnCloseRequest(event -> {
                    controller.closeCommandCounter();
                    c.shutdown();
                    executorService.shutdown();
                });
                // Swap screen
                stage.setScene(scene);
            }
            catch (Exception ex) {
                commandline.setText("Kein Server konnte gefunden werden!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inizialisiert alle Variablen und setzt werde in das Layout
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // ints only

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        this.port.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), 5050, integerFilter));


        this.createGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                moveToField();
            }
        });
    }

    /**
     * @param text ist der Text der in die Commandline gesetzt wird
     */
    public void initCommandLine(String text) {
        this.commandline.setText(text);
    }
}
