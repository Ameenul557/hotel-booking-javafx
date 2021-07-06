package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;

//Main class inherits from Application class required to build javafx applications
public class Main extends Application {
    static Connection con = null; //creating a static connection variable to db to maintain a single connection throughout the app

    static Stage stg; //creating a static Stage variable to maintain a single stage(window) throughout the app

    //override the start method of application class to start our application
    @Override
    public void start(Stage primaryStage) throws Exception{
        stg = primaryStage;
        Parent login = FXMLLoader.load(getClass().getResource("../fxml_files/login.fxml")); //loading the scene design from fxml file created by scene builder
        Scene loginScene = new Scene(login,800,550); //creating new scene and setting window size
        loginScene.getStylesheets().add(getClass().getResource("../css files/authScene.css").toExternalForm()); //linking css file
        stg.setResizable(false); //fixing window size
        stg.setTitle("Hotel Booking");
        stg.setScene(loginScene); //loading scene
        stg.show(); //displaying scene
    }

    public static void main(String[] args) {
        //connection to mysql is done with jdbc driver
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver"); //it finds and  returns jdbc driver class //may throw classNotFoundException

            con = DriverManager.getConnection("jdbc:mysql://localhost/hotelbookingapp","root", ""); //establishing connection to our local db
            System.out.print("Database is connected !");
        }
        catch(Exception e)
        {
            System.out.print("Not connect to DB - Error:"+e);
        }

        launch(args); //launches the app by invoking start method //similar to thread.start invoking run method

    }
}


