package client.GUI;

import client.Client;
import client.GUI.Start.ClientConnect;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import jdk.jfr.BooleanFlag;
import msg.MessageProtocol;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController implements Initializable, EventHandler {

    private ClientModel model;

    @FXML
    private GridPane root;
    @FXML
    private GridPane eigFig;
    private SteinTile[] eigFigClick;
    @FXML
    private GridPane mainField;
    private Tile[][] mainFieldClick;
    @FXML
    private GridPane enmFig;
    private SteinTile[] enmFigClick;

    // Eig Fig clicked
    private boolean figClicked;
    private SteinTile markedOne;

    private StackPane eigTextBase;
    private Text eigText;
    private String name;
    private StackPane enmTextBase;
    private Text enmText;
    private String enmName;


    private CommandLineCapsule commandLineCapsule;
    @FXML
    private Label commandLine;
    private CommandCounter counter;
    private Thread counterThread;

    private boolean enmFound;
    private boolean eigZugFinished;
    private boolean enmZugFinished;
    private boolean placingFinished;

    // Callback
    private Client c;

    /**
     * Setzt einen Text in die Commandline
     * @param msg ist der Text
     */
    public void setCommandLineText( String msg ) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                commandLine.setText(msg);
            }
        });
    }




    /**
     * Setzt das der Server down ist. Spieler wird zum Start Fenster geleitet
     */
    public void srvDisconnected() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Start/start.fxml"));
            Parent root = null;
            root = loader.load();
            Scene scene = new Scene(root);

            ClientConnect controller = loader.getController();
            controller.initCommandLine("Verbindung zum Server unterbrochen!");
            Stage stage = (Stage) this.root.getScene().getWindow();
            // Swap screen
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setzt das der Gegner disconnected ist, spiel wird zurückgesetzt
     */
    public void enmDisconnected() {
        this.enmFound = false;
        // start counter
        this.startCommandCounterAgain();
    }

    /**
     * Setzt das ein Gegner gefunden wurde, spiel kann beginnen
     */
    public void foundEnm() {
        this.enmFound = true;
        this.closeCommandCounter();
    }

    /**
     * Setzt den Namen des Gegners in der GUI
     * @param name name des Gegners
     */
    public void setEnmName(String name ) {
        this.enmName = name;
        this.enmText.setText(name + "'s Spielfeld");
    }

    /**
     * Setzt den eigenen Namen in der GUI
     * @param name Name
     */
    public void setName ( String name ) {
        this.name = name;
        this.eigText.setText(name + "'s Spielfeld");
    }

    /**
     * @return den eigenen Namen
     */
    public String getName() {
        return this.name;
    }


    /**
     * Setzt den lose
     */
    public void setLose() {
        // damit keine züge mehr möglich sind
        this.enmFound = false;


        this.commandLineCapsule.setText("Du hast verloren! " + this.enmName + " hat gewonnen! Vielleich nächstes Mal :)", true);
    }

    /**
     * Wird aufgerufen wenn die win Bedingung erfüllt wurde
     * Leitet das auch an den gegner weiter
     */
    private void setWin() {
        // damit keine züge mehr möglich sind
        this.enmFound = false;


        this.commandLineCapsule.setText(this.name + " hast gewonnen!!!", true);

        this.c.send(MessageProtocol.LOSE);
    }

    private void setMainUnset() {
        for ( int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].unsetReadyToSet();
            }
        }
    }

    private void setMainSet() {
        for ( int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].setReadyToSet();
            }
        }
    }
    public void setSteinClicked( SteinTile clickedTile ) {
        if ( enmZugFinished ) {
            if ( !clickedTile.isSet() ) {
                if ( clickedTile.isActivated() ) {
                    clickedTile.deactivate();
                    this.markedOne = null;
                    this.figClicked = false;

                    this.setMainUnset();
                }
                else {
                    if ( figClicked ) {
                        for (SteinTile steinTile : this.eigFigClick) {
                            steinTile.deactivate();
                        }
                    }
                    clickedTile.activate();
                    this.markedOne = clickedTile;
                    this.figClicked = true;

                    this.setMainSet();
                }
            }
        }

    }

    private void setFieldClicked( Tile currentTile) {
        if ( this.figClicked ) {
            if ( !currentTile.isSteinTile() ) {
                this.markedOne.setSet();
                this.markedOne = null;
                this.figClicked = false;

                currentTile.setSteinTile(true, true);

                this.setMainUnset();
            }
        }
    }

    public void setEnmStein( int x, int y ){
        this.mainFieldClick[x][y].setSteinTile(true, false);
    }


    /**
     * Setzt ob der Gegner oder der eigene Spieler dran ist
     * @param to wird auf true oder false gesetzt (true: gegner ist dran, false: du bist dran)
     */
    public void setAlreadyPlaced( boolean to ) {
        if (to) {
            this.setCommandLineText("Your enemies turn");
        } else {
            this.setCommandLineText("Your turn");
        }

        this.model.setAlreadyPlaced(to);
    }




    /**
     * Wird aufgerufen wenn die view created wird. Erstellt alle GUI elemente
     */
    private void createView() {

        mainFieldClick = model.createFieldContent(mainField);
        eigFigClick = model.createEigFigContent(eigFig);
        enmFigClick = model.createEnmFigContent(enmFig);

        for (int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Tile currentTile = (Tile)mouseEvent.getSource();

                        if ( enmFound ) {
                            setFieldClicked( currentTile);
                        }
                    }
                });
            }
        }

        for ( int i = 0; i < this.eigFigClick.length; i++ ) {
            this.eigFigClick[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    SteinTile currentTile = (SteinTile)mouseEvent.getSource();

                    if ( enmFound ) {
                        setSteinClicked( currentTile );
                    }
                }
            });
        }
    }


    /**
     * Init methode um ein Callback an den {@link Client} zu setzen
     * @param c Controller {@link Client}
     */
    public void init(Client c){
        this.c = c;
    }

    /**
     * Standart initialize methode, werden ebenso startwerte und Elemente der GUI gesetzt
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model = new ClientModel();



        commandLineCapsule = new CommandLineCapsule(this.commandLine);
        counter = new CommandCounter(commandLineCapsule);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();


        figClicked = false;
        this.enmFound = false;
        this.placingFinished = false;
        this.enmZugFinished = false;
        this.eigZugFinished = false;

        this.createView();
    }


    /**
     * Setzt den Counter für die Spielersuche auf stop
     */
    public void closeCommandCounter() {
        this.commandLineCapsule.stop();
        this.counter.stop();
        try {
            this.counterThread.join();
            System.out.println("Closing Thread");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    commandLine.setText("");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Startet den Counter für die SPielersuche wieder
     */
    public void startCommandCounterAgain() {
        System.out.println("Starting again");
        this.counter.startAgain();

        this.commandLineCapsule.start();
        this.counter = new CommandCounter(commandLineCapsule);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();

        this.enmText = new Text("Gegner Spielfeld");
        enmTextBase = new StackPane();
        enmTextBase.getChildren().add(enmText);

        this.eigText = new Text("Eigenes Spielfeld");
        eigTextBase = new StackPane();
        eigTextBase.getChildren().add(eigText);

        this.root.add(eigTextBase, 0,0);
        this.root.add(enmTextBase, 1,0);
    }

    @Override
    public void handle(Event event) {

    }


    /**
     * Counter für die Spielersuche
     */
    class CommandCounter implements Runnable {

        private CommandLineCapsule line;
        private int min;
        private int sec;

        private boolean running;

        public CommandCounter( CommandLineCapsule line) {
            this.line = line;
            this.running = true;
            min = 0;
            sec = 0;
        }

        /**
         * Startet den Counter wieder
         */
        public void startAgain() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Enemy Player disconnected! Trying to lookout for new Players", true);
                }
            });
        }

        /**
         * Stopt den Couter
         */
        public void stop() {
            this.running = false;
            this.line.stop();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    line.setText("Player found!", true);
                    running = false;
                }
            });
        }

        /**
         * Run methode, laufender Counter
         */
        @Override
        public void run() {
            while ( running ) {
                try {
                    Thread.sleep(1000);
                    sec++;
                    if ( sec == 60 ) {
                        min++;
                        sec = 0;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            line.setText("Searching for Players: " + String.format("%02d", min) + ":"+String.format("%02d", sec), false);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
