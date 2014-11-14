package sample;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
//import static javafx.application.Application.launch;

import static javafx.application.Application.launch;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sample.Library;

public class Client3 extends Application {

    private static int count = 0;
    private ArrayList<Library> lib = new ArrayList<Library>();
    private Client client;

    private void init(final Stage primaryStage) throws RemoteException, NotBoundException {

        System.out.println("Starting...");

        client = new Client();
        client.lib(client);


        client.ReadXL();

        final List data1 = client.Print();
        final Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 400, 400));

        final ToolBar toolbar = new ToolBar();

        final Button Adding;
        Button Removing;
        final Button Output;
        Button Searching;
        Button Exit;
        toolbar.getItems().add(Adding = new Button("ДОБАВЛЕНИЕ"));
        toolbar.getItems().add(Removing = new Button("УДАЛЕНИЕ"));
        toolbar.getItems().add(Output = new Button("ВЫВОД"));
        toolbar.getItems().add(Searching = new Button("НАЙТИ"));
        toolbar.getItems().add(Exit = new Button("ВЫЙТИ"));
        root.getChildren().add(toolbar);
        toolbar.setMinSize(500, 40);

//adding
        Adding.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.AQUA);
                rect.getStyleClass().add("my-rect");

                root.getChildren().addAll(rect);

                final TextField numberText = new TextField();
                final TextField nameText = new TextField();
                final TextField dateText = new TextField();
                final TextField authorText = new TextField();

                Label label = new Label("Номер");
                label.setLayoutX(170);
                label.setLayoutY(40);
                root.getChildren().add(label);

                Label label1 = new Label("Название книги");
                label1.setLayoutX(155);
                label1.setLayoutY(80);
                root.getChildren().add(label1);

                Label label2 = new Label("Дата выдачи");
                label2.setLayoutX(160);
                label2.setLayoutY(120);
                root.getChildren().add(label2);

                Label label3 = new Label("Автор");
                label3.setLayoutX(175);
                label3.setLayoutY(160);
                root.getChildren().add(label3);

                final Button ok = new Button("OK");
                root.getChildren().add(numberText);
                root.getChildren().add(authorText);
                root.getChildren().add(nameText);
                root.getChildren().add(dateText);
                root.getChildren().add(ok);

                numberText.setMaxSize(140, 20);
                numberText.setLayoutX(125);
                numberText.setLayoutY(60);

                authorText.setMaxSize(140, 20);
                authorText.setLayoutX(125);
                authorText.setLayoutY(180);

                nameText.setMaxSize(140, 20);
                nameText.setLayoutX(125);
                nameText.setLayoutY(100);

                dateText.setMaxSize(140, 20);
                dateText.setLayoutX(125);
                dateText.setLayoutY(140);

                ok.setLayoutX(175);
                ok.setLayoutY(210);

                ok.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {

                        String number1 = numberText.getText();
                        String date1 = dateText.getText();
                        String name1 = nameText.getText();
                        String author1 = authorText.getText();
                        int i = 0;
                        Library book = new Library(i, Long.parseLong(number1), name1, Long.parseLong(date1), author1);
                        i++;
                        client.AddBook(book);
                        System.out.println(number1 + ' ' + date1 + ' ' + name1 + ' ' + author1);
                        try {
                            client.regAll();
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }

                    }
                });

                System.out.println("Added!");

            }
        });


