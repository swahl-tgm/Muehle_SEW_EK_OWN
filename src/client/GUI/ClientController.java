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

import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller für den Client
 */
public class ClientController implements Initializable, EventHandler {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

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
    private Tile moveTile;
    private boolean toRemove;

    // win / lose
    private boolean win;
    private boolean lose;

    private String name;
    private String enmName;


    private CommandLineCapsule commandLineCapsule;
    @FXML
    private Label commandLine;
    private CommandCounter counter;
    private Thread counterThread;



    // Callback
    private Client c;

    /**
     * Sendet einen String an den gegner
     * @param msg ist der String
     */
    public void sendEnm( String msg ) {
        c.send(msg);
    }

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
     * Setzt den Spieler auf die Farbe schwarz
     */
    public void setToBlack() {
        if ( !this.model.isColorSet() ) {
            this.model.setWhite(false);
            this.model.setColorSet(true);
            this.model.setEnmZugFinished(false);
            this.model.setEigZugFinished(true);

            for ( SteinTile tile: this.eigFigClick ) {
                tile.setBlack();
            }
            for ( SteinTile tile: this.enmFigClick ) {
                tile.setWhite();
            }

            this.setWhitsTurn();
        }
    }

    /**
     * Setzt den Spieler auf die Farbe Weiß
     */
    public void setToWhite() {
        if ( !this.model.isColorSet() ) {
            this.model.setWhite(true);
            this.model.setColorSet(true);
            this.model.setEnmZugFinished(true);
            this.model.setEigZugFinished(false);

            for ( SteinTile tile: this.eigFigClick ) {
                tile.setWhite();
            }
            for ( SteinTile tile: this.enmFigClick ) {
                tile.setBlack();
            }
            this.setWhitsTurn();
        }
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
        this.model.setEnmFound(false);
        this.reset();
        // start counter
        this.startCommandCounterAgain();
    }

    /**
     * Setzt das ein Gegner gefunden wurde, spiel kann beginnen
     */
    public void foundEnm() {
        this.model.setEnmFound(true);
        this.closeCommandCounter();
    }

    /**
     * Setzt den Text in der Commandzeile (auf "Weiß ist am Zug..)
     */
    private void setWhitsTurn() {
        String temp = "(Du)";
        if ( !this.model.isWhite() ) {
            temp = "(Gegner)";
        }
        this.commandLineCapsule.setText("Weiß ist am Zug " + temp, true);
    }

    /**
     * Setzt den Text in der Commandzeile (auf "Schwarz ist am Zug..)
     */
    private void setBlacksTurn() {
        String temp = "(Du)";
        if ( this.model.isWhite() ) {
            temp = "(Gegner)";
        }
        this.commandLineCapsule.setText("Schwarz ist am Zug " + temp, true);
    }

    /**
     * Setzt den Namen des Gegners in der GUI
     * @param name name des Gegners
     */
    public void setEnmName(String name ) {
        this.enmName = name;
        this.model.setEnmText(name);
    }

    /**
     * Setzt den eigenen Namen in der GUI
     * @param name Name
     */
    public void setName ( String name ) {
        this.name = name;
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
        if ( !this.lose ) {
            // damit keine züge mehr möglich sind
            this.lose = true;
            this.c.send(MessageProtocol.WIN);
            this.commandLineCapsule.setText("Du hast verloren! " + this.enmName + " hat gewonnen! Vielleich nächstes Mal :)", true);
        }
    }

    /**
     * Wird aufgerufen wenn die win Bedingung erfüllt wurde
     */
    public void setWin() {
        if ( !this.win ) {
            this.win = true;
            this.c.send(MessageProtocol.LOSE);
            this.commandLineCapsule.setText("Du hast gewonnen! " + this.enmName + " hat verloren! Gratulation :)", true);

        }
    }

