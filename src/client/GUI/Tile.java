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


    private Circle border = new Circle(15);
    private Text text = new Text();
    private Text stein = new Text();
    private ClientModel callback;

    // Setter / Getter


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
        this.isKante = false;

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

    public void setSteinTile( boolean isSteinTile, boolean own ) {
        this.isSteinTile = isSteinTile;
        if ( isSteinTile ) {
            if ( own ) {
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
        }
    }

    public void unsetReadyToSet() {
        if ( this.isKante && !this.isSteinTile ) {
            border.setFill(Color.TRANSPARENT);
        }
    }




}
