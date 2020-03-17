package client.GUI;


import javafx.scene.control.Label;

/**
 * Kapselt die Commandline damit nur zu gewollten Situationen der Value gesetzt werden kann
 */
public class CommandLineCapsule {
    private Label commandLine;
    private boolean editable;

    public CommandLineCapsule( Label c) {
        this.commandLine = c;
        this.editable = true;
    }

    /**
     * Setzt die Commandline auf nicht mehr Änderbar
     */
    public void stop() {
        this.editable = false;
    }

    /**
     * Setzt die Commandline auf Änderbar
     */
    public void start() {
        this.editable = true;
    }

    /**
     * Setzt den Text
     * @param text text
     * @param force wenn true, text kann immer gesetzt werden auch wenn durch {@link #stop()} gestoppt
     */
    public void setText( String text, boolean force ) {
        if ( editable || force ) {
            this.commandLine.setText(text);
        }
    }

}
