package aydin.firebasedemospring2024;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PrimaryController {
    @FXML
    private TextField ageTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextArea outputTextArea;

    @FXML
    private Button readButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button signInButton;

    @FXML
    private Button writeButton;

    @FXML
    private VBox welcomeScreen;

    @FXML
    private VBox dataAccessScreen;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button switchSecondaryViewButton;

    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;

    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    void initialize() {
        AccessDataView accessDataViewModel = new AccessDataView();
        nameTextField.textProperty().bindBidirectional(accessDataViewModel.personNameProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());

        // Initially show the welcome screen and hide the data access screen
        welcomeScreen.setVisible(true);
        dataAccessScreen.setVisible(false);
    }

    @FXML
    void signInButtonClicked(ActionEvent event) {
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        // Here you would typically validate against Firebase Authentication
        // For this example, we'll use a simple check
        if (email.equals("user@example.com") && password.equals("password")) {
            welcomeScreen.setVisible(false);
            dataAccessScreen.setVisible(true);
        } else {
            showAlert("Sign In Failed", "Invalid email or password.");
        }
    }

    @FXML
    void readButtonClicked(ActionEvent event) {
        readFirebase();
    }

    @FXML
    void registerButtonClicked(ActionEvent event) {
        registerUser();
    }

    @FXML
    void writeButtonClicked(ActionEvent event) {
        addData();
    }

    public boolean readFirebase() {
        key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  DemoApp.fstore.collection("Persons").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            if(documents.size() > 0) {
                System.out.println("Getting (reading) data from firebase database....");
                listOfUsers.clear();
                outputTextArea.clear();
                for (QueryDocumentSnapshot document : documents) {
                    outputTextArea.appendText(document.getData().get("Name") + " , Age: " +
                            document.getData().get("Age") + " \n ");
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    person = new Person(String.valueOf(document.getData().get("Name")),
                            Integer.parseInt(document.getData().get("Age").toString()),
                    Integer.parseInt(document.getData().get("Phone").toString()));
                    listOfUsers.add(person);
                }
            } else {
                System.out.println("No data");
                outputTextArea.setText("No data available.");
            }
            key = true;
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to read data from Firebase.");
        }
        return key;
    }

    public boolean registerUser() {
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Registration Error", "Email and password cannot be empty.");
            return false;
        }

        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);
            UserRecord userRecord = DemoApp.fauth.createUser(request);
            System.out.println("Successfully created new user with Firebase Uid: " + userRecord.getUid());
            showAlert("Registration Successful", "User registered successfully. You can now sign in.");
            return true;
        } catch (FirebaseAuthException ex) {
            System.out.println("Error creating a new user in Firebase: " + ex.getMessage());
            showAlert("Registration Error", "Failed to register user. " + ex.getMessage());
            return false;
        }
    }

    public void addData() {
        String name = nameTextField.getText();
        String ageText = ageTextField.getText();
        String phone = phoneTextField.getText();

        if (name.isEmpty() || ageText.isEmpty()) {
            showAlert("Input Error", "Name and age cannot be empty.");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);

            DocumentReference docRef = DemoApp.fstore.collection("Persons").document(UUID.randomUUID().toString());

            Map<String, Object> data = new HashMap<>();
            data.put("Name", name);
            data.put("Age", age);

            //asynchronously write data
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get(); // Wait for the write to complete
            showAlert("Success", "Data added successfully.");
            nameTextField.clear();
            ageTextField.clear();
            phoneTextField.clear();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Age must be a valid number.");
        } catch (InterruptedException | ExecutionException e) {
            showAlert("Error", "Failed to add data to Firebase.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}