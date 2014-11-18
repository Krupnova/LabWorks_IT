package main;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
//import static javafx.application.Application.launch;

import static javafx.application.Application.launch;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class ClientGUI extends Application {

    private static int count = 0;
    private ArrayList<LibraryNode> lib = new ArrayList<LibraryNode>();
    private ClientClass client;

    // Константа, хранящая высоту элементов по-умолчанию
    private final double DEFAULT_HEIGHT = 20;
    // Константа, хранящая отступ слева по-умолчанию
    private final double DEFAULT_LEFT_SPACING = 20;
    // Константа, хранящая отступ слева по-умолчанию для кнопок
    private final double DEFAULT_BUTTON_LEFT_SPACING = 50;
    // Константа, которая будет определять отступ между объектами групп
    private final double DEFAULT_SPACING = 5;
    // Константа, которая хранит общую ширину окна
    private final double WINDOW_WIDTH = 800;
    // Константа, которая хранит ширину части окна с инструментарием
    private final double TOOLS_WIDTH = 350;

   /* private enum Buttons {
        ADDING, REMOVING, OUTPUT, SEARCHING, EXIT
    }*/

    // Процедура автоматического создания текстового поля с его наименованием на форме
    private TextField createTextField (Group root, final double layoutX, final double layoutY, final double width,
                                       final String fieldName) {
        // Создаём новый текстовый объект с именем поля
        Label newLabel = new Label(fieldName);
        // Задаём левый отступ с учётом константы
        newLabel.setLayoutX(layoutX + DEFAULT_LEFT_SPACING);
        // Задаём отступ сверху
        newLabel.setLayoutY(layoutY);
        // Создаём новое текстовое поле
        TextField newTextField = new TextField();
        // Задаём ему размеры
        newTextField.setMaxSize(width, DEFAULT_HEIGHT);
        // Задаём отступ слева
        newTextField.setLayoutX(layoutX);
        // Задаём отступ сверху с учётом константы
        newTextField.setLayoutY(layoutY + DEFAULT_HEIGHT);
        // Помещаем созданные объекты на форму
        root.getChildren().add(newLabel);
        root.getChildren().add(newTextField);
        // Возвращаем текстовое поле для дальнеёшей обработки введённых данных
        return newTextField;
    }

    // Метод для создания групп объектов для ввода данных
    private LinkedList<TextField> createFields (Group root, final double layoutX, final double layoutY, final double width,
                                                final String[] fieldNames) {
        // Создаём массив из объектов
        LinkedList<TextField> newTextFields = new LinkedList<TextField>();
        // В цикле по количеству имён
        for (int i = 0; i < fieldNames.length; i++) {
            if (i == 0) {
                newTextFields.add(createTextField(root, layoutX, layoutY, width, fieldNames[i]));
            } else {
                newTextFields.add(createTextField(
                        root,
                        layoutX,
                        /*
                        т.к. верхний отступ у нас будет постоянно смещаться (элементы добавляем в столбик),
                        то необходимо сделать его изменение, при помощи DEFAULT'ов. Умножение на i - для увеличения
                        значения отступа
                         */
                        layoutY + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) * i,
                        width,
                        fieldNames[i]
                ));
            }
        }
        // Возвращаем созданные объекты
        return newTextFields;
    }

    // Метод для создания кнопочек :3
    private Button createButton (Group root, final double layoutX, final double layoutY, final String buttonText) {
        // Создаём объект кнопки
        Button newButton = new Button(buttonText);
        // Задаём параметры
        newButton.setLayoutX(layoutX);
        newButton.setLayoutY(layoutY);
        // Размещаем кнопку на форме
        root.getChildren().add(newButton);
        // Возвращаем кнопку
        return newButton;
    }

    // Метод для вывода базы данных
    private TableView printDatabase (Group root, final String[] columns, final String[] columnsValues) {
        // Создаём объект таблицы
        TableView newTableView = new TableView();
        // Задаём параметры
        newTableView.setLayoutX(TOOLS_WIDTH);
        newTableView.setLayoutY(0);

        double tableWidth = WINDOW_WIDTH - TOOLS_WIDTH;

        newTableView.setMaxWidth(tableWidth);
        newTableView.setMinWidth(tableWidth);

        // Создадим колонки для таблицы
        for (int i = 0; i < columns.length; i++) {
            // Создаём колонку
            TableColumn newColumn = new TableColumn();
            // Задаём её название
            newColumn.setText(columns[i]);
            // Задаём её минимальную ширину
            newColumn.setMinWidth(tableWidth / columns.length);
            // Задаём значение, которое будет храниться в этой колонке из LibraryNode
            newColumn.setCellValueFactory(new PropertyValueFactory(columnsValues[i]));
            // Помещаем в таблицу
            newTableView.getColumns().add(newColumn);
        }

        // Заполняем таблицу
        newTableView.setItems(client.getData());

        // Помещаем её на форму
        root.getChildren().add(newTableView);
        // Возвращаем таблицу
        return newTableView;
    }

    private void initializeGUI(final Stage primaryStage) throws RemoteException, NotBoundException {

        System.out.println("Starting...");

        // Инициализируем объект, который служит для связи с сервером.
        client = new ClientClass();
        // Пробуем присоединиться к серверу
        client.lib(client);

        final List data1 = client.Print();
        // Инициализируем базовую группу инструментов рабочего окна
        final Group root = new Group();
        // Запрещаем изменение размеров окна
        primaryStage.setResizable(false);
        // Задание параметров основного окна программы (объект сцены, ширина, высота)
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, 450));
        // Инициализируем панель инструментов
        final ToolBar toolbar = new ToolBar();

        /*Button[] controlButtons = {
                new Button("ДОБАВЛЕНИЕ"),
                new Button("УДАЛЕНИЕ"),
                new Button("ВЫВОД"),
                new Button("НАЙТИ"),
                new Button("ВЫЙТИ")
        };

        for (Button b : controlButtons)
                toolbar.getItems().add(b);*/

        // Создаём кнопки для управления БД
        Button Adding = new Button("ДОБАВЛЕНИЕ");
        Button Removing = new Button("УДАЛЕНИЕ");
        Button Output = new Button("ВЫВОД"); // delete this horseshit
        Button Searching = new Button("НАЙТИ");
        Button Exit = new Button("ВЫЙТИ");

        toolbar.getItems().add(Adding);
        toolbar.getItems().add(Removing);
        toolbar.getItems().add(Output);
        toolbar.getItems().add(Searching);
        toolbar.getItems().add(Exit);

        root.getChildren().add(toolbar);
        toolbar.setMinSize(TOOLS_WIDTH, 40);

        System.out.println("toolbar width: " + toolbar.getWidth());

        client.update();

        printDatabase(
                root,
                new String[] {
                        "Номер",
                        "Название",
                        "Дата",
                        "Автор"
                },
                new String[] {
                        "number",
                        "name",
                        "date",
                        "author"
                }
        );

