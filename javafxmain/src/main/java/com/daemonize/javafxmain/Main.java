package com.daemonize.javafxmain;

import com.daemonize.daemonengine.utils.DaemonUtils;
import com.daemonize.game.Game;

import com.daemonize.game.images.Image;
import com.daemonize.game.images.imageloader.ImageLoader;
import com.daemonize.game.renderer.Renderer2D;

import java.io.IOException;

import javafx.application.Application;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {

    private Game game;

    @Override
    public void start(Stage primaryStage) {

        ///////////////////////////////////////////////////////////////////////////////////////////
        //                                GAME INITIALIZATION                                    //
        ///////////////////////////////////////////////////////////////////////////////////////////


        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        int borderX = (int) primaryScreenBounds.getWidth() / 2 > 800 ? (int) primaryScreenBounds.getWidth() / 2 : 800;
        //int borderY = 200;

        int rows = 6;
        int columns = 9;

        int gridWidth = (borderX * 70) / 100;

        int width = gridWidth/columns;
        int height = width; //160

        int borderY = (rows + 2) * height;

        Canvas canvas = new Canvas(borderX, borderY);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Renderer2D renderer = new JavaFXRenderer(gc, borderX, borderY);
        ImageLoader imageLoader = new JavaFxImageLoader("");

        game = new Game(renderer, imageLoader, borderX, borderY, rows, columns,50,50);

        Group root = new Group(canvas);
        primaryStage.setTitle("Tower Defense");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        //primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> game.onTouch((float) event.getSceneX(), (float) event.getSceneY()));

//            scene.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
//                if (game.isPaused())
//                    game.cont();
//            });
//
//            scene.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
//                if (!game.isPaused())
//                    game.pause();
//            });

        if(!game.isRunning())
            game.run();

    }

    @Override
    public void stop() throws Exception {
        game.stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