//searching
        Searching.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.GREEN);
                rect.getStyleClass().add("my-rect");

                root.getChildren().addAll(rect);

                Button Number_s;
                Button Author_s;
                Button Date_s;
                Button Name_s;
                final TextField text = new TextField();
                root.getChildren().add(Number_s = new Button("Номер"));
                root.getChildren().add(Author_s = new Button("Автор"));
                root.getChildren().add(Date_s = new Button("Дата"));
                root.getChildren().add(Name_s = new Button("Название"));
                root.getChildren().add(text);
                Number_s.setLayoutX(50);
                Number_s.setLayoutY(90);
                Author_s.setLayoutX(130);
                Author_s.setLayoutY(90);
                Date_s.setLayoutX(207);
                Date_s.setLayoutY(90);
                Name_s.setLayoutX(275);
                Name_s.setLayoutY(90);
                text.setMinSize(250, 30);
                text.setLayoutX(80);
                text.setLayoutY(50);

                Number_s.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        count = 1;
                        String field = text.getText();
                        ArrayList<Library> lib = new ArrayList<Library>();
                        try {
                            lib = client.Searching(field, count);
                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(lib);
                        if (lib != null) {
                            ObservableList<Library> data = FXCollections.observableArrayList(lib);

                            TableColumn numberCol = new TableColumn();
                            numberCol.setText("Номер");
                            numberCol.setMinWidth(104);
                            numberCol.setCellValueFactory(new PropertyValueFactory("number"));
                            TableColumn nameCol = new TableColumn();
                            nameCol.setText("Название");
                            nameCol.setMinWidth(104);
                            nameCol.setCellValueFactory(new PropertyValueFactory("name"));
                            TableColumn dateColl = new TableColumn();
                            dateColl.setMinWidth(104);
                            dateColl.setText("Дата");
                            dateColl.setCellValueFactory(new PropertyValueFactory("date"));
                            TableColumn authorCol = new TableColumn();
                            authorCol.setText("Автор");
                            authorCol.setMinWidth(104);
                            authorCol.setCellValueFactory(new PropertyValueFactory("author"));

                            TableView tableView = new TableView();
                            tableView.setLayoutX(0);
                            tableView.setLayoutY(150);
                            tableView.setItems(data);
                            tableView.getColumns().addAll(numberCol, nameCol, dateColl, authorCol);
                            root.getChildren().add(tableView);
                            lib = null;
                        }

                    }
                });
                Name_s.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {

                        count = 2;
                        String field = text.getText();
                        ArrayList<Library> lib = new ArrayList<Library>();

                        try {
                            lib = client.Searching(field, count);
                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (lib != null) {
                            final ObservableList<Library> data = FXCollections.observableArrayList(lib);
                            TableColumn numberCol = new TableColumn();
                            numberCol.setText("Номер");
                            numberCol.setMinWidth(104);
                            numberCol.setCellValueFactory(new PropertyValueFactory("number"));
                            TableColumn nameCol = new TableColumn();
                            nameCol.setText("Название");
                            nameCol.setMinWidth(104);
                            nameCol.setCellValueFactory(new PropertyValueFactory("name"));
                            TableColumn dateColl = new TableColumn();
                            dateColl.setMinWidth(104);
                            dateColl.setText("Дата");
                            dateColl.setCellValueFactory(new PropertyValueFactory("date"));
                            TableColumn authorCol = new TableColumn();
                            authorCol.setText("Автор");
                            authorCol.setMinWidth(104);
                            authorCol.setCellValueFactory(new PropertyValueFactory("author"));

                            TableView tableView = new TableView();
                            tableView.setLayoutX(0);
                            tableView.setLayoutY(150);
                            tableView.setItems(data);
                            tableView.getColumns().addAll(numberCol, nameCol, dateColl, authorCol);
                            root.getChildren().add(tableView);
                            lib = null;
                        }

                    }
                });

                Date_s.setOnAction(new EventHandler<ActionEvent>() {

                    public void handle(ActionEvent event) {
                        try {
                            count = 3;
                            String field = text.getText();
                            ArrayList<Library> lib = new ArrayList<Library>();

                            lib = client.Searching(field, count);
                            if (lib != null) {
                                final ObservableList<Library> data = FXCollections.observableArrayList(lib);
                                TableColumn numberCol = new TableColumn();
                                numberCol.setText("Номер");
                                numberCol.setMinWidth(104);
                                numberCol.setCellValueFactory(new PropertyValueFactory("number"));
                                TableColumn nameCol = new TableColumn();
                                nameCol.setText("Название");
                                nameCol.setMinWidth(104);
                                nameCol.setCellValueFactory(new PropertyValueFactory("name"));
                                TableColumn dateColl = new TableColumn();
                                dateColl.setMinWidth(104);
                                dateColl.setText("Дата");
                                dateColl.setCellValueFactory(new PropertyValueFactory("date"));
                                TableColumn authorCol = new TableColumn();
                                authorCol.setText("Автор");
                                authorCol.setMinWidth(104);
                                authorCol.setCellValueFactory(new PropertyValueFactory("author"));

                                TableView tableView = new TableView();
                                tableView.setLayoutX(0);
                                tableView.setLayoutY(150);
                                tableView.setItems(data);
                                tableView.getColumns().addAll(numberCol, nameCol, dateColl, authorCol);
                                root.getChildren().add(tableView);
                            }

                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
                Author_s.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        try {
                            count = 4;
                            String field = text.getText();
                            ArrayList<Library> lib = new ArrayList<Library>();

                            lib = client.Searching(field, count);
                            if (lib != null) {
                                final ObservableList<Library> data = FXCollections.observableArrayList(lib);
                                TableColumn numberCol = new TableColumn();
                                numberCol.setText("Номер");
                                numberCol.setMinWidth(104);
                                numberCol.setCellValueFactory(new PropertyValueFactory("number"));
                                TableColumn nameCol = new TableColumn();
                                nameCol.setText("Название");
                                nameCol.setMinWidth(104);
                                nameCol.setCellValueFactory(new PropertyValueFactory("name"));
                                TableColumn dateColl = new TableColumn();
                                dateColl.setMinWidth(104);
                                dateColl.setText("Дата");
                                dateColl.setCellValueFactory(new PropertyValueFactory("date"));
                                TableColumn authorCol = new TableColumn();
                                authorCol.setText("Автор");
                                authorCol.setMinWidth(104);
                                authorCol.setCellValueFactory(new PropertyValueFactory("author"));

                                TableView tableView = new TableView();
                                tableView.setLayoutX(0);
                                tableView.setLayoutY(150);
                                tableView.setItems(data);
                                tableView.getColumns().addAll(numberCol, nameCol, dateColl, authorCol);
                                root.getChildren().add(tableView);
                            }

                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

            }
        });

        Removing.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.YELLOW);
                rect.getStyleClass().add("my-rect");

                root.getChildren().addAll(rect);

                Label label = new Label("Введите номер удаляемого элемента");
                label.setLayoutX(100);
                label.setLayoutY(50);
                root.getChildren().add(label);

                final TextField text = new TextField();
                final Button ok = new Button("OK");
                root.getChildren().add(text);
                root.getChildren().add(ok);

                text.setMinSize(250, 30);
                text.setLayoutX(70);
                text.setLayoutY(70);
                ok.setLayoutX(330);
                ok.setLayoutY(70);

                ok.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {

                        int num = Integer.parseInt(text.getText());

                        try {
                            if (client.DelBook(num)) {
                                System.out.println("Deleted!");
                                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                                rect.setLayoutX(0);
                                rect.setLayoutY(100);
                                rect.setFill(Color.YELLOW);
                                rect.getStyleClass().add("my-rect");
                                root.getChildren().addAll(rect);
                                Label labe2 = new Label("Удален");
                                labe2.setLayoutX(170);
                                labe2.setLayoutY(100);

                                root.getChildren().add(labe2);
                            } else {
                                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                                rect.setLayoutX(0);
                                rect.setLayoutY(100);
                                rect.setFill(Color.YELLOW);
                                rect.getStyleClass().add("my-rect");
                                root.getChildren().addAll(rect);
                                Label labe2 = new Label("Не удален");

                                labe2.setLayoutX(165);
                                labe2.setLayoutY(100);
                                root.getChildren().add(labe2);
                            }
                        } catch (RemoteException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            client.regAll();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                });
            }

        });

