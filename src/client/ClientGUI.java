package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import node.LibraryNode;
import server.SearchMode;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientGUI extends Application {
    // Класс, реализующий связь с сервером
    private ClientClass client;
    // Класс, хранящий список баз данных
    private ObservableList<String> databaseList;
    // Хранилище сцен графического интерфейса
    private StackPane layouts;

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
    // Константа, которая хранит общую высоту окна
    private final double WINDOW_HEIGHT = 450;
    // Константа, которая хранит ширину части окна с инструментарием
    private final double TOOLS_WIDTH = 350;
    // Константа, которая хранит высоту панели инструментов
    private final double TOOLS_HEIGHT = 40;
    // Константа, которая хранит отступ слева по-умолчанию для элементов сцены root
    private final double ROOT_STAGE_LEFT_SPACING = 50;

    // Создаём буфер для созданных графических элементов
    private LinkedList<Object> objectBuffer = new LinkedList<Object>();

    // Таблица
    private TableView mainDatabase;

    // Стандартные поля и значения
    private final String[] TOOLS_LIST = {
            "Добавление",
            "Удаление",
            "Найти"
    };

    // Добавляем индексы кнопок, для использования
    int addIndex = 0, deleteIndex = 1, findIndex = 2;

    private final String[] TABLE_FIELDS_NAMES = {
            "ID",
            "Номер",
            "Название",
            "Дата",
            "Автор"
    };

    private final String[] TABLE_FIELDS = {
            "id",
            "number",
            "name",
            "date",
            "author"
    };

    // СОЗДАНИЕ ГРАФИЧЕСКИХ ЭЛЕМЕНТОВ СЦЕН

    // Метод для создания текстовых полей
    private Label createLabel (Group root, final double layoutX, final double layoutY, final String text) {
        // Создаём текстовое поле
        Label newLabel = new Label(text);
        // Задаём отступы
        newLabel.setLayoutX(layoutX);
        newLabel.setLayoutY(layoutY);
        // Помещаем созданный объект на форму
        root.getChildren().add(newLabel);
        // Оставляем ссылку на объект в буфере
        objectBuffer.add(newLabel);
        // ВОзвращаем объект
        return newLabel;
    }

    // Процедура автоматического создания текстового поля с его наименованием на форме
    private TextField createTextField (Group root, final double layoutX, final double layoutY, final double width,
                                       final String fieldName) {
        // Создаём новый текстовый объект с именем поля
        createLabel(root, layoutX + DEFAULT_LEFT_SPACING, layoutY, fieldName);
        // Создаём новое текстовое поле
        TextField newTextField = new TextField();
        // Задаём ему размеры
        newTextField.setMaxSize(width, DEFAULT_HEIGHT);
        newTextField.setMinSize(width, DEFAULT_HEIGHT);
        // Задаём отступ слева
        newTextField.setLayoutX(layoutX);
        // Задаём отступ сверху с учётом константы
        newTextField.setLayoutY(layoutY + DEFAULT_HEIGHT);
        // Помещаем созданные объекты на форму
        root.getChildren().add(newTextField);
        // Оставляем ссылки на объекты в буфере
        objectBuffer.add(newTextField);
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
    private Button createButton (Group root, final double layoutX, final double layoutY, final double width,
                                 final String buttonText) {
        // Создаём объект кнопки
        Button newButton = new Button(buttonText);
        // Задаём параметры
        newButton.setLayoutX(layoutX);
        newButton.setLayoutY(layoutY);

        if (width != -1) {
            newButton.setMaxWidth(width);
            newButton.setMinWidth(width);
        }

        // Размещаем кнопку на форме
        root.getChildren().add(newButton);
        // Оставляем ссылку на кнопку в буффере
        objectBuffer.add(newButton);
        // Возвращаем кнопку
        return newButton;
    }

    // Метод для вывода базы данных
    private TableView printDatabase (Group root, final String[] columns, final String[] columnsValues) throws RemoteException {
        // Создаём объект таблицы
        TableView newTableView = new TableView();
        // Задаём параметры
        newTableView.setLayoutX(TOOLS_WIDTH);
        newTableView.setLayoutY(TOOLS_HEIGHT);

        double tableWidth = WINDOW_WIDTH - TOOLS_WIDTH;

        newTableView.setMaxWidth(tableWidth);
        newTableView.setMinWidth(tableWidth);
        newTableView.setMinHeight(WINDOW_HEIGHT - TOOLS_HEIGHT);
        newTableView.setMaxHeight(WINDOW_HEIGHT - TOOLS_HEIGHT);

        // Создадим колонки для таблицы
        for (int i = 0; i < columns.length; i++) {
            // Создаём колонку
            TableColumn newColumn = new TableColumn();
            // Задаём её название
            newColumn.setText(columns[i]);
            // Задаём её минимальную ширину
            newColumn.setMaxWidth(tableWidth / columns.length);
            // Задаём значение, которое будет храниться в этой колонке из LibraryNode
            newColumn.setCellValueFactory(new PropertyValueFactory(columnsValues[i]));
            // Помещаем в таблицу
            newTableView.getColumns().add(newColumn);
        }

        //
        client.update();
        // Заполняем таблицу
        newTableView.setItems(client.getData());
        // Помещаем её на форму
        root.getChildren().add(newTableView);
        // Возвращаем таблицу
        return newTableView;
    }

    // Метод для создания панели инструментов
    private LinkedList<Button> createToolbar (Group root, final String[] toolNames) {
        // Список кнопок на панели инструментов
        LinkedList<Button> tools = new LinkedList<Button>();
        // Инициализируем панель инструментов
        final ToolBar newToolbar = new ToolBar();
        // Задаём размеры панели инструментов
        newToolbar.setMinHeight(TOOLS_HEIGHT);
        newToolbar.setMinWidth(WINDOW_WIDTH);
        // Создаём кнопки на панели
        for (String toolName: toolNames) {
            Button newTool = new Button(toolName.toUpperCase());
            newToolbar.getItems().add(newTool);
            tools.add(newTool);
        }
        // Размещаем панель инструментов на форме
        root.getChildren().add(newToolbar);
        // Возвращаем кнопки для работы с ними
        return tools;
    }

    // СОЗДАНИЕ ГРАФИЧЕСКИХ СЦЕН

    // Создаём группу объектов для выбора базы данных
    private Group selectDatabase () throws Exception {
        // Создаём группу объектов
        final Group newGroup = new Group();

        // Создаём фон группы
        Rectangle background = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        background.setFill(Color.LIGHTGRAY);
        newGroup.getChildren().add(background);
        objectBuffer.add(background);

        // Получим данные от сервера о базах данных
        try {
            databaseList = client.getDatabaseList();
        } catch (RemoteException e) {
            System.err.println("Could not connect to the server.");
        }

        // Создадим выпадающий список и заполним его значениями
        ObservableList<String> comboBoxValues = FXCollections.observableArrayList();
        // Заполняем его значениями
        if (!databaseList.isEmpty()) {
            for (String source : databaseList) {
                comboBoxValues.add(getDatabaseName(source));
            }
        } else {
            throw new Exception("Cannot initialize database list.");
        }
        // Размеры списка
        final double comboBoxWidth = 350, comboBoxHeight = 25;
        // Создаём объект combo box
        final ComboBox<String> newComboBox = new ComboBox<String>(comboBoxValues);
        // Зададим параметры списка
        newComboBox.setMinSize(comboBoxWidth, comboBoxHeight);
        newComboBox.setMaxSize(comboBoxWidth, comboBoxHeight);
        newComboBox.setLayoutX((WINDOW_WIDTH - comboBoxWidth) / 2);
        newComboBox.setLayoutY(2 * DEFAULT_SPACING + DEFAULT_HEIGHT);
        newComboBox.setItems(comboBoxValues);
        newComboBox.setValue(comboBoxValues.get(0));
        // Добавляем список на форму
        newGroup.getChildren().add(newComboBox);
        objectBuffer.add(newComboBox);

        // Создадим текстовое поле
        createLabel(
                newGroup,
                ((WINDOW_WIDTH - comboBoxWidth) / 2) + DEFAULT_LEFT_SPACING,
                DEFAULT_SPACING,
                "Выберите базу данных из списка"
        );

        // Создадим кнопки управления
        final double buttonSpace = DEFAULT_HEIGHT;
        final double buttonWidth = (comboBoxWidth - 2 * DEFAULT_LEFT_SPACING - buttonSpace) / 2;

        Button confirm = createButton(
                newGroup,
                ((WINDOW_WIDTH - comboBoxWidth) / 2) + DEFAULT_LEFT_SPACING,
                3 * DEFAULT_SPACING + DEFAULT_HEIGHT + comboBoxHeight,
                buttonWidth,
                "OK"
        );

        confirm.setDefaultButton(true);

        confirm.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    client.setDatabase(
                            client,
                            databaseList.get(newComboBox.getSelectionModel().getSelectedIndex())
                    );
                    layouts.getChildren().remove(this);
                    clearObjectBuffer(newGroup);
                    layouts.getChildren().add(initializeGUI());
                } catch (Exception e) {}
            }
        });


        Button decline = createButton(
                newGroup,
                ((WINDOW_WIDTH - comboBoxWidth) / 2) + DEFAULT_LEFT_SPACING + buttonWidth + buttonSpace,
                3 * DEFAULT_SPACING + DEFAULT_HEIGHT + comboBoxHeight,
                buttonWidth,
                "Cancel"
        );

        decline.setCancelButton(true);

        decline.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Database was not chosen, exit!");
                System.exit(0);
            }
        });

        // Возвращаем группу объектов
        return newGroup;
    }

    // Инициализируем графический интерфейс
    private Group initializeGUI() throws Exception {
        // Инициализируем базовую группу инструментов рабочего окна
        final Group root = new Group();
        // Инициализируем панель инструментов
        LinkedList<Button> tools = createToolbar(root, TOOLS_LIST);
        // Обновляем БД
        // client.update();
        // Выводим базу данных
        mainDatabase = printDatabase(root, TABLE_FIELDS_NAMES, TABLE_FIELDS);
        // Разрешаем множественное выделение строк в базе данных
        mainDatabase.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Создаём подложку для области ввода данных для запросов к базе
        final Rectangle toolsBackground = new Rectangle(TOOLS_WIDTH, WINDOW_HEIGHT - TOOLS_HEIGHT);
        toolsBackground.setLayoutX(0);
        toolsBackground.setLayoutY(TOOLS_HEIGHT);
        toolsBackground.setFill(Color.ANTIQUEWHITE);
        root.getChildren().add(toolsBackground);
        // Добавляем обработчик нажатия на кнопку добавления
        tools.get(addIndex).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // Очищаем буфер
                clearObjectBuffer(root);
                // Меняем цвет фона
                toolsBackground.setFill(Color.AQUA);
                // Имена текстовых полей
                final String[] textFieldsName = {
                        "Номер",
                        "Название книги",
                        "Дата выдачи",
                        "Автор"
                };
                // Создаём текстовые поля
                final LinkedList<TextField> addFields = createFields(
                        root,
                        ROOT_STAGE_LEFT_SPACING,
                        TOOLS_HEIGHT,
                        TOOLS_WIDTH - 2 * ROOT_STAGE_LEFT_SPACING,
                        textFieldsName
                );
                // Кнопка для подтверждения
                Button addConfirm = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING + DEFAULT_BUTTON_LEFT_SPACING,
                        TOOLS_HEIGHT + textFieldsName.length * (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        -1,
                        "Добавить"
                );
                // Обработчик нажания кнопки подтверждения
                addConfirm.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        // Проверка введённых данных
                        for (int i = 0; i < addFields.size(); i++) {
                            if (addFields.get(i).getText().isEmpty()) {
                                return;
                            } else if ((i % 2 == 0) && (!isValidLong(addFields.get(i).getText()))) {
                                return;
                            }
                        }
                        try {
                            client.AddBook(
                                    new LibraryNode(
                                            0,
                                            Long.parseLong(addFields.get(0).getText()),
                                            addFields.get(1).getText(),
                                            Long.parseLong(addFields.get(2).getText()),
                                            addFields.get(3).getText()
                                    )
                            );

                            client.updateAll();
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }
                    }
                });
            }
        });

        // Добавляем обработчик нажания на кнопку Найти
        tools.get(findIndex).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // Очищаем буфер
                clearObjectBuffer(root);
                // Задаём фон
                toolsBackground.setFill(Color.GREEN);
                // Cоздадим переменную, которая хранит расстояние между кнопками
                double space = 10;
                // Количество кнопок
                double buttonsAmount = 4;
                // Создадим переменную, которая хранит ширину кнопок
                double buttonWidth = ((TOOLS_WIDTH - 2 * ROOT_STAGE_LEFT_SPACING) - (buttonsAmount - 1) * space) / buttonsAmount;
                // Создаём текстовое поле
                final TextField text = createTextField(
                        root,
                        ROOT_STAGE_LEFT_SPACING,
                        TOOLS_HEIGHT,
                        TOOLS_WIDTH - 2 * ROOT_STAGE_LEFT_SPACING,
                        "Введите данные элемента для поиска:"
                );
                // Создадим кнопки
                Button byNumber = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING,
                        TOOLS_HEIGHT + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        buttonWidth,
                        "Номер"
                );
                Button byDate = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING + (buttonWidth + space),
                        TOOLS_HEIGHT + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        buttonWidth,
                        "Дата"
                );
                Button byName = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING + 2*(buttonWidth + space),
                        TOOLS_HEIGHT + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        buttonWidth,
                        "Имя"
                );
                Button byAuthor = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING + 3*(buttonWidth + space),
                        TOOLS_HEIGHT + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        buttonWidth,
                        "Автор"
                );

                // Если ищем по номеру
                byNumber.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        // Проверка данных
                        if (!isValidLong(text.getText()))
                            return;

                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(text.getText(), SearchMode.BY_NUMBER);
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }

                        System.out.println(lib);

                        if (lib != null) {
                            mainDatabase.getSelectionModel().clearSelection();
                            for (LibraryNode node: lib) {
                                mainDatabase.getSelectionModel().select(node.getId());
                            }
                        }
                    }
                });
                // Если ищем по имени
                byName.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(text.getText(), SearchMode.BY_NAME);
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }

                        System.out.println(lib);

                        if (lib != null) {
                            mainDatabase.getSelectionModel().clearSelection();
                            for (LibraryNode node: lib) {
                                mainDatabase.getSelectionModel().select(node.getId());
                            }
                        }
                    }
                });
                // Если ищем по дате
                byDate.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {


                        // Проверка данных
                        if (!isValidLong(text.getText()))
                            return;

                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(text.getText(), SearchMode.BY_DATE);
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }

                        System.out.println(lib);

                        if (lib != null) {
                            mainDatabase.getSelectionModel().clearSelection();
                            for (LibraryNode node: lib) {
                                mainDatabase.getSelectionModel().select(node.getId());
                            }
                        }
                    }
                });
                // Если ищем по автору
                byAuthor.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        LinkedList<LibraryNode> lib = new LinkedList<LibraryNode>();
                        try {
                            lib = client.Searching(text.getText(), SearchMode.BY_AUTHOR);
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }

                        System.out.println(lib);

                        if (lib != null) {
                            mainDatabase.getSelectionModel().clearSelection();
                            for (LibraryNode node: lib) {
                                mainDatabase.getSelectionModel().select(node.getId());
                            }
                        }
                    }
                });
            }
        });

        // Задаём обработчик инструмента удаления
        tools.get(deleteIndex).setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // Очищаем буфер
                clearObjectBuffer(root);
                // Задаём фон
                toolsBackground.setFill(Color.YELLOW);
                // Создаём текстовое поле
                final TextField id = createTextField(
                        root,
                        ROOT_STAGE_LEFT_SPACING,
                        TOOLS_HEIGHT,
                        TOOLS_WIDTH - 2 * ROOT_STAGE_LEFT_SPACING,
                        "Введите ID удаляемого элемента:"
                );
                // Создаём кнопку подтверждения
                Button deleteButton = createButton(
                        root,
                        ROOT_STAGE_LEFT_SPACING + DEFAULT_BUTTON_LEFT_SPACING,
                        TOOLS_HEIGHT + (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        -1,
                        "УДАЛИТЬ"
                );

                // Создаём текстовое поле с уведомлением
                final Label notification = createLabel(
                        root,
                        ROOT_STAGE_LEFT_SPACING,
                        TOOLS_HEIGHT + 2 * (2 * DEFAULT_HEIGHT + DEFAULT_SPACING) + DEFAULT_SPACING,
                        ""
                );

                // Задаём действие на кнопку подтверждения удаления
                deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        notification.setText("");
                        // Берём ID элемента с проверкой
                        int removeIndex;
                        // Проверка введённых данных
                        try {
                            removeIndex = Integer.parseInt(id.getText());
                        } catch (NumberFormatException nfe) {
                            return;
                        }

                        // Удаляем
                        try {
                            if (client.DelBook(removeIndex)) {
                                notification.setText("Запись # " + id.getText() + " была удалена.");
                            } else {
                                notification.setText("Запись # " + id.getText() + " не была удалена.");
                            }

                            client.updateAll();
                        } catch (RemoteException ex) {
                            System.err.println("Could not connect to the server");
                        }
                    }
                });
            }
        });

        // Возвращаем группу
        return root;
    }

    // УТИЛИТЫ

    // Метод для очистки буфера
    private void clearObjectBuffer(Group root) {
        for (Object obj: objectBuffer) {
            root.getChildren().remove(obj);
        }
        objectBuffer.clear();
    }

    // Строковый парсер, убирающий все ненужные символы, кроме названия базы данных
    private String getDatabaseName(String source) {
        return source.substring(source.lastIndexOf('/') + 1, source.lastIndexOf('.'));
    }

    // Проверка валидности введенного числа
    private boolean isValidLong (String source) {
        try {
            Long.parseLong(source);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    // ПЕРЕОПРЕДЕЛЁННЫЕ СТАНДАРТНЫЕ МЕТОДЫ

    // Метод обработки закрытия окна через крестик в углу
    @Override
    public void stop() {
        if (client.isRegistered()) {
            try {
                client.unregister(client);
            } catch (RemoteException e) {
                System.err.println("Could not connect to the server");
            }
        }
        System.out.println("Exit!");
        System.exit(0);
    }

    // Метод для запуска GUI
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Инициализируем объект, который служит для связи с сервером.
        client = new ClientClass();
        // Пробуем присоединиться к серверу
        client.connect();
        // Указываем титульник окна
        primaryStage.setTitle("Книги");
        // Запрещаем изменение размеров окна
        primaryStage.setResizable(false);
        // Задание параметров основного окна программы (объект сцены, ширина, высота)
        primaryStage.setMinWidth(WINDOW_WIDTH + 5);
        primaryStage.setMaxWidth(WINDOW_WIDTH + 5);
        primaryStage.setMinHeight(WINDOW_HEIGHT + 25);
        primaryStage.setMaxHeight(WINDOW_HEIGHT + 25);
        // СОздаём список графических сцен
        layouts = new StackPane();
        layouts.getChildren().add(selectDatabase());
        // Задаём сцену
        primaryStage.setScene(new Scene(layouts));
        // Показываем окно
        primaryStage.show();
    }

    // Главный метод для запуска приложения
    public static void main(String[] args) throws Exception {
        launch(args);
    }
}