//adding
        Adding.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {

                System.out.println("toolbar width: " + toolbar.getWidth());

                Rectangle rect = new Rectangle(TOOLS_WIDTH, 410);

                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.AQUA);
                rect.getStyleClass().add("my-rect");
                root.getChildren().addAll(rect);

                // Имена текстовых полей
                final String[] textFieldsName = {
                        "Номер",
                        "Название книги",
                        "Дата выдачи",
                        "Автор"
                };
                // Создаём текстовые поля
                final LinkedList<TextField> addFields = createFields(root, 20, 220, 200, textFieldsName);
                // Кнопка для подтверждения
                Button addConfirm = createButton(
                        root,
                        20 + DEFAULT_BUTTON_LEFT_SPACING,
                        220 + textFieldsName.length * (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        "OKAY"
                );

                addConfirm.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        String number1 = addFields.get(0).getText();
                        String date1 = addFields.get(2).getText();
                        String name1 = addFields.get(1).getText();
                        String author1 = addFields.get(3).getText();
                        int i = 0;
                        LibraryNode book = new LibraryNode(i, Long.parseLong(number1), name1, Long.parseLong(date1), author1);
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

                System.out.println("root: " + root.getChildren().toString());
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
                        count = 0;
                        String field = text.getText();
                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(field, SearchMode.BY_NUMBER);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println(lib);
                        if (lib != null) {
                            ObservableList<LibraryNode> data = FXCollections.observableArrayList(lib);

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
                        count = 1;
                        String field = text.getText();
                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(field, SearchMode.BY_NAME);
                        } catch (RemoteException ex) {
                            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (lib != null) {
                            final ObservableList<LibraryNode> data = FXCollections.observableArrayList(lib);
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
                            count = 2;
                            String field = text.getText();
                            LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();

                            lib = client.Searching(field, SearchMode.BY_DATE);
                            if (lib != null) {
                                final ObservableList<LibraryNode> data = FXCollections.observableArrayList(lib);
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
                            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                });
                Author_s.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        try {
                            count = 3;
                            String field = text.getText();
                            LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();

                            lib = client.Searching(field, SearchMode.BY_AUTHOR);
                            if (lib != null) {
                                final ObservableList<LibraryNode> data = FXCollections.observableArrayList(lib);
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
                            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                System.out.println("root: " + root.getChildren().toString());

            }
        });
// delete
        Removing.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(500, 400);
                rect.setLayoutX(0);
                rect.setLayoutY(40);
                rect.setFill(Color.YELLOW);
                rect.getStyleClass().add("my-rect");

                root.getChildren().addAll(rect);

                Label label = new Label("Введите ID удаляемого элемента");
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
                            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            client.regAll();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                });


                System.out.println("root: " + root.getChildren().toString());
            }



        });

//print
        Output.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Rectangle rect = new Rectangle(500, 400);
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

                System.out.println("root: " + root.getChildren().toString());
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

    // Метод обработки закрытия окна через крестик в углу
    @Override
    public void stop() {
        try {
            client.unregister(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Exit!");
        System.exit(0);
    }

    // Метод для запуска GUI
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Инициализируем окно
        initializeGUI(primaryStage);
        // Указываем титульник окна
        primaryStage.setTitle("Книги");
        // Показываем окно
        primaryStage.show();
    }

    // Главный метод для запуска приложения
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}