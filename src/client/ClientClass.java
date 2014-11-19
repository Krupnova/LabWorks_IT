package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import node.LibraryNode;
import server.RMI_Interface;
import server.SearchMode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;

public class ClientClass extends UnicastRemoteObject implements ClientInterface {
    // Объект, хранящий интерфейс сервера
    private RMI_Interface server;
    // Объект, хранящий данные базы данных
    public final static ObservableList<LibraryNode> data = FXCollections.observableArrayList();
    // Переменная, хранящая состояние клиента (зарегистрирован на сервере или нет)
    private boolean isRegistered = false;

    // Конструктор
    public ClientClass() throws RemoteException {
        super();
    }

    // Метод, возвращающий состояние клиента
    public boolean isRegistered() {
        return isRegistered;
    }

    // Метод, возвращающий данные из базы данных
    public ObservableList<LibraryNode> getData() {
        return data;
    }

    // Метод, осуществляющий начальное подключение к серверу (подключиться, но не регистрироваться)
    public void connect() throws RemoteException, NotBoundException {
        // Имя объекта
        String objectName = "server";
        // Создаём объект, хранящий запись из регистра по указанным IP и порту
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        // Ищем в регистре объует с указанным именем
        server = (RMI_Interface) registry.lookup(objectName);
    }

    // Добавление записи
    public void AddBook(LibraryNode book) {
        try {
            server.AddBook(book, this);
        } catch (RemoteException ex) {
            System.err.println("Could not connect to the server.");
        }
    }

    // Поиск записи
    public LinkedList<LibraryNode> Searching(String searchSource, SearchMode mode) throws RemoteException {
        return server.Searching(searchSource, mode, this);
    }

    // Удаление записи
    public Boolean DelBook(int index) throws RemoteException {
        return server.DelBook(index, this);
    }

    // Отмена регистрации клиента
    public void unregister(ClientInterface client) throws RemoteException {
        server.unregister(client);
    }

    // Обновление данных для всех клиентов
    public void updateAll() throws RemoteException {
        server.updateAll(this);
    }

    // Получение списка баз данных
    public ObservableList<String> getDatabaseList() throws RemoteException {
        return FXCollections.observableArrayList(server.getDatabaseList());
    }

    // Выбор базы данных и регистрация
    public void setDatabase(ClientClass client, String databaseName) throws Exception {
        server.register(client, databaseName);
        this.isRegistered = true;
    }

    // Обновление данных на клиенте
    public void update() throws RemoteException {
        ArrayList<LibraryNode> data1 = new ArrayList<LibraryNode>();
        try {
            data1.addAll(server.Print(this));
        } catch (RemoteException ex) {
            System.err.println("Could not connect to the server");
        }

        if (!data1.isEmpty()) {
            data.clear();
            data.addAll(data1);
        }
    }
}