//print
        Output.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.RED);
                rect.getStyleClass().add("my-rect");

                root.getChildren().addAll(rect);

                try {
                    client.update();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                TableColumn numberCol = new TableColumn();
                numberCol.setText("Номер");
                numberCol.setMinWidth(104);
                numberCol.setCellValueFactory(new PropertyValueFactory("number"));
                TableColumn nameCol = new TableColumn();
                nameCol.setText("Название");
                nameCol.setMinWidth(104);
                nameCol.setCellValueFactory(new PropertyValueFactory("name"));
                TableColumn dateColl = new TableColumn();
                dateColl.setText("Дата");
                dateColl.setMinWidth(104);
                dateColl.setCellValueFactory(new PropertyValueFactory("date"));
                TableColumn authorCol = new TableColumn();
                authorCol.setText("Автор");
                authorCol.setMinWidth(104);
                authorCol.setCellValueFactory(new PropertyValueFactory("author"));

                final TableView tableView = new TableView();
                tableView.setLayoutX(0);
                tableView.setLayoutY(50);
                tableView.setItems(client.data);
                ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
                Runnable pinger = new Runnable() {
                    public void run() {

                        try {
                            client.update();
                            tableView.setItems(client.print());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ses.scheduleAtFixedRate(pinger, 1, 1, TimeUnit.SECONDS);
                tableView.getColumns().addAll(numberCol, nameCol, dateColl, authorCol);
                root.getChildren().add(tableView);
            }
        });

//exit
        Exit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {

                try {
                    client.unregister(client);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                System.out.println("Exit!");
                System.exit(0);
            }
        });

    }
/*
    public double getSampleWidth() {
        return 400;
    }

    public double getSampleHeight() {
        return 400;
    }

    public void update() throws RemoteException {
        System.out.println("up");
    }

    public static class Library1 {

        private StringProperty number;
        private StringProperty name;
        private StringProperty date;
        private StringProperty author;

        private Library1(int number, String name, int date, String author) {
            this.number = new SimpleStringProperty(Integer.toString(number));
            this.name = new SimpleStringProperty(name);
            this.date = new SimpleStringProperty(Integer.toString(date));
            this.author = new SimpleStringProperty(author);
        }

        public StringProperty numberProperty() {
            return number;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty dateProperty() {
            return date;
        }

        public StringProperty authorProperty() {
            return author;
        }

    }
*/
    @Override
    public void start(Stage primaryStage) throws Exception {

        init(primaryStage);

        primaryStage.setTitle("Книги");

        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {

        launch(args);
    }
}