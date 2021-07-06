package application;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


//Controller class performs operations and has control functions for the widgets
public class Controller {

    //these fields are used as id for the screen widgets in the fxml files
    @FXML
    private TextField userMail,password,confirmPass,phone,name,address,searchBox;

    @FXML
    private Label warning,nameLabel,mailLabel,phoneLabel,addressLabel,bookingLabel,listViewMiddleLabel;

    @FXML
    private ListView<Hotel> searchedHotelsList;

    @FXML
    private TableView<Booking> bookingTable;

    @FXML
    private TableColumn<Booking,Object> hnameColumn,checkinColumn,checkoutColumn,daysColumn,roomsColumn,amountColumn,statusColumn;

    @FXML
    private TableColumn<Booking,Booking> cancelButtonCell;

    static User activeUser=new User();
    static ObservableList<Hotel> activeHotelList = FXCollections.observableArrayList();
    static ObservableList<Booking> activeBookingList = FXCollections.observableArrayList();

    public void initialize() throws SQLException {
        Booking booking = new Booking();
        activeBookingList.clear();
        booking.getBookingDetails(activeUser.userId);
        setAccountScreenDetails();
        if(searchedHotelsList!=null){
            if (( activeHotelList==null)) {
                System.out.println("null");
            } else if(activeHotelList.isEmpty())
                System.out.println("empty");
            else {
                System.out.println(activeHotelList.get(0).hotelName);
            }
            searchedHotelsList.setFocusTraversable( false );
            searchedHotelsList.setCellFactory(e-> {
                try {
                    return new CustomListCell();
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                return null;
            }
            );
        }
        if(activeBookingList!=null && bookingTable!=null){
            bookingTable.setItems(activeBookingList);
            hnameColumn.setCellValueFactory(e->new ReadOnlyObjectWrapper<>(e.getValue().hotelName));
            checkinColumn.setCellValueFactory(e->new ReadOnlyObjectWrapper<>(e.getValue().checkInDate));
            checkoutColumn.setCellValueFactory(e->new ReadOnlyObjectWrapper<>(e.getValue().checkOutDate));
            checkinColumn.setCellValueFactory(e->new ReadOnlyObjectWrapper<>(e.getValue().checkInDate));
            daysColumn.setCellValueFactory(e->new ReadOnlyObjectWrapper<>(e.getValue().noOfDays));
            roomsColumn.setCellValueFactory(e-> new ReadOnlyObjectWrapper<>(e.getValue().noOfRoomsBooked));
            amountColumn.setCellValueFactory(e-> new ReadOnlyObjectWrapper<>(e.getValue().amount));
            statusColumn.setCellValueFactory(e-> new ReadOnlyObjectWrapper<>(e.getValue().bookingStatus));
            cancelButtonCell.setCellFactory(e-> new ButtonCell());
        }

    }

    //login controller assigned to the loginButton of the login page
    public void login() throws SQLException, IOException {
        int userId = User.login(userMail.getText(), password.getText()); //getText returns the text entered in the text fields in gui
        if (userId >= 0) {
            System.out.print(userId);
            activeUser.getUserDataFromDb(userId);
            setUserHomeScreen();

        } else
            warning.setText("invalid user credentials");
    }

    //signup controller assigned to signup button
    public void signUp() throws IOException, SQLException {
        User user = new User(); //creating new user
        //sign up details validation
        if (userMail.getText().isEmpty() || password.getText().isEmpty() || name.getText().isEmpty() || address.getText().isEmpty() || phone.getText().isEmpty()) {
            warning.setText("All fields are required"); //used to set label text //initially that's empty
        } else if (!password.getText().equals(confirmPass.getText())) {
            warning.setText("passwords don't match");
        } else if (!userMail.getText().contains("@") || !userMail.getText().contains(".")) {
            warning.setText("Enter a valid Email");
        } else if (password.getText().length() < 6) {
            warning.setText("password too short");
        } else if (phone.getText().length() < 10) {
            warning.setText("Enter a valid Phone number");
        }

        else {
            try {
                user.signUp(userMail.getText(), password.getText(), name.getText(), address.getText(), phone.getText());//calling sign up method


                warning.setText("Signed up successfully");
                setLoginScreen();//redirecting to login screen
            }
            catch (SQLIntegrityConstraintViolationException e){
                warning.setText("Email or phone already exists");
            }
            //clearing text fields
            userMail.clear();
            password.clear();
            name.clear();
            confirmPass.clear();
            address.clear();
            phone.clear();

        }
    }


    //these functions or assigned to the two buttons on the top of auth screen
    //go to login screen
    public void setLoginScreen() throws IOException {
        Parent login = FXMLLoader.load(getClass().getResource("../fxml_files/login.fxml"));
        Scene loginScene = new Scene(login, 800, 550);
        loginScene.getStylesheets().add(getClass().getResource("../css files/authScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setScene(loginScene);
        Main.stg.show();

    }

    //to go to sign up screen
    public void setSignUpScreen() throws IOException {
        //register
        Parent register = FXMLLoader.load(getClass().getResource("../fxml_files/register.fxml"));
        Scene registerScene = new Scene(register, 800, 550);
        registerScene.getStylesheets().add(getClass().getResource("../css files/authScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setScene(registerScene);
        Main.stg.show();
    }

    //to go to admin login screen


    public void setUserHomeScreen() throws IOException {
        Parent userHome = FXMLLoader.load(getClass().getResource("../fxml_files/userHome.fxml"));
        Scene userHomeScene = new Scene(userHome, 800, 550);
        userHomeScene.getStylesheets().add(getClass().getResource("../css files/homeScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setTitle("Hotel booking-"+activeUser.userName);
        Main.stg.setScene(userHomeScene);
        Main.stg.show();
    }

    public void setAccountDetailsScreen() throws IOException{
        Parent account = FXMLLoader.load(getClass().getResource("../fxml_files/accountDetails.fxml"));
        Scene accountScene = new Scene(account, 800, 550);
        accountScene.getStylesheets().add(getClass().getResource("../css files/homeScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setTitle("Hotel booking-"+activeUser.userName);

        Main.stg.setScene(accountScene);

        Main.stg.show();
    }

    void setAccountScreenDetails(){
        if(nameLabel!=null){
            nameLabel.setText(activeUser.userName);
            phoneLabel.setText(activeUser.userPhone);
            mailLabel.setText(activeUser.userMail);
            addressLabel.setText(activeUser.userAddress);
            bookingLabel.setText(String.valueOf(activeUser.bookingsDone));
        }

    }



    public void setSearchScreen() throws IOException {
        activeHotelList.clear();
        Parent account = FXMLLoader.load(getClass().getResource("../fxml_files/hotelsearch.fxml"));
        Scene accountScene = new Scene(account, 800, 550);
        accountScene.getStylesheets().add(getClass().getResource("../css files/homeScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setTitle("Hotel booking-"+activeUser.userName);

        Main.stg.setScene(accountScene);

        Main.stg.show();
    }

    public void searchHotels() throws SQLException {
        activeHotelList.clear();
       String location=searchBox.getText();
       Hotel hotel=new Hotel();
       hotel.getHotelsBasedOnLocation(location);
       if(activeHotelList.isEmpty()){
          listViewMiddleLabel.setText("No results found :(");
       }
       else {
           listViewMiddleLabel.setText("");
           searchedHotelsList.setItems(activeHotelList);

       }
    }

    static class CustomListCell extends ListCell<Hotel> {
        HBox hBox=new HBox();
        VBox vBox=new VBox();
        VBox vBox1=new VBox();
        VBox vBox2=new VBox();
        Image image = new Image(new FileInputStream("C:\\Users\\Ameenul\\IdeaProjects\\final_project\\assets\\images\\tajmahal.jpg"));
        ImageView imageView = new ImageView(image);
        Label label1=new Label();
        Label label2=new Label();
        Label label3=new Label();
        Label label4=new Label();
        Label label5=new Label();
        Label label6=new Label();
        Label label7=new Label();
        Label label8=new Label();
        Label label9=new Label();
        Label label10=new Label();
        Label label11=new Label();
        Label label12=new Label();
        Label label13=new Label();
        Label label14=new Label();
        TextField textField1=new TextField();
        TextField textField2 = new TextField();
        TextField textField3= new TextField();
        Button book = new Button("BOOK NOW");
        Separator separator = new Separator();
        Pane pane = new Pane();

        public CustomListCell() throws FileNotFoundException {
           super();

           vBox.getChildren().addAll(label1,label2,label3,label4,label5,label6,label7);
           hBox.getChildren().addAll(imageView,vBox);
           vBox1.setSpacing(5);
           vBox1.getChildren().addAll(label8,label9,label10,label11,label12,label13,label14,textField1,textField2,textField3,book,separator);
           vBox2.getChildren().addAll(hBox,vBox1,pane);
           VBox.setVgrow(pane, Priority.ALWAYS);


        }

        @Override
        protected void updateItem(Hotel item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item != null && !empty) { // <== test for null item and empty parameter
                label1.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-padding:5,5,5,5");
                label1.setText(item.hotelName);
                label2.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label2.setText(item.hotelStars + " star hotel");
                label3.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label3.setText("Rating: "+ item.hotelCusRating +"/5");
                label4.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label4.setText(item.hotelDescription);
                label5.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label5.setText("Address: "+item.hotelAddress);
                label6.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label6.setText("Phone: "+item.hotelPhone);
                label7.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label7.setText("Mail: "+item.hotelMail);
                label8.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-padding:5,5,5,5");
                label8.setText("Room details:");
                Room room = new Room();
                try {
                    room.getRoomDetails(item.hotelId);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                label9.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label9.setText("Available Rooms: "+item.availableRooms);
                label10.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label10.setText("Room types : "+room.noOfRooms);
                label11.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label11.setText("Accommodation: "+room.accommodation);
                label12.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label12.setText("Available facilities: "+room.availableFacilities);
                label13.setStyle("-fx-font-size:15; -fx-padding:3,3,3,3");
                label13.setText("Price per room per 24 hours: "+room.price);
                label14.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-padding:5,5,5,5");
                label14.setText("Book now!");
                textField1.setPadding(new Insets(5,5,5,5));
                textField2.setPadding(new Insets(5,5,5,5));
                textField2.setPadding(new Insets(5,5,5,5));
                textField1.setPromptText("Enter number of rooms");
                textField2.setPromptText("Enter check in date DD/MM/YYYY");
                textField3.setPromptText("Enter check out date DD/MM/YYYY");
                separator.setStyle("-fx-border-style: solid; -fx-padding: 0 -50 0 0; -fx-background-color:#000000");
                separator.setOrientation(Orientation.HORIZONTAL);
                book.setStyle("-fx-background-color:#ff0157; -fx-padding:5 5 5 5; -fx-font-color:white;");
                book.setOnAction(e->{
                    Date checkIn =new Date(),checkOut=new Date();
                    try{
                        if (Integer.parseInt(textField1.getText()) > item.availableRooms) {
                            textField1.setText("invalid entry");
                            return;
                        }
                    }
                    catch (Exception exception){
                        textField1.setText("invalid entry");
                    }
                        try {
                            checkIn=new SimpleDateFormat("dd/MM/yyyy").parse(textField2.getText());
                        } catch (ParseException parseException) {
                            textField2.setText("Invalid date");
                        }
                    try {
                        checkOut=new SimpleDateFormat("dd/MM/yyyy").parse(textField3.getText());
                    } catch (ParseException parseException) {
                        textField3.setText("Invalid date");
                        parseException.printStackTrace();
                    }
                    try {
                        if(checkIn.before(new SimpleDateFormat("dd/MM/yyyy").parse("25/12/2020")) || checkOut.before(new SimpleDateFormat("dd/MM/yyyy").parse("25/12/2020"))){
                            textField3.setText("Invalid date");
                            textField2.setText("Invalid date");
                        }
                    } catch (ParseException parseException) {
                        textField3.setText("Invalid date");
                        textField2.setText("Invalid date");
                        parseException.printStackTrace();
                    }
                    long noOfmillisec = (checkOut.getTime()-checkIn.getTime());
                    int noOfDays= (int) TimeUnit.MILLISECONDS.toDays(noOfmillisec);
                    if(noOfDays<0){
                        System.out.println(noOfDays);
                        textField3.setText("Invalid date");
                        textField2.setText("Invalid date");
                    }
                    else if(Integer.parseInt(textField1.getText()) < item.availableRooms){
                        try {
                            Booking.newBooking((int) (room.price*noOfDays*Integer.parseInt(textField1.getText())),Integer.parseInt(textField1.getText()),noOfDays,textField2.getText(),textField3.getText(), item.hotelName);
                            User.updateBooking(1);
                            activeUser.getUserDataFromDb(activeUser.userId);
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        book.setText("Booked");
                        book.setDisable(true);
                    }

                });


                try {
                    imageView.setFitHeight(250);
                    imageView.setFitWidth(250);
                    imageView.setImage(new Image(new FileInputStream("E:/subjects/DBMS/final project/assets/hotel images/"+item.hotelId+".png")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                setGraphic(vBox2);
            }
            else {
                setGraphic(null);
            }
        }
    }

    public void setBookings() throws IOException {
        Parent bookings = FXMLLoader.load(getClass().getResource("../fxml_files/bookings.fxml"));

        Scene bookingScene = new Scene(bookings, 800, 550);
        bookingScene.getStylesheets().add(getClass().getResource("../css files/homeScene.css").toExternalForm());
        Main.stg.setResizable(false);
        Main.stg.setTitle("Your booking-"+activeUser.userName);
        Main.stg.setScene(bookingScene);
        Main.stg.show();
    }

    static class ButtonCell extends TableCell<Booking,Booking> {
        Button cellButton;
        ButtonCell(){
            super();
            cellButton = new Button();
        }

        //Display button if the row is not empty
        @Override
        protected void updateItem(Booking item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if(!empty){
                Booking booking = getTableView().getItems().get(getIndex());
                cellButton.setStyle("-fx-background-color: #e96868");
                cellButton.setText("Cancel");
                cellButton.setOnAction(e->{
                    try {
                        Booking.cancelBooking(booking.bookingId);
                        booking.bookingStatus="cancelled";
                        activeBookingList.clear();
                        User.updateBooking(0);
                        booking.getBookingDetails(activeUser.userId);
                        activeUser.getUserDataFromDb(activeUser.userId);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                });
                if (booking.bookingStatus.equals("cancelled")) {
                        cellButton.setText("cancelled");
                        cellButton.setDisable(true);
                    }

                setGraphic(cellButton);

            } else {
                setGraphic(null);
            }
        }
    }

}


//user class
class User {
    public String userMail, userPass, userName, userAddress, userPhone;
    public int userId,bookingsDone;

    void signUp(String userMail, String userPass, String userName, String userAddress, String userPhone) throws SQLException {
        this.userPass = userPass;
        this.userMail = userMail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;

        addUser(this); //calling add user method
    }

    static int login(String userMail, String password) {
        //checking if the user already exist
        try {
            String SQL = "Select * from users";//sql query
            ResultSet rs = Main.con.createStatement().executeQuery(SQL); //ResultSet has a cursor that points to the first row of db
            while (rs.next()) { //rs.next() traverses table rows and returns false if the cursor is in the last row i.e no rows after that and true if there is a row after the current row pointed by rs cursor
               if( userMail.equals(rs.getString("UserMail")) && password.equals(rs.getString("Password"))){
                   return rs.getInt("UserId");
               }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
        return -1;
    }

    private void addUser(User user) throws SQLException {
        Main.con.createStatement().executeUpdate(
                "INSERT INTO users(UserName,Password,UserMail,UserPhone,address) " +
                        "values('" + user.userName + "','" + user.userPass + "','" + user.userMail + "','" + user.userPhone + "','" + user.userAddress + "')"
        );
    }

    void getUserDataFromDb(int userId) throws SQLException {
        ResultSet rs =Main.con.createStatement().executeQuery("SELECT * FROM users WHERE UserId=" + userId);
        rs.next();
        this.userId=rs.getInt("UserID");
        userMail=rs.getString("UserMail");
        userName=rs.getString("UserName");
        userPhone=rs.getString("UserPhone");
        userPass=rs.getString("Password");
        userAddress=rs.getString("address");
        bookingsDone=rs.getInt("No_of_bookings_done");
    }

    static void updateBooking(int mode) throws SQLException {
        ResultSet rs =Main.con.createStatement().executeQuery("SELECT * FROM users WHERE UserID=" + Controller.activeUser.userId);
        rs.next();
        int no_of_bookings_done=rs.getInt("No_of_bookings_done");
        if(mode==1){
            no_of_bookings_done++;
            String updateQuery = "UPDATE users SET No_of_bookings_done=" +no_of_bookings_done+ " where UserID="+Controller.activeUser.userId;
            Main.con.createStatement().executeUpdate(updateQuery);
        }
        else {
            no_of_bookings_done--;
            String updateQuery = "UPDATE users SET No_of_bookings_done=" +no_of_bookings_done+ " where UserId="+Controller.activeUser.userId;
            Main.con.createStatement().executeUpdate(updateQuery);
        }
    }
}

class Hotel{
    protected long hotelPinCode,hotelPhone;
    protected int hotelId,hotelCusRating,hotelStars,availableRooms;
    protected String hotelName,hotelMail,hotelCity,hotelState,hotelDescription,hotelAddress;

    Hotel() {}

    Hotel(long hotelPinCode,long hotelPhone,int hotelCusRating,int hotelStars,int availableRooms,int hotelId,String hotelName,String hotelMail,String hotelCity,String hotelState,String hotelDescription,String hotelAddress){
        this.hotelAddress=hotelAddress;
        this.hotelId=hotelId;
        this.hotelDescription=hotelDescription;
        this.hotelState=hotelState;
        this.hotelCity=hotelCity;
        this.hotelMail=hotelMail;
        this.hotelName=hotelName;
        this.hotelPhone=hotelPhone;
        this.hotelPinCode=hotelPinCode;
        this.availableRooms=availableRooms;
        this.hotelStars=hotelStars;
        this.hotelCusRating=hotelCusRating;
    }

    void getHotelsBasedOnLocation(String location) throws SQLException {

       ResultSet rs =Main.con.createStatement().executeQuery("SELECT * FROM hotels WHERE hcity='"+location+"'");
       while (rs.next()){
           hotelId=rs.getInt("hotelid");
           hotelCusRating=rs.getInt("customerrating");
           hotelStars=rs.getInt("stars");
           availableRooms=rs.getInt("availablerooms");
           hotelPinCode=rs.getInt("hpincode");
           hotelPhone=rs.getLong("hotelphone");
           hotelName=rs.getString("hotelname");
           hotelMail=rs.getString("hotelmail");
           hotelCity=rs.getString("hcity");
           hotelState=rs.getString("hstate");
           hotelDescription=rs.getString("description");
           hotelAddress=rs.getString("address");

           Controller.activeHotelList.add(new Hotel(hotelPinCode,hotelPhone,hotelCusRating,hotelStars,availableRooms,hotelId,hotelName,hotelMail,hotelCity,hotelState,hotelDescription,hotelAddress));
       }
    }

}

class Room extends Hotel{

    int accommodation;
    float price;
    String availableFacilities,noOfRooms;

    void getRoomDetails(int id) throws SQLException {

        ResultSet rs =Main.con.createStatement().executeQuery("SELECT * FROM rooms WHERE roomid="+id);
        while (rs.next()){
            accommodation=rs.getInt("accommodation");
            price=rs.getInt("priceper24hrs");
            availableFacilities=rs.getString("availablefacilities");
            noOfRooms=rs.getString("noofrooms");
        }
    }
}

class Booking{

    int amount,noOfRoomsBooked,noOfDays;
    String checkInDate,checkOutDate;
    String bookingStatus,hotelName;
    int bookingId;

    Booking(){}

    Booking(int bookingId,int amount,int noOfDays,int noOfRoomsBooked,String checkOutDate,String checkInDate,String bookingStatus,String hotelName){
        this.hotelName=hotelName;
        this.bookingStatus=bookingStatus;
        this.checkOutDate=checkOutDate;
        this.checkInDate=checkInDate;
        this.noOfRoomsBooked=noOfRoomsBooked;
        this.noOfDays=noOfDays;
        this.amount=amount;
        this.bookingId=bookingId;
    }


    void getBookingDetails(int id) throws SQLException {

        ResultSet rs =Main.con.createStatement().executeQuery("SELECT * FROM booking WHERE userid="+id);
        while (rs.next()){
            amount=rs.getInt("amount");
            noOfRoomsBooked=rs.getInt("rooms_booked");
            noOfDays=rs.getInt("noofdays");
            checkInDate=rs.getString("checkindate");
            checkOutDate=rs.getString("checkoutdate");
            bookingStatus=rs.getString("b_status");
            bookingId=rs.getInt("booking_id");
            hotelName=rs.getString("hotelname");
            Controller.activeBookingList.add(new Booking(bookingId,amount,noOfDays,noOfRoomsBooked,checkOutDate,checkInDate,bookingStatus,hotelName));
        }
    }

    static void newBooking(int amount, int noOfRoomsBooked, int noOfDays, String checkInDate, String checkOutDate, String hotelName) throws SQLException {

        Main.con.createStatement().executeUpdate("INSERT INTO booking(userid,hotelname,checkindate,checkoutdate,rooms_booked,amount,b_status,noofdays) " +
                "values('" + Controller.activeUser.userId + "','" + hotelName + "','" + checkInDate + "','" + checkOutDate + "','" + noOfRoomsBooked + "','"+amount+ "','" + "active" + "','" + noOfDays+"')");
        ResultSet rs=Main.con.createStatement().executeQuery("SELECT availablerooms from hotels where hotelname='"+hotelName+"'");
        rs.next();
        int availableRooms=rs.getInt("availablerooms");
        availableRooms-=noOfRoomsBooked;
        String updateQuery = "UPDATE hotels SET availablerooms= '"+availableRooms+"' where hotelname='"+hotelName+"'";
        Main.con.createStatement().executeUpdate(updateQuery);
    }

    static void cancelBooking(int bookingId) throws SQLException {
        String updateQuery = "UPDATE booking SET b_status= 'cancelled' where booking_id="+bookingId;
        Main.con.createStatement().executeUpdate(updateQuery);
    }
}