    /**
     * Setzt alle Felder auf normal zurück
     */
    private void setMainUnset() {
        for ( int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].unsetReadyToSet();
            }
        }
    }

    /**
     * Setzt alle Felder die frei sind auf Grün (markiert das plaziert werden kann)
     */
    private void setMainSet() {
        for ( int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].setReadyToSet();
            }
        }
    }

    /**
     * Eventlistener für das Klicken auf die eigenen Steine auf der Linken Seite
     * @param clickedTile ist der geklickte Stein
     */
    private void setSteinClicked( SteinTile clickedTile ) {
        if ( this.model.isEnmZugFinished() ) {
            if ( !clickedTile.isSet() ) {
                if ( clickedTile.isActivated() ) {
                    clickedTile.deactivate( this.model.isWhite() );
                    this.markedOne = null;
                    this.figClicked = false;

                    this.setMainUnset();
                }
                else {
                    if ( figClicked ) {
                        for (SteinTile steinTile : this.eigFigClick) {
                            steinTile.deactivate( this.model.isWhite() );
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

    /**
     * Eventlistener für das Klicken auf das Feld
     * @param currentTile ist das geklickte Feld
     */
    private void setFieldClicked( Tile currentTile) {
        if ( this.model.isEnmZugFinished() ) {
            if ( this.toRemove ) {
                if ( currentTile.isSteinTile() && !currentTile.isUsed() && currentTile.isWhite() != this.model.isWhite() ) {
                    currentTile.setNormal();

                    this.c.send(MessageProtocol.REMOVE + " x:"+currentTile.getX() + ",y:" + currentTile.getY());

                    if ( this.model.isWhite() ) {
                        this.setBlacksTurn();
                    }
                    else {
                        this.setWhitsTurn();
                    }
                    this.model.setEigZugFinished(true);
                    this.model.setEnmZugFinished(false);
                    toRemove = false;
                    // make normal
                    for ( Tile[] tiles : this.mainFieldClick ) {
                        for ( Tile tile : tiles ) {
                            tile.setUntouched();
                        }
                    }
                }
            }
            else if ( this.figClicked ) {
                if ( !currentTile.isSteinTile() && currentTile.isKante() ) {

                    this.markedOne.setSet();
                    this.markedOne = null;
                    this.figClicked = false;

                    currentTile.setSteinTile(true, this.model.isWhite());
                    this.c.send(MessageProtocol.PLACED + " x:"+ currentTile.getX() + ", y:"+currentTile.getY());

                    // add placed stones
                    this.model.stonePlaced();
                    // back to normal
                    this.markedOne = null;
                    this.figClicked = false;

                    if ( this.model.isPlacingFinished() ) {
                        String toAdd = "";
                        if ( this.model.isWhite() ) {
                            toAdd = "Schwarz ist am Zug (Gegner)";
                        }
                        else {
                            toAdd = "Weiß ist am Zug (Gegner)";
                        }
                        this.commandLineCapsule.setText("Letzter Stein plaziert. Nun kannst du im nächsten Zug deine Steine verschieben! " + toAdd, true);
                    }
                    else {
                        if ( this.model.isWhite() ) {
                            this.setBlacksTurn();
                        }
                        else {
                            this.setWhitsTurn();
                        }
                    }

                    this.setMainUnset();


                    if ( this.model.checkForMuehle( this.mainFieldClick ) ) {
                        if ( this.model.checkForRemovableTiles( this.mainFieldClick ) ) {
                            this.c.send(MessageProtocol.STARTREMOVE);
                            this.showRemove();
                            this.toRemove = true;

                            this.model.setEigZugFinished(false);
                            this.model.setEnmZugFinished(true);
                            this.commandLineCapsule.setText("Mühle! Entferne einen freien Stein des Gegners!", true);
                        }
                        else {
                            this.model.setEigZugFinished(true);
                            this.model.setEnmZugFinished(false);
                            String temp;
                            if ( this.model.isWhite() ) {
                                temp = "Schwarz ist am Zug (Gegner)";
                            }
                            else {
                                temp = "Weiß ist am Zug (Gegner)";
                            }
                            this.commandLineCapsule.setText("Mühle! Es sind jedoch alle Steine des Gegners in einer Mühle und können nicht entfernt werden. Dein Zug ist somit beendet. " + temp, true);
                        }
                    }
                    else {
                        this.model.setEigZugFinished(true);
                        this.model.setEnmZugFinished(false);
                    }
                }
            }
            else if ( this.model.isPlacingFinished() ) {
                boolean error = true;
                if ( this.moveTile != null && !currentTile.isSteinTile()  ) {
                    System.out.println("Moving tile from: x:" + moveTile.getX() + ", y:" + moveTile.getY() + "; to x:" + currentTile.getX() + ", y:" + currentTile.getY());
                    if ( !this.model.isJumpingAllow() ) {
                        if ( currentTile.getX() == moveTile.getX() ) {
                            // vertical
                            int diff = currentTile.getY() - moveTile.getY();
                            if ( diff < 0 ) {
                                diff *= -1;
                            }

                            if ( moveTile.getX() == 0 || moveTile.getX() == 6 ) {
                                if ( diff == 3 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            else if ( moveTile.getX() == 1 || moveTile.getX() == 5 ) {
                                if ( diff == 2 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            else if ( moveTile.getX() == 2 || moveTile.getX() == 3 || moveTile.getX() == 4 ) {
                                if ( diff == 1 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            if ( !error ) {
                                System.out.println("Moved correctly!");
                                this.unsetAllGreen();
                                currentTile.setSteinTile(true, this.model.isWhite());
                                if ( moveTile.isUsed() ) {
                                    // check opposite (horizontal)
                                    this.fixUsedNeighbors(moveTile, HORIZONTAL, moveTile.getY());
                                }
                                moveTile.setNormal();
                                this.c.send(MessageProtocol.MOVED + " sx:"+this.moveTile.getX() + ",sy:"+this.moveTile.getY() + ",x:" + currentTile.getX() + ",y:" + currentTile.getY());
                            }
                        }
                        else {
                            int diff = currentTile.getX() - moveTile.getX();
                            if ( diff < 0 ) {
                                diff *= -1;
                            }
                            // horizontal
                            if ( moveTile.getY() == 0 || moveTile.getY() == 6 ) {
                                if ( diff == 3 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            else if ( moveTile.getY() == 1 || moveTile.getY() == 5 ) {
                                if ( diff == 2 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            else if ( moveTile.getY() == 2 || moveTile.getY() == 3 || moveTile.getY() == 4 ) {
                                if ( diff == 1 ) {
                                    // correct
                                    error = false;
                                }
                            }
                            if ( !error ) {
                                System.out.println("Moved correctly!");
                                this.unsetAllGreen();
                                currentTile.setSteinTile(true, this.model.isWhite());
                                if ( moveTile.isUsed() ) {
                                    // check opposite (vertical)
                                    this.fixUsedNeighbors(moveTile, VERTICAL, moveTile.getX());
                                }
                                moveTile.setNormal();
                                this.c.send(MessageProtocol.MOVED + " sx:"+this.moveTile.getX() + ",sy:"+this.moveTile.getY() + ",x:" + currentTile.getX() + ",y:" + currentTile.getY());
                            }
                        }
                    }
                    else {
                        if ( this.mainFieldClick[currentTile.getX()][currentTile.getY()].isKante() && !this.mainFieldClick[currentTile.getX()][currentTile.getY()].isSteinTile() ) {
                            System.out.println("Moved correctly!");
                            this.unsetAllGreen();
                            currentTile.setSteinTile(true, this.model.isWhite());
                            if ( moveTile.isUsed() ) {
                                // check opposite (vertical)
                                this.fixUsedNeighbors(moveTile, VERTICAL, moveTile.getX());
                            }
                            moveTile.setNormal();
                            this.c.send(MessageProtocol.MOVED + " sx:"+this.moveTile.getX() + ",sy:"+this.moveTile.getY() + ",x:" + currentTile.getX() + ",y:" + currentTile.getY());
                        }
                    }
                    if ( this.model.checkForMuehle( this.mainFieldClick ) ) {
                        if ( this.model.checkForRemovableTiles( this.mainFieldClick ) ) {
                            this.c.send(MessageProtocol.STARTREMOVE);
                            this.showRemove();
                            this.toRemove = true;

                            this.model.setEigZugFinished(false);
                            this.model.setEnmZugFinished(true);
                            this.commandLineCapsule.setText("Mühle! Entferne einen freien Stein des Gegners!", true);
                        }
                        else {
                            this.model.setEigZugFinished(true);
                            this.model.setEnmZugFinished(false);
                            String temp;
                            if ( this.model.isWhite() ) {
                                temp = "Schwarz ist am Zug (Gegner)";
                            }
                            else {
                                temp = "Weiß ist am Zug (Gegner)";
                            }
                            this.commandLineCapsule.setText("Mühle! Es sind jedoch alle Steine des Gegners in einer Mühle und können nicht entfernt werden. Dein Zug ist somit beendet. " + temp, true);
                        }
                    }
                    else {
                        this.model.setEigZugFinished(true);
                        this.model.setEnmZugFinished(false);
                    }

                }
                else {
                    this.setMoveStein(currentTile);
                }
            }
        }
    }

    /**
     * Wenn ein Spieler einen Stein bewegt, wird diese Methode aufgerufen um, wenn dieser Stein in einer Mühle war, die Nachbarn freizugeben
     * @param moveTile Stein der bewegt wird
     * @param orientation Horizontal | Vertical
     * @param rowOrColumn die Reihe oder Spalte
     */
    private void fixUsedNeighbors( Tile moveTile, int orientation, int rowOrColumn ) {
        int diff = 0;
        int diff2 = 0;
        // for better understanding
        int row = rowOrColumn;
        int column = rowOrColumn;
        int leftX = -1, topY = -1;
        int tempBreak = -1;
        if ( orientation == HORIZONTAL ) {
            // Horizontal ----- moving vertical --> checking horizontal
            if ( row == 0 || row == 6 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    diff2 = 0;
                    if ( this.mainFieldClick[i][row].isSteinTile() && this.mainFieldClick[i][row].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row].isUsed() ) {
                        if ( i == 0 || i == 6 ) {
                            if ( row == 0 ) {
                                diff = 3;
                                diff2 = 6;
                            }
                            else {
                                diff = -3;
                                diff2 = -6;
                            }
                        }
                        else if ( i == 3 ) {
                            if ( row == 0 ) {
                                diff = 1;
                                diff2 = 2;
                            }
                            else {
                                diff = -1;
                                diff2 = -2;
                            }
                        }
                        if ( diff != 0 ) {
                            if (!( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff].isUsed()) && !( this.mainFieldClick[i][row + diff2].isSteinTile() && this.mainFieldClick[i][row + diff2].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff2].isUsed())) {
                                this.mainFieldClick[i][row].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                            }
                        }
                    }
                }
            }
            else if ( row == 1 || row == 5 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    diff2 = 0;
                    if ( this.mainFieldClick[i][row].isSteinTile() && this.mainFieldClick[i][row].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row].isUsed() ) {
                        if ( i == 1 || i == 5 ) {
                            if ( row == 1 ) {
                                diff = 2;
                                diff2 = 4;
                            }
                            else {
                                diff = -2;
                                diff2 = -4;
                            }
                            if ( !( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff].isUsed()) && !( this.mainFieldClick[i][row + diff2].isSteinTile() && this.mainFieldClick[i][row + diff2].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff2].isUsed()) ) {
                                this.mainFieldClick[i][row].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                            }
                        }
                        else if ( i == 3 ) {
                            if ( !( (this.mainFieldClick[i][row + 1].isSteinTile() && this.mainFieldClick[i][row + 1].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + 1].isUsed()) && (this.mainFieldClick[i][row - 1].isSteinTile() && this.mainFieldClick[i][row - 1].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row - 1].isUsed()) ) ) {
                                this.mainFieldClick[i][row].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                            }
                        }
                    }
                }
            }
            else if ( row == 2 || row == 3 || row == 4 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    diff2 = 0;
                    if ( this.mainFieldClick[i][row].isSteinTile() && this.mainFieldClick[i][row].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row].isUsed() ) {
                        if ( row == 3 ) {
                            if ( i == 0 || i == 6 ) {
                                diff = 3;
                            }
                            else if ( i == 1 || i == 5) {
                                diff = 2;
                            }
                            else if ( i == 2 || i == 4) {
                               diff = 1;
                            }
                            if ( diff != 0 ) {
                                if ( !( (this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff].isUsed()) && (this.mainFieldClick[i][row - diff].isSteinTile() && this.mainFieldClick[i][row - diff].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row - diff].isUsed()) ) ) {
                                    this.mainFieldClick[i][row].setUsed(false);
                                    this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                                }
                            }
                        }
                        else { // 2 || 4
                            if ( i == 2 || i == 4 ) {
                                diff = 1;
                                diff2 = 2;
                                if ( row == 4 ) {
                                    diff = -1;
                                    diff2 = -2;
                                }
                            }
                            else if ( i == 3 ) {
                                diff = -1;
                                diff2 = -2;
                                if ( row == 4 ) {
                                    diff = 1;
                                    diff2 = 2;
                                }
                            }
                            if ( diff != 0 ) {
                                if ( !( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row + diff].isUsed()) && !( this.mainFieldClick[i][row + diff2].isSteinTile() && this.mainFieldClick[i][row + diff2].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row + diff2].isUsed()) ) {
                                    this.mainFieldClick[i][row].setUsed(false);
                                    this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            // Vertical |
            if ( column == 0 || column == 6 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    diff2 = 0;
                    if ( this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed() ) {
                        if ( i == 0 || i == 6 ) {
                            if ( column == 0 ) {
                                diff = 3;
                                diff2 = 6;
                            }
                            else {
                                diff = -3;
                                diff2 = -6;
                            }
                        }
                        else if ( i == 3 ) {
                            if ( column == 0 ) {
                                diff = 1;
                                diff2 = 2;
                            }
                            else {
                                diff = -1;
                                diff2 = -2;
                            }
                        }
                        if ( diff != 0 ) {
                            if ( !( this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff][i].isUsed()) && !( this.mainFieldClick[column + diff2][i].isSteinTile() && this.mainFieldClick[column + diff2][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff2][i].isUsed()) ) {
                                this.mainFieldClick[column][i].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + column + ",y:" + i);
                            }
                        }
                    }
                }
            }
            else if ( column == 1 || column == 5 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    diff2 = 0;
                    if ( this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed() ) {
                        if ( i == 1 || i == 5 ) {
                            if ( column == 1 ) {
                                diff = 2;
                                diff2 = 4;
                            }
                            else {
                                diff = -2;
                                diff2 = -4;
                            }
                            if ( !( this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff][i].isUsed()) && !( this.mainFieldClick[column + diff2][i].isSteinTile() && this.mainFieldClick[column + diff2][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff2][i].isUsed()) ) {
                                this.mainFieldClick[i][row].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + i + ",y:" + row);
                            }
                        }
                        else if ( i == 3 ) {
                            if ( !( (this.mainFieldClick[column + 1][i].isSteinTile() && this.mainFieldClick[column + 1][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + 1][i].isUsed()) && (this.mainFieldClick[column - 1][i].isSteinTile() && this.mainFieldClick[column - 1][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column - 1][i].isUsed()) ) ) {
                                this.mainFieldClick[column][i].setUsed(false);
                                this.c.send(MessageProtocol.SETUNUSED + " x:" + column + ",y:" + i);
                            }
                        }
                    }
                }
            }
            else if ( column == 2 || column == 3 || column == 4 ) {
                for (int i = 0; i < 7; i++) {
                    diff = 0;
                    diff2 = 0;
                    if (this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed()) {
                        if (column == 3) {
                            if (i == 0 || i == 6) {
                                diff = 3;
                            } else if (i == 1 || i == 5) {
                                diff = 2;
                            } else if (i == 2 || i == 4) {
                                diff = 1;
                            }
                            if (diff != 0) {
                                if (!((this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + diff][i].isUsed()) && (this.mainFieldClick[column - diff][i].isSteinTile() && this.mainFieldClick[column - diff][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column - diff][i].isUsed()))) {
                                    this.mainFieldClick[column][i].setUsed(false);
                                    this.c.send(MessageProtocol.SETUNUSED + " x:" + column + ",y:" + i);
                                }
                            }
                        } else {
                            if (i == 2 || i == 4) {
                                diff = 1;
                                diff2 = 2;
                                if (column == 4) {
                                    diff = -1;
                                    diff2 = -2;
                                }
                            } else if (i == 3) {
                                diff = -1;
                                diff2 = -2;
                                if (column == 4) {
                                    diff = 1;
                                    diff2 = 2;
                                }
                            }
                            if (diff != 0) {
                                if ( !(this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + diff][i].isUsed()) && !(this.mainFieldClick[column + diff2][i].isSteinTile() && this.mainFieldClick[column + diff2][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + diff2][i].isUsed()) ) {
                                    this.mainFieldClick[column][i].setUsed(false);
                                    this.c.send(MessageProtocol.SETUNUSED + " x:" + column + ",y:" + i);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Zeigt an, welche Steine des Gegners entfernt werden können
     */
    private void showRemove() {
        for ( Tile[] tiles : this.mainFieldClick ) {
            for ( Tile tile: tiles ) {
                if ( tile.isWhite() == !this.model.isWhite() && !tile.isUsed()) {
                    tile.setRemovable();
                }
            }
        }
    }

    /**
     * Markiert die Felder grün, die freu sind (keine Steine sind)
     */
    private void setAllFreeGreen() {
        for ( Tile[] tiles : this.mainFieldClick ) {
            for ( Tile tile : tiles ) {
                if ( tile.isKante() && !tile.isSteinTile() ) {
                    tile.setReadyToSet();
                }
            }
        }
    }

    /**
     * Setzt alle Felder die Grün sind, auf normal zurück
     */
    private void unsetAllGreen() {
        for ( Tile[] tiles : this.mainFieldClick ) {
            for ( Tile tile : tiles ) {
                if ( tile.isGreen() ) {
                    tile.unsetReadyToSet();
                }
            }
        }
    }

    /**
     * Setzt alle Felder die frei sind um ein Feld herum auf grün
     * @param currentTile das Feld
     */
    private void setNearFreeGreen( Tile currentTile ) {
        for ( int i = currentTile.getX() + 1; i < 7; i++ ) {
            if ( ( currentTile.getY() == 3 && i == 3 )) {
                break;
            }
            if ( this.mainFieldClick[i][currentTile.getY()].isKante() ) {
                if ( !this.mainFieldClick[i][currentTile.getY()].isSteinTile() ) {
                    this.mainFieldClick[i][currentTile.getY()].setReadyToSet();
                }
                break;
            }
        }
        for ( int i = currentTile.getX() -1 ; i >= 0; i-- ) {
            if ( ( currentTile.getY() == 3 && i == 3 )) {
                break;
            }
            if ( this.mainFieldClick[i][currentTile.getY()].isKante() ) {
                if ( !this.mainFieldClick[i][currentTile.getY()].isSteinTile() ) {
                    this.mainFieldClick[i][currentTile.getY()].setReadyToSet();
                }
                break;
            }
        }

        for ( int i = currentTile.getY() + 1; i < 7; i++ ) {
            if ( currentTile.getX() == 3 && i == 3 ) {
                break;
            }
            if ( this.mainFieldClick[currentTile.getX()][i].isKante() ) {
                if ( !this.mainFieldClick[currentTile.getX()][i].isSteinTile() ) {
                    this.mainFieldClick[currentTile.getX()][i].setReadyToSet();
                }
                break;
            }
        }
        for ( int i = currentTile.getY() - 1; i >= 0; i-- ) {
            if ( currentTile.getX() == 3 && i == 3 ) {
                break;
            }
            if ( this.mainFieldClick[currentTile.getX()][i].isKante() ) {
                if ( !this.mainFieldClick[currentTile.getX()][i].isSteinTile() ) {
                    this.mainFieldClick[currentTile.getX()][i].setReadyToSet();
                }
                break;
            }
        }
    }

    /**
     * Setzt den Stein der bewegt werden soll
     * @param currentTile ist der Stein der angeklickt wurde und welcher später zum bewegen benutzt wird
     */
    private void setMoveStein( Tile currentTile ) {
        if ( currentTile.isWhite() == this.model.isWhite() ) {
            // if only 3 stones left
            if ( currentTile == this.moveTile ) {
                this.moveTile = null;
                currentTile.setUntouched();
                this.unsetAllGreen();
            }
            else {
                if ( currentTile.isSteinTile() && currentTile.isWhite() == this.model.isWhite() ) {
                    for ( Tile[] tiles : this.mainFieldClick ) {
                        for ( Tile tile : tiles ) {
                            tile.setUntouched();
                        }
                    }
                    this.unsetAllGreen();

                    if ( this.model.isJumpingAllow() ) {
                        this.setAllFreeGreen();
                    }
                    else {
                        this.setNearFreeGreen( currentTile );
                    }
                    this.moveTile = currentTile;
                    this.moveTile.setMoveable();
                }
            }
        }
    }

    /**
     * Löscht wenn der Gegner einen Stein platziert den Stein aus der Anzeige (rechts)
     */
    private void removeEnmSteinFromSide() {
        for ( SteinTile tile : this.enmFigClick ) {
            if ( !tile.isSet() ) {
                tile.setSet();
                break;
            }
        }
    }

    /**
     * Setzt einen Stein des Gegners auf Used, somit ist dieser Stein in einer Mühle und kann nicht entfernt werden
     * @param x X Koordinate
     * @param y Y Koordinate
     */
    public void setEnmUsed( int x, int y ) {
        this.mainFieldClick[x][y].setUsed(true);
    }

    /**
     * Setzt einen Stein des Gegners auf nicht Used, somit ist dieser Stein nicht mehr in einer Mühle und kann entfernt werden
     * @param x X Koordinate
     * @param y > Koordinate
     */
    public void setEnmUnused( int x, int y ) {
        this.mainFieldClick[x][y].setUsed(false);
    }

    /**
     * Startet den Remove vorgang
     */
    public void startRemove() {
        this.model.setEigZugFinished(true);
        this.model.setEnmZugFinished(false);
        if ( this.model.isWhite() ) {
            this.setBlacksTurn();
        }
        else {
            this.setWhitsTurn();
        }
    }


    /**
     * Löscht ein Feld, auf wunsch des Gegners
     * @param x X Koordinate
     * @param y Y Koordinate
     */
    public void removeTile( int x, int y ) {
        this.mainFieldClick[x][y].setNormal();
        if ( this.model.ownStoneRemoved() ) {
            this.setLose();
        }
        else {
            this.model.setEigZugFinished(false);
            this.model.setEnmZugFinished(true);
            if ( !this.model.isWhite() ) {
                this.setBlacksTurn();
            }
            else {
                this.setWhitsTurn();
            }

            // if only 3 left -> message
            if ( this.model.isJumpingAllow() ) {
                String temp;
                if ( this.model.isWhite() ) {
                    temp = "Schwarz ist am Zug (Gegner)";
                }
                else {
                    temp = "Weiß ist am Zug (Gegner)";
                }
                this.commandLineCapsule.setText("Du hast nur noch 3 Steine! Mit diesen kannst du nun auf jedes freie Feld auf dem Brett hüpfen! " + temp, true);
            }
        }
    }

    /**
     * Setzt einen Stein (in der eigenen GUI) den der Gegner bei sich gesetzt hat gesetzt hat
     * @param x X Koordinate
     * @param y Y Koordinate
     */
    public void setEnmStein( int x, int y ){
        this.removeEnmSteinFromSide();

        this.mainFieldClick[x][y].setSteinTile(true, !this.model.isWhite());

        try {
            // check if own client can move tiles
            if ( this.model.isPlacingFinished() && !this.model.checkIfPlayerCanMove( mainFieldClick ) ) {
                this.setLose();
            }
            else {
                this.model.setEigZugFinished(false);
                this.model.setEnmZugFinished(true);
                if ( !this.model.isWhite() ) {
                    this.setBlacksTurn();
                }
                else {
                    this.setWhitsTurn();
                }
            }
        }
        catch (Exception ex ) {
            ex.printStackTrace();
        }

    }

    /**
     * Bewegt einen Stein den der Gegner ausgewählt hat von dem Startpunkt zum entgültig ausgewählten Feld (in der eigenen GUI)
     * @param startX Startwert X
     * @param startY Startwert Y
     * @param toX Endwert X
     * @param toY Endwert Y
     */
    public void moveEnmStein( int startX, int startY, int toX, int toY ) {

        this.mainFieldClick[startX][startY].setNormal();
        this.mainFieldClick[toX][toY].setSteinTile(true, !this.model.isWhite());
        this.mainFieldClick[toX][toY].setUsed(false);

        // check if own client can move tiles
        if ( this.model.checkIfPlayerCanMove( mainFieldClick ) ) {
            this.model.setEigZugFinished(false);
            this.model.setEnmZugFinished(true);
            if ( !this.model.isWhite() ) {
                this.setBlacksTurn();
            }
            else {
                this.setWhitsTurn();
            }
        }
        else {
            this.setLose();
        }
    }

    /**
     * Setzt für den Neustart des Spiels alles zurücl
     */
    private void reset() {
        try {
            this.model.reset();

            this.figClicked = false;
            this.toRemove = false;

            this.markedOne = null;
            this.moveTile = null;

            this.lose = false;
            this.win = false;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    createView();
                }
            });
        }
        catch ( Exception ex ){
            ex.printStackTrace();
        }
    }


    /**
     * Wird aufgerufen wenn die view created wird. Erstellt alle GUI elemente
     */
    private void createView() {
        mainField.getChildren().removeAll(mainField.getChildren());
        eigFig.getChildren().removeAll(eigFig.getChildren());
        enmFig.getChildren().removeAll(enmFig.getChildren());

        mainFieldClick = model.createFieldContent(mainField);
        eigFigClick = model.createEigFigContent(eigFig);
        enmFigClick = model.createEnmFigContent(enmFig);

        for (int i = 0; i < this.mainFieldClick.length; i++ ) {
            for ( int j = 0; j < this.mainFieldClick[i].length; j++ ) {
                this.mainFieldClick[i][j].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Tile currentTile = (Tile)mouseEvent.getSource();

                        if ( model.isEnmFound() && !win && !lose ) {
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

                    if ( model.isEnmFound() && !win && !lose ) {
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
        model = new ClientModel(this);



        commandLineCapsule = new CommandLineCapsule(this.commandLine);
        counter = new CommandCounter(commandLineCapsule);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();


        this.figClicked = false;
        this.toRemove = false;


        this.lose = false;
        this.win = false;

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
        this.commandLineCapsule.start();
        this.counter.startAgain();

        this.counter = new CommandCounter(commandLineCapsule);
        counterThread = new Thread(counter);
        counterThread.setDaemon(true);
        counterThread.start();
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
