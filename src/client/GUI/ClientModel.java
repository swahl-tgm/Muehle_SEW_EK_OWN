package client.GUI;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import msg.MessageProtocol;

import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.net.ssl.HostnameVerifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.KeyRep;
import java.security.spec.ECField;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

public class ClientModel
{

    private ClientController callback;

    // Values
    private boolean enmFound;
    private boolean eigZugFinished;
    private boolean enmZugFinished;
    private boolean isWhite;
    private boolean colorSet;
    private boolean placingFinished;

    private int placedStones;
    private int ownRemovedStones;
    private boolean jumpingAllow;

    // Text
    private Text enmText;
    private StackPane enmTextBase;
    private Text eigText;
    private StackPane eigTextBase;

    public void setEnmText ( String text ) {
        this.enmText.setText( text + "'s Spielfiguren" );
    }

    public void setEigText ( String text ) {
        this.eigText.setText( text );
    }

    public boolean isEnmFound() {
        return enmFound;
    }

    public void setEnmFound(boolean enmFound) {
        this.enmFound = enmFound;
    }

    public boolean isEigZugFinished() {
        return eigZugFinished;
    }

    public void setEigZugFinished(boolean eigZugFinished) {
        this.eigZugFinished = eigZugFinished;
    }

    public boolean isEnmZugFinished() {
        return enmZugFinished;
    }

    public void setEnmZugFinished(boolean enmZugFinished) {
        this.enmZugFinished = enmZugFinished;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }

    public boolean isColorSet() {
        return colorSet;
    }

    public void setColorSet(boolean colorSet) {
        this.colorSet = colorSet;
    }

    public boolean isPlacingFinished() {
        return placingFinished;
    }

    public void setPlacingFinished(boolean placingFinished) {
        this.placingFinished = placingFinished;
    }

    public boolean isJumpingAllow() {
        return jumpingAllow;
    }

    public void setJumpingAllow(boolean jumpingAllow) {
        this.jumpingAllow = jumpingAllow;
    }

    public void stonePlaced(){
        this.placedStones++;
        if ( placedStones == 9 ) {
            this.placingFinished = true;
        }
    }

    public boolean ownStoneRemoved() {
        this.ownRemovedStones++;
        if ( this.ownRemovedStones == 6 ) {
            this.jumpingAllow = true;
            return false;
        }
        else return this.ownRemovedStones == 7;
    }

    public ClientModel( ClientController callback) {
        // Value
        this.enmFound = false;
        this.isWhite = false;
        this.colorSet = false;
        this.placingFinished = false;
        this.enmZugFinished = false;
        this.eigZugFinished = false;
        this.placedStones = 0;
        this.ownRemovedStones = 0;
        this.jumpingAllow = false;
        this.callback = callback;

        // Text
        enmText = new Text("Gegner's Spielfiguren");
        enmTextBase = new StackPane();
        enmTextBase.getChildren().add(enmText);
        eigText = new Text("Eigene Spielfiguren");
        eigTextBase = new StackPane();
        eigTextBase.getChildren().add(eigText);
    }

    public void reset() {
        // Value
        this.enmFound = false;
        this.isWhite = false;
        this.colorSet = false;
        this.placingFinished = false;
        this.enmZugFinished = false;
        this.eigZugFinished = false;
        this.placedStones = 0;
        this.ownRemovedStones = 0;
        this.jumpingAllow = false;

        enmText = new Text("Gegner's Spielfiguren");
        enmTextBase = new StackPane();
        enmTextBase.getChildren().add(enmText);
        eigText = new Text("Eigene Spielfiguren");
        eigTextBase = new StackPane();
        eigTextBase.getChildren().add(eigText);
    }

