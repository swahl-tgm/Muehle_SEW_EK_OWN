package client.GUI;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class SteinTile extends StackPane
{
    private int x;
    private int y;
    private boolean isActivated;
    private boolean isSet;



    private Rectangle border = new Rectangle(35, 35);
    private Circle stein = new Circle(15);
    private Text text = new Text();
    private ClientModel callback;

    // Setter / Getter
    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
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



    public SteinTile ( int y, boolean own ) {
        this.y = y;
        this.isActivated = false;
        this.isSet = false;
        border.setStroke(Color.color(0.1,0.1,0.1,0.1));
        border.setFill(Color.WHITE);
        if ( own ) {
            stein.setFill(Color.WHITE);
        }
        else {
            stein.setFill(Color.BLACK);
        }
        stein.setStroke(Color.LIGHTGRAY);


        getChildren().addAll(border, stein);
    }

    public void setBlack() {
        stein.setFill(Color.BLACK);
    }

    public void setWhite() {
        stein.setFill(Color.WHITE);
    }
    public void activate() {
        if ( !this.isSet ) {
            this.isActivated = true;
            this.stein.setFill(Color.LIGHTGRAY);
        }
    }

    public void deactivate() {
        if (!this.isSet ) {
            this.isActivated = false;
            this.stein.setFill(Color.WHITE);
        }
    }

    public void setSet(){
        this.isSet = true;
        this.isActivated = false;
        this.stein.setFill(Color.DARKGRAY);
        this.stein.setStroke(Color.DARKGRAY);
        this.border.setFill(Color.DARKGRAY);
    }

}
