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


    private String name;
    private String enmName;


    private CommandLineCapsule commandLineCapsule;
    @FXML
    private Label commandLine;
    private CommandCounter counter;
    private Thread counterThread;



    // Callback
    private Client c;

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

    private void setWhitsTurn() {
        String temp = "(Du)";
        if ( !this.model.isWhite() ) {
            temp = "(Gegner)";
        }
        this.commandLineCapsule.setText("Weiß ist am Zug " + temp, true);
    }

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
        // damit keine züge mehr möglich sind
        this.model.setEnmFound(false);

        this.commandLineCapsule.setText("Du hast verloren! " + this.enmName + " hat gewonnen! Vielleich nächstes Mal :)", true);
    }

    /**
     * Wird aufgerufen wenn die win Bedingung erfüllt wurde
     * Leitet das auch an den gegner weiter
     */
    private void setWin() {
        // damit keine züge mehr möglich sind
        this.model.setEnmFound(false);

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

    private void setSteinClicked( SteinTile clickedTile ) {
        if ( this.model.isEnmZugFinished() ) {
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
            if ( !currentTile.isSteinTile() && currentTile.isKante() && currentTile.isWhite() == this.model.isWhite() ) {

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
                }
            }
        }
        else if ( this.model.isPlacingFinished() ) {
            boolean error = true;
            if ( this.moveTile != null && !currentTile.isSteinTile() ) {
                System.out.println("Moving tile from: x:" + moveTile.getX() + ", y:" + moveTile.getY() + "; to x:" + currentTile.getX() + ", y:" + currentTile.getY());
                if ( currentTile.getX() == moveTile.getX() ) {
                    // vertical
                    int diff = currentTile.getY() - moveTile.getY();
                    if ( diff < 0 ) {
                        diff *= -1;
                    }

                    if ( moveTile.getX() == 0 || moveTile.getX() == 6 ) {
                        if ( diff == 3 ) {
                            // correct
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    else if ( moveTile.getX() == 1 || moveTile.getX() == 5 ) {
                        if ( diff == 2 ) {
                            // correct
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    else if ( moveTile.getX() == 2 || moveTile.getX() == 3 || moveTile.getX() == 4 ) {
                        if ( diff == 1 ) {
                            // correct
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    if ( !error ) {
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
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    else if ( moveTile.getY() == 1 || moveTile.getY() == 5 ) {
                        if ( diff == 2 ) {
                            // correct
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    else if ( moveTile.getY() == 2 || moveTile.getY() == 3 || moveTile.getY() == 4 ) {
                        if ( diff == 1 ) {
                            // correct
                            System.out.println("Correct");
                            error = false;
                        }
                    }
                    if ( !error ) {
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
                    this.showRemove();
                    this.toRemove = true;

                    this.model.setEigZugFinished(false);
                    this.model.setEnmZugFinished(true);
                    this.commandLineCapsule.setText("Mühle! Entferne einen freien Stein des Gegners!", true);
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

    private void fixUsedNeighbors( Tile moveTile, int orientation, int rowOrColumn ) {
        int diff = 0;
        // for better understanding
        int row = rowOrColumn;
        int column = rowOrColumn;
        if ( orientation == HORIZONTAL ) {
            // Horizontal -----
            if ( row == 0 || row == 6 ) {
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    if ( this.mainFieldClick[i][row].isSteinTile() && this.mainFieldClick[i][row].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row].isUsed() ) {
                        if ( i == 0 || i == 6 ) {
                            if ( row == 0 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                        }
                        else if ( i == 3 ) {
                            if ( row == 0 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                        }
                        if ( diff != 0 ) {
                            if (!( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row + diff].isUsed())) {
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
                    if ( this.mainFieldClick[i][row].isSteinTile() && this.mainFieldClick[i][row].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row].isUsed() ) {
                        if ( i == 1 || i == 5 ) {
                            if ( row == 1 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                            if (!( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() && this.mainFieldClick[i][row + diff].isUsed())) {
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
                        else if ( row == 2 || row == 4 ) {
                            if ( i == 2 || i == 4 ) {
                                diff = 1;
                                if ( row == 4 ) {
                                    diff = -1;
                                }
                            }
                            else if ( i == 3 ) {
                                diff = -1;
                                if ( row == 4 ) {
                                    diff = 1;
                                }
                            }
                            if ( diff != 0 ) {
                                if (!( this.mainFieldClick[i][row + diff].isSteinTile() && this.mainFieldClick[i][row + diff].isWhite() == this.model.isWhite() &&this.mainFieldClick[i][row + diff].isUsed())) {
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
                    if ( this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed() ) {
                        if ( i == 0 || i == 6 ) {
                            if ( column == 0 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                        }
                        else if ( i == 3 ) {
                            if ( column == 0 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                        }
                        if ( diff != 0 ) {
                            if (!( this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff][i].isUsed())) {
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
                    if ( this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed() ) {
                        if ( i == 1 || i == 5 ) {
                            if ( column == 1 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                            if (!( this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column + diff][i].isUsed())) {
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
                for ( int i = 0; i < 7; i++ ) {
                    diff = 0;
                    if ( this.mainFieldClick[column][i].isSteinTile() && this.mainFieldClick[column][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column][i].isUsed() ) {
                        if ( column == 3 ) {
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
                                if ( !( (this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + diff][i].isUsed()) && (this.mainFieldClick[column - diff][i].isSteinTile() && this.mainFieldClick[column - diff][i].isWhite() == this.model.isWhite() &&this.mainFieldClick[column - diff][i].isUsed()) ) ) {
                                    this.mainFieldClick[column][i].setUsed(false);
                                    this.c.send(MessageProtocol.SETUNUSED + " x:" + column + ",y:" + i);
                                }
                            }
                        }
                        else if ( column == 2 || column == 4 ) {
                            if ( i == 2 || i == 4 ) {
                                diff = 1;
                                if ( column == 4 ) {
                                    diff = -1;
                                }
                            }
                            else if ( i == 3 ) {
                                diff = -1;
                                if ( column == 4 ) {
                                    diff = 1;
                                }
                            }
                            if ( diff != 0 ) {
                                if (!( this.mainFieldClick[column + diff][i].isSteinTile() && this.mainFieldClick[column + diff][i].isWhite() == this.model.isWhite() && this.mainFieldClick[column + diff][i].isUsed())) {
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

    private void showRemove() {
        for ( Tile[] tiles : this.mainFieldClick ) {
            for ( Tile tile: tiles ) {
                if ( tile.isWhite() == !this.model.isWhite() && !tile.isUsed()) {
                    tile.setRemovable();
                }
            }
        }
    }

    private void setMoveStein( Tile currentTile ) {
        if ( currentTile == this.moveTile ) {
            this.moveTile = null;
            currentTile.setUntouched();
        }
        else {
            if ( currentTile.isSteinTile() ) {
                this.moveTile = currentTile;
                this.moveTile.setMoveable();
            }
        }
    }

    private void removeEnmSteinFromSide() {
        for ( SteinTile tile : this.enmFigClick ) {
            if ( !tile.isSet() ) {
                tile.setSet();
                break;
            }
        }
    }

    public void setEnmUsed( int x, int y ) {
        this.mainFieldClick[x][y].setUsed(true);
    }

    public void setEnmUnused( int x, int y ) {
        this.mainFieldClick[x][y].setUsed(false);
    }

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

    public void removeTile( int x, int y ) {
        this.mainFieldClick[x][y].setNormal();

        this.model.setEigZugFinished(false);
        this.model.setEnmZugFinished(true);
        if ( !this.model.isWhite() ) {
            this.setBlacksTurn();
        }
        else {
            this.setWhitsTurn();
        }
    }

    public void setEnmStein( int x, int y ){
        this.removeEnmSteinFromSide();

        this.mainFieldClick[x][y].setSteinTile(true, !this.model.isWhite());

        this.model.setEigZugFinished(false);
        this.model.setEnmZugFinished(true);
        if ( !this.model.isWhite() ) {
            this.setBlacksTurn();
        }
        else {
            this.setWhitsTurn();
        }
    }

    public void moveEnmStein( int startX, int startY, int toX, int toY ) {

        this.mainFieldClick[startX][startY].setNormal();
        this.mainFieldClick[toX][toY].setSteinTile(true, !this.model.isWhite());
        this.mainFieldClick[toX][toY].setUsed(false);


        this.model.setEigZugFinished(false);
        this.model.setEnmZugFinished(true);
        if ( !this.model.isWhite() ) {
            this.setBlacksTurn();
        }
        else {
            this.setWhitsTurn();
        }
    }


    private void reset() {
        try {
            this.model.reset();

            this.figClicked = false;
            this.toRemove = false;

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

                        if ( model.isEnmFound() ) {
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

                    if ( model.isEnmFound() ) {
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
