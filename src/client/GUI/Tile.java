package client.GUI;

import javafx.animation.FillTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javax.imageio.plugins.tiff.TIFFDirectory;

public class Tile extends StackPane
{
    private int x;
    private int y;



    private Rectangle border = new Rectangle(20, 20);
    private Text text = new Text();
    private ClientModel callback;

    // Setter / Getter

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

    public Rectangle getRecBorder() {
        return border;
    }

    public void setRecBorder(Rectangle border) {
        this.border = border;
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
        border.setStroke(Color.DARKGRAY);

        getChildren().addAll(border, text);
    }


}
