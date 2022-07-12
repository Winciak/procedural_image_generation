package sample;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;

public class Main extends Application {

    private final ImageView imageView = new ImageView();
    private final ImageView imageView2 = new ImageView();
    final Slider slider = new Slider(3, 35, 11);
    final Slider sliderThresh = new Slider(-15, 15, 5);
    final Slider sliderGreenYellow = new Slider(-0.05, 0.14, -0.04);

    private Image image;
    private Image image1;
    private PixelReader pixelReader;
    private PixelWriter pixelWriter;
    private WritableImage wImage;


    BorderPane border;

    private final int width = 600;
    private final int height = 600;
    int min = 0;
    int max = 255;

    private int firstThreshold = 128;


    @Override
    public void start(Stage stage) {
        stage.setTitle("BIOM_Project_Procedural_Generation");
        border = new BorderPane();



        Button buttonGenerate = new Button("Generate");
        buttonGenerate.setOnAction(GreatEvent);


        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(2.0f);
        slider.setMinorTickCount(0);
        slider.setValueChanging(true);
        slider.setBlockIncrement(2.0f);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(300);


        sliderThresh.setShowTickMarks(true);
        sliderThresh.setShowTickLabels(true);
        sliderThresh.setMajorTickUnit(1.0f);
        sliderThresh.setMinorTickCount(0);
        sliderThresh.setValueChanging(true);
        sliderThresh.setBlockIncrement(1.0f);
        sliderThresh.setSnapToTicks(true);
        sliderThresh.setPrefWidth(600);

        sliderGreenYellow.setShowTickMarks(true);
        sliderGreenYellow.setShowTickLabels(true);
        sliderGreenYellow.setMajorTickUnit(0.01f);
        sliderGreenYellow.setMinorTickCount(0);
        sliderGreenYellow.setValueChanging(true);
        sliderGreenYellow.setBlockIncrement(0.01f);
        sliderGreenYellow.setSnapToTicks(false);
        sliderGreenYellow.setPrefWidth(75);
        sliderGreenYellow.setPrefHeight(550);
        sliderGreenYellow.setOrientation(Orientation.VERTICAL);



        HBox box = new HBox();
        box.getChildren().addAll(imageView, imageView2);


        box.setAlignment(Pos.CENTER);
        border.setCenter(box);

        Label labelLeftTop = new Label("(More Yellow)");
        Label labelLeftBot = new Label("(More Green)");

        VBox vBoxLeft = new VBox();
        vBoxLeft.setAlignment(Pos.CENTER);
        vBoxLeft.setPadding(new Insets(0,0,0,5));
        vBoxLeft.setSpacing(2);
        vBoxLeft.getChildren().addAll(labelLeftTop, sliderGreenYellow, labelLeftBot);
        border.setLeft(vBoxLeft);

        Label labelBotLeft = new Label("(Less Water)");
        Label labelBotRight = new Label("(More Water)");

        HBox hBoxDown = new HBox();
        hBoxDown.setAlignment(Pos.CENTER);
        hBoxDown.setSpacing(2);
        hBoxDown.getChildren().addAll(labelBotLeft, sliderThresh, labelBotRight);

        Label labelBot = new Label("(Also affects green/yellow)");

        VBox vBoxDown = new VBox();
        vBoxDown.setAlignment(Pos.CENTER);
        vBoxDown.setSpacing(2);
        vBoxDown.getChildren().addAll(labelBot,hBoxDown);
        border.setBottom(vBoxDown);


        Label labelTopLeft = new Label("(More little islands)");
        Label labelTopRight = new Label("(Less, but bigger islands)     ");

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(2);
        hBox.getChildren().addAll(labelTopLeft, slider, labelTopRight, buttonGenerate);

        border.setTop(hBox);

        Scene scene = new Scene(border, 1800, 1000);

        stage.setScene(scene);
        stage.show();
    }

    private final EventHandler<ActionEvent> GreatEvent = actionEvent ->{

        generate();

        median(11);

        median((int) slider.getValue());

        median(3);

        median(11);

        kindaBinaryAlgorithm();

        actionEvent.consume();

    };

    private void generate(){


        wImage = new WritableImage(width,height);
        pixelWriter = wImage.getPixelWriter();

        for(int readY = 0; readY<height; readY++){
            for(int readX = 0; readX<width; readX++){

                Color color = generateColor();


                pixelWriter.setColor(readX,readY,color);
            }

        }
        image1 = wImage;
        image = image1;
        imageView.setImage(image1);


    }

