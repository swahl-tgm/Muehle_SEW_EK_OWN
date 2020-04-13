package client.GUI;

import javafx.animation.FillTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.imageio.plugins.tiff.TIFFDirectory;

public class Tile extends StackPane
{
    private int x;
    private int y;
    private boolean isSteinTile;
    private boolean isKante;
    private boolean isWhite;
    private boolean isUsed;

    private boolean isGreen;


    private Circle border = new Circle(15);
    private Text text = new Text();
    private Text stein = new Text();
    private ClientModel callback;

    // Setter / Getter
    public boolean isUsed() {
        return isUsed;
    }

    public boolean isGreen() {
        return isGreen;
    }

    public void setGreen(boolean green) {
        isGreen = green;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isKante() {
        return isKante;
    }

    public void setKante(boolean kante) {
        isKante = kante;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public void setWhite(boolean white) {
        isWhite = white;
    }

    public boolean isSteinTile() {
        return isSteinTile;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ClientModel getCallback() {
        return callback;
    }

    public void setCallback(ClientModel callback) {
        this.callback = callback;
    }



    public Tile ( int x, int y ) {
        this.x = x;
        this.y = y;
        this.isSteinTile = false;
        this.isWhite = false;
        this.isKante = false;
        this.isUsed = false;
        this.isGreen = false;

        border.setStroke(Color.TRANSPARENT);
        border.setFill(Color.TRANSPARENT);
        if ( (this.x == 0 || this.x == 3 || this.x == 6) && (this.y == 0 || this.y == 6) ) {
            this.isKante = true;
        }
        if ( (this.x == 1 || this.x == 3 || this.x == 5) && (this.y == 1 || this.y == 5 )) {
            this.isKante = true;
        }
        if ( (this.x == 2 || this.x == 3 || this.x == 4) && (this.y == 2 || this.y == 4)) {
            this.isKante = true;
        }
        if ( (this.x == 0 || this.x == 1 || this.x == 2 || this.x == 4 || this.x == 5 || this.x == 6) && this.y == 3) {
            this.isKante = true;
        }
        getChildren().addAll(border, text, stein);
    }

    public void setSteinTile( boolean isSteinTile, boolean white ) {
        this.isSteinTile = isSteinTile;
        this.isWhite = white;
        if ( isSteinTile ) {
            if ( white ) {
                this.border.setFill(Color.WHITE);
            }
            else {
                this.border.setFill(Color.BLACK);
            }
            this.border.setStroke(Color.LIGHTGRAY);
        }
    }

    public void setReadyToSet() {
        if ( this.isKante && !this.isSteinTile) {
            border.setFill(Color.color((18.0/255), 1, 0, 0.5));
            this.isGreen = true;
        }
    }

    public void unsetReadyToSet() {
        if ( this.isKante && !this.isSteinTile ) {
            border.setFill(Color.TRANSPARENT);
            this.isGreen = false;
        }
    }

    public void setNormal() {
        this.isWhite = false;
        this.isUsed = false;
        this.isSteinTile = false;
        border.setStroke(Color.TRANSPARENT);
        border.setFill(Color.TRANSPARENT);
        this.isGreen = false;
    }

    public void setUntouched() {
        if ( isSteinTile ) {
            if ( isWhite ) {
                this.border.setFill(Color.WHITE);
            }
            else {
                this.border.setFill(Color.BLACK);
            }
            this.border.setStroke(Color.LIGHTGRAY);
            this.isGreen = false;
        }
    }

    public void setMoveable() {
        if ( isSteinTile ) {
            if ( this.isWhite ) {
                this.border.setFill(Color.LIGHTGRAY);
            }
            else {
                this.border.setFill(Color.DARKGRAY);
            }
            this.border.setStroke(Color.LIGHTGRAY);
            this.isGreen = false;

        }
    }

    public void setRemovable() {
        if ( isSteinTile ) {
            border.setFill(Color.color(1, 0, (18.0)/255, 0.5));
            this.border.setStroke(Color.LIGHTGRAY);
            this.isGreen = false;
        }
    }




}
