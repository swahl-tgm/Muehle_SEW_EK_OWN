package client.GUI;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    public Tile[][] createFieldContent( GridPane mainField ) {
        Tile[][] out = new Tile[7][7];

        FileInputStream inputstream = null;
        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
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
        for ( int y = 0; y < 9; y++) {
            SteinTile cache = new SteinTile(y, own);
            fig.add(cache, 0, y);
            out[y] = cache;
        }
        return out;
    }

    public void setAlreadyPlaced( boolean to ) {

    }
}