    public Tile[][] createFieldContent( GridPane mainField ) {
        Tile[][] out = new Tile[7][7];

        FileInputStream inputstream = null;
        String userDirectory = new File("").getAbsolutePath();
        try {
            inputstream = new FileInputStream(userDirectory + "\\src\\client\\GUI\\img\\Spielfeld.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Image img = new Image(inputstream);
        BackgroundImage background = new BackgroundImage(img,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(1.0, 1.0, true, true, false, false));
        mainField.setBackground(new Background(background));
        //Setting the image view

        for( int x = 0; x < 7; x ++ ) {
            for ( int y = 0; y < 7; y++) {
                Tile cache = new Tile(x, y);
                out[x][y] = cache;
                mainField.add(cache, x, y);
            }
        }

        return out;
    }

    public SteinTile[] createEigFigContent( GridPane eigFig ) {
        return createFigContent(eigFig, true);
    }

    public SteinTile[] createEnmFigContent( GridPane enmFig ) {
        return createFigContent(enmFig, false);
    }

    public SteinTile[] createFigContent( GridPane fig, boolean own ) {
        SteinTile[] out = new SteinTile[9];


        if ( own ) {
            fig.add(eigTextBase,0,0);

        }
        else {
            fig.add(enmTextBase,0,0);

        }

        GridPane toAdd = new GridPane();
        for ( int y = 0; y < 9; y++) {
            SteinTile cache = new SteinTile(y, own);
            toAdd.add(cache, 0, y);
            out[y] = cache;
        }
        toAdd.setAlignment(Pos.CENTER);
        fig.add(toAdd,0,1);
        return out;
    }

    public boolean checkIfPlayerCanMove( Tile[][] mainField ) {
        int diff;
        for ( int x = 0; x < mainField.length; x++ ) {
            for ( int y = 0; y < mainField[x].length; y++ ) {
                diff = 0;
                if ( mainField[x][y].isKante() && mainField[x][y].isWhite() == this.isWhite && mainField[x][y].isSteinTile() ) {
                    if ( x == 0 || x == 6 ) {
                        if ( y == 0 || y == 6 ) {
                            if ( x == 0 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                            if ( !mainField[x + diff][y].isSteinTile() ) {
                                return true;
                            }
                            if ( y == 0 ) {
                                diff = 3;
                            }
                            else {
                                diff = -3;
                            }
                            if ( !mainField[x][y + diff].isSteinTile() ) {
                                return true;
                            }
                        }
                        else if ( y == 3 ) {
                            if ( x == 0 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                            if ( !mainField[x + diff][y].isSteinTile() ) {
                                return true;
                            }
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 3;
                                }
                                else {
                                    diff = -3;
                                }
                                if ( !mainField[x][y + diff].isSteinTile() ) {
                                    return true;
                                }
                            }
                        }
                    }
                    else if ( x == 1 || x == 5 ) {
                        if ( y == 1 || y == 5 ) {
                            if ( x == 1 ) {
                                diff = 2;
                            }
                            else {
                                diff = -2;
                            }
                            if ( !mainField[x + diff][y].isSteinTile() ) {
                                return true;
                            }
                            if ( y == 1 ) {
                                diff = 2;
                            }
                            else {
                                diff = -2;
                            }
                            if ( !mainField[x][y + diff].isSteinTile() ) {
                                return true;
                            }
                        }
                        else if ( y == 3 ) {
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 1;
                                }
                                else {
                                    diff = -1;
                                }
                                if ( !mainField[x + diff ][y].isSteinTile() ) {
                                    return true;
                                }
                            }
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 2;
                                }
                                else {
                                    diff = -2;
                                }
                                if ( !mainField[x][y + diff].isSteinTile() ) {
                                    return true;
                                }
                            }
                        }
                    }
                    else if ( x == 2 || x == 4) {
                        if ( y == 2 || y == 4 ) {
                            if ( x == 2 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                            if ( !mainField[x + diff][y].isSteinTile() ) {
                                return true;
                            }
                            if ( y == 2 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                            if ( !mainField[x][y + diff].isSteinTile() ) {
                                return true;
                            }
                        }
                        else if ( y == 3 ) {
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = -1;
                                }
                                else {
                                    diff = 1;
                                }
                                if ( !mainField[x][y + diff].isSteinTile() ) {
                                    return true;
                                }
                            }
                            if ( x == 2 ) {
                                diff = -1;
                            }
                            else {
                                diff = 1;
                            }
                            if ( !mainField[x + diff][y].isSteinTile() ) {
                                return true;
                            }
                        }
                    }
                    else if ( x == 3 ) {
                        if ( y == 0 || y == 6 ) {
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 3;
                                }
                                else {
                                    diff = -3;
                                }
                                if ( !mainField[x + diff ][y].isSteinTile() ) {
                                    return true;
                                }
                            }
                            if ( y == 0 ) {
                                diff = 1;
                            }
                            else {
                                diff = -1;
                            }
                            if ( !mainField[x][y + diff].isSteinTile() ) {
                                return true;
                            }
                        }
                        else if ( y == 1 || y == 5 ) {
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 2;
                                }
                                else {
                                    diff = -2;
                                }
                                if ( !mainField[x + diff ][y].isSteinTile() ) {
                                    return true;
                                }
                            }
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 1;
                                }
                                else {
                                    diff = -1;
                                }
                                if ( !mainField[x][y + diff].isSteinTile() ) {
                                    return true;
                                }
                            }
                        }
                        else if ( y == 2 || y == 4 ) {
                            for ( int i = 0; i < 2; i++ ) {
                                if ( i == 0 ) {
                                    diff = 2;
                                }
                                else {
                                    diff = -2;
                                }
                                if ( !mainField[x + diff ][y].isSteinTile() ) {
                                    return true;
                                }
                            }
                            if ( y == 2 ) {
                                diff = -1;
                            }
                            else {
                                diff = 1;
                            }
                            if ( !mainField[x][y + diff].isSteinTile() ) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean checkForRemovableTiles( Tile[][] mainField ) {
        for ( Tile[] tiles : mainField ) {
            for ( Tile tile : tiles ) {
                if ( tile.isSteinTile() && tile.isWhite() != this.isWhite && !tile.isUsed() ) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkForMuehle( Tile[][] mainField ) {
        boolean lookForWhite = this.isWhite;
        int count = 0;
        int usedCount = 0;
        Tile[] toSet = new Tile[3];
        int toSetInd = 0;

        for ( int y = 0; y < mainField.length; y++ ) {
            count = 0;
            usedCount = 0;
            toSetInd = 0;
            for ( int x = 0; x < mainField[y].length; x++ ) {
                if ( mainField[x][y].isKante() && mainField[x][y].isSteinTile() ) {
                    if ( mainField[x][y].isWhite() == lookForWhite)  {
                        count++;
                        toSet[toSetInd] = mainField[x][y];
                        toSetInd++;

                        if ( mainField[x][y].isUsed() ) {
                            usedCount++;
                        }

                        if ( count == 3 && usedCount <= 2 ) {
                            // clear figs
                            for (Tile tile : toSet) {
                                this.callback.sendEnm(MessageProtocol.SETUSED + " x:"+tile.getX()+", y:"+tile.getY());
                                tile.setUsed(true);
                            }
                            return true;
                        }
                    }
                    else {
                        count = 0;
                        usedCount = 0;
                    }
                }
                else if ( mainField[x][y].isKante() && !mainField[x][y].isSteinTile() ) {
                    count = 0;
                    usedCount = 0;
                }
            }
        }
        for ( int x = 0; x < mainField.length; x++ ) {
            count = 0;
            usedCount = 0;
            toSetInd = 0;
            for ( int y = 0; y < mainField[x].length; y++ ) {
                if ( mainField[x][y].isKante() && mainField[x][y].isSteinTile() ) {
                    if ( mainField[x][y].isWhite() == lookForWhite ) {
                        count++;
                        toSet[toSetInd] = mainField[x][y];
                        toSetInd++;
                        if ( mainField[x][y].isUsed() ) {
                            usedCount++;
                        }

                        if ( count == 3 && usedCount <= 2 ) {
                            // clear figs
                            for (Tile tile : toSet) {
                                this.callback.sendEnm(MessageProtocol.SETUSED + " x:"+tile.getX()+", y:"+tile.getY());
                                tile.setUsed(true);
                            }
                            return true;
                        }
                    }
                    else {
                        count = 0;
                        usedCount = 0;
                    }
                }
                else if ( mainField[x][y].isKante() && !mainField[x][y].isSteinTile() ) {
                    count = 0;
                    usedCount = 0;
                }
            }
        }

        return false;
    }

}