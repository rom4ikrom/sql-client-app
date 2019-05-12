package net.romanov.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SQLClient extends Application {
	
	//connection to database
	private Connection connection;
	
	//statement to execute SQL commands
	private Statement statement;
	
	//text area to enter SQL commands
	private TextArea taSQLCommand = new TextArea();
	
	//text area to display results from SQL commands
	private TextArea taSQLResult = new TextArea();
	
	//DBC info for database connection
	private TextField tfUsername = new TextField();
	private PasswordField pfPassword = new PasswordField();
	private ComboBox<String> cbURL = new ComboBox<>();
	private ComboBox<String> cbDriver = new ComboBox<>();
	
	private Button btnExecuteSQL = new Button("Execute SQL command");
	private Button btnClearSQLCommand = new Button("Clear");
	private Button btnConnectDB = new Button("Connect to DB");
	private Button btnClearSQLResult = new Button("Clear Result");
	private Label lblConnectionStatus = new Label("No Connection Now");
	

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		String mySQLStudentDB = "jdbc:mysql://localhost/studentinfo";
		
		cbURL.getItems().addAll(FXCollections.observableArrayList(
				mySQLStudentDB));
		
		cbURL.getSelectionModel().selectFirst();
		
		cbDriver.getItems().addAll(FXCollections.observableArrayList("com.mysql.cj.jdbc.Driver"));
		cbDriver.getSelectionModel().selectFirst();
		
		//create UI for connecting to the database
		GridPane gPane = new GridPane();
		
		//database infromation panel
		gPane.add(cbURL, 1, 1);
		gPane.add(cbDriver, 1, 2);
		
		gPane.add(tfUsername, 1, 3);
		gPane.add(pfPassword, 1, 4);
		
		cbURL.setPrefWidth(300);
		cbDriver.setPrefWidth(300);
		
		Label lblDBURL = new Label("Database URL");
		
		lblDBURL.setPrefWidth(150);
		
		gPane.add(lblDBURL, 0, 1);
		gPane.add(new Label ("JDBC Driver"), 0, 2);
		gPane.add(new Label ("Username"), 0, 3);
		gPane.add(new Label ("Password"), 0, 4);
		
		gPane.setVgap(10);
		gPane.setHgap(10);
		
		Label lblDBInfo = new Label("Enter Database Information");
		
		gPane.getChildren().addAll(lblDBInfo, lblConnectionStatus, btnConnectDB);
		
		GridPane.setConstraints(lblDBInfo, 0, 0, 2, 1);
		GridPane.setConstraints(lblConnectionStatus, 0, 5, 2, 1);
		GridPane.setConstraints(btnConnectDB, 0, 6, 2, 1);
		
		GridPane.setHalignment(lblConnectionStatus, HPos.CENTER);
		GridPane.setHalignment(btnConnectDB, HPos.CENTER);
		
		gPane.setPadding(new Insets(10, 10, 10, 10));
		
		//database sql enter panel
		
		Label lblSQLEnter = new Label("Enter an SQL Command");
		
		GridPane.setConstraints(lblSQLEnter, 2, 0, 1, 1);
		GridPane.setConstraints(taSQLCommand, 2, 1, 1, 5);
		
		HBox hBoxSQLCommand = new HBox(5);
		hBoxSQLCommand.getChildren().addAll(btnExecuteSQL, btnClearSQLCommand);
		hBoxSQLCommand.setAlignment(Pos.CENTER_RIGHT);
		
		GridPane.setConstraints(hBoxSQLCommand, 2, 6, 1, 1);
		
		gPane.getChildren().addAll(lblSQLEnter, taSQLCommand, hBoxSQLCommand);
		
		//database sql result
		
		Label lblSQLResult = new Label("SQL Execution Result");
		
		gPane.getChildren().addAll(lblSQLResult, taSQLResult, btnClearSQLResult);
		
		GridPane.setConstraints(lblSQLResult, 0, 7, 2, 1);
		GridPane.setConstraints(taSQLResult, 0, 8, 3, 1);
		GridPane.setConstraints(btnClearSQLResult, 0, 9, 2, 1);
		
		// create a scene and place it in the stage
		Scene scene = new Scene(gPane, 850, 600);
		primaryStage.setTitle("SQLClient");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		btnConnectDB.setOnAction(e -> connectToDB());
		btnExecuteSQL.setOnAction(e -> executeSQL());
		btnClearSQLCommand.setOnAction(e -> taSQLCommand.setText(null));
		btnClearSQLResult.setOnAction(e -> taSQLResult.setText(null));
		
	}
	
	//Connect to DB
	private void connectToDB() {
		//get database information from the user input
		String driver = cbDriver.getSelectionModel().getSelectedItem();
		String url = cbURL.getSelectionModel().getSelectedItem();
		String username = tfUsername.getText().trim();
		String password = pfPassword.getText().trim();
		
		//Connection to the database
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
			lblConnectionStatus.setText("Connected to " + url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//Execute SQL commands
	private void executeSQL() {
		if(connection == null) {
			taSQLResult.setText("Please connect to a database first");
			return;
		} else {
			String sqlCommands = taSQLCommand.getText().trim();
			String[] commands = sqlCommands.replace('\n', ' ').split(";");
			
			for(String aCommand : commands) {
				if(aCommand.trim().toUpperCase().startsWith("SELECT")) {
					processSQLSelect(aCommand);
				} else {
					processSQLNonSelect(aCommand);
				}
			}
		}
	}
	
	//Execute SQL SELECT commands
	private void processSQLSelect(String sqlCommand) {
		try {
			//get a new statement for the current connection
			statement = connection.createStatement();
			
			//execute a SELECT SQL command
			ResultSet resultSet = statement.executeQuery(sqlCommand);
			
			//find the number of columns in the result set
			int columnCount = resultSet.getMetaData().getColumnCount();
			String row = "";
			
			//display column names
			for(int i = 1; i <= columnCount; i++) {
				row += resultSet.getMetaData().getColumnName(i) + "\t";
			}
			
			taSQLResult.appendText(row + '\n');
			
			while (resultSet.next()) {
				//reset row to empty
				row = "";
				
				for(int i = 1; i <= columnCount; i++) {
					//a non-string column is converted to a string
					row += resultSet.getString(i) + "\t";
				}
				
				taSQLResult.appendText(row + '\n');
			}
			
		} catch(SQLException ex) {
			taSQLResult.setText(ex.toString());
		}
	}
	
	//Execute SQL DDL, and modification commands
	private void processSQLNonSelect(String sqlCommand) {
		try {
			//get a new statement for the current connection
			statement = connection.createStatement();
			
			//execute a non-SELECT SQL command
			statement.executeUpdate(sqlCommand);
			
			taSQLResult.setText("SQL command executed");
		} catch (SQLException ex) {
			taSQLResult.setText(ex.toString());
		}
	}
	
	public static void main(String[] args) {
		Application.launch(args);
		
	}

}