    private Color generateColor() {

        int red = ThreadLocalRandom.current().nextInt(min, max + 1);
        int green = ThreadLocalRandom.current().nextInt(min, max + 1);
        int blue = ThreadLocalRandom.current().nextInt(min, max + 1);

        if(red<firstThreshold) red=0;
        if(green<firstThreshold) green=0;

        return  Color.rgb(red,green,blue);
    }



    private void median (int mask){

        initPixelReaders();

        for(int readY = 0; readY<image.getHeight(); readY++){
            for(int readX = 0; readX<image.getWidth(); readX++){

                Color color = pixelReader.getColor(readX,readY);
                if (readY >= mask / 2 && !(readY >= image.getHeight() - mask / 2) && readX >= mask / 2 && !(readX >= image.getWidth() - mask / 2)) {

                    color = Medianing(readX,readY,mask);

                }
                pixelWriter.setColor(readX,readY,color);
            }
        }
        image1 = wImage;
        image=image1;


        imageView2.setImage(image1);


    }

    private Color Medianing(int x, int y, int mask){

        int size = mask*mask;

        int[] allRed = new int[size];
        int[] allGreen = new int[size];
        int[] allBlue = new int[size];

        int k = 0;

        for (int i = y -(mask /2); i<= y +(mask /2); i++) {
            for (int j = x -(mask /2); j<= x +(mask /2); j++) {
                Color color = pixelReader.getColor(j,i);
                int red = (int) (color.getRed() >= 1.0 ? 255 : color.getRed() * 256.0);
                int green = (int) (color.getGreen() >= 1.0 ? 255 : color.getGreen() * 256.0);
                int blue =  (int) (color.getBlue() >= 1.0 ? 255 : color.getBlue() * 256.0);

                allRed[k] = red;
                allGreen[k] = green;
                allBlue[k] = blue;
                k++;
            }
        }

        Arrays.sort(allRed);
        Arrays.sort(allGreen);
        Arrays.sort(allBlue);


        return Color.rgb(allRed[size/2],allGreen[size/2],allBlue[size/2]);
    }



//--------------------------------------------------------------------------------------------------------------------------------------------------------


    private void kindaBinaryAlgorithm(){

        initPixelReaders();

        int threshold = 125;

        int threshDiff = (int) sliderThresh.getValue();
        double greenYellowDiff = sliderGreenYellow.getValue();

        threshold = threshold + threshDiff;

        int minHueYellow = 50;
        int maxHueYellow = 60;

        int minHueGreen = 100;
        int maxHueGreen = 140;

        int minHueBlue = 200;
        int maxHueBlue = 210;

        for(int readY=0;readY<image.getHeight();readY++){
            for(int readX=0; readX<image.getWidth();readX++){
                Color color = pixelReader.getColor(readX,readY);

                int red,green,blue;
                red = (int) floor(color.getRed() >= 1.0 ? 255 : color.getRed() * 256.0);
                green = (int) floor(color.getGreen() >= 1.0 ? 255 : color.getGreen() * 256.0);
                blue =  (int) floor(color.getBlue() >= 1.0 ? 255 : color.getBlue() * 256.0);

                int avg = red+green+blue;
                avg=avg/3;


                int hue;
                double sat = 1.0;
                double bright = ThreadLocalRandom.current().nextDouble(0.6,0.8);


                if(avg>threshold) {
                    if (avg>threshold*(1.05+greenYellowDiff)){
                        hue = ThreadLocalRandom.current().nextInt(minHueGreen, maxHueGreen + 1);
                    } else {
                        hue = ThreadLocalRandom.current().nextInt(minHueYellow, maxHueYellow + 1);
                        bright = ThreadLocalRandom.current().nextDouble(0.85,1.0);
                    }

                } else {
                    bright=1.0;
                    if(avg<threshold/2){
                        hue = ThreadLocalRandom.current().nextInt((int) (minHueBlue*0.98), maxHueBlue + 1);
                    }else
                    hue = ThreadLocalRandom.current().nextInt(minHueBlue, maxHueBlue + 1);

                }
                color = Color.hsb(hue,sat,bright);


                pixelWriter.setColor(readX,readY,color);
            }
        }
        image1 = wImage;
        image = image1;
        imageView2.setImage(image1);


    }


//------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void initPixelReaders(){
        pixelReader = image.getPixelReader();

        wImage = new WritableImage(
                (int)image.getWidth(),
                (int)image.getHeight());
        pixelWriter = wImage.getPixelWriter();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
