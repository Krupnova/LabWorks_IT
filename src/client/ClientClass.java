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
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientClass extends UnicastRemoteObject implements ClientInterface {
    private RMI_Interface server;
    public final static ObservableList<LibraryNode> data = FXCollections.observableArrayList();
    private int chosenDatabase = -1;
    private boolean isRegistered = false;

    public boolean isRegistered() {
        return isRegistered;
    }

    public ObservableList<LibraryNode> getData() {
        return data;
    }

    // #debug
    private static final String [] DATABASES = {
            "src/databases/DATABASE1.xml",
            "src/databases/DATABASE2.xml",
            "src/databases/DATABASE3.xml"
    };

    public ClientClass() throws RemoteException {
        super();
    }

    public void lib(/*ClientClass client, String databaseName*/) throws RemoteException, NotBoundException {

        // #debug
        /*System.out.println("Choose database:");
        for (String elem: DATABASES) {
            System.out.println(elem);
        }

        int choose = -1;

        System.out.println("From 1 to 3: ");

        Scanner keyboard = new Scanner(System.in);

        try {
            choose = keyboard.nextInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Your choice: " + choose);*/

        //#debug

        String objectName = "server";
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        server = (RMI_Interface) registry.lookup(objectName);
        //server.register(client, DATABASES[choose - 1]);
    }

    public List Print() throws RemoteException {
        List data1;
        data1 = server.Print(this);
        return data1;
    }

    public void AddBook(LibraryNode book) {
        try {
            server.AddBook(book, this);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientClass.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public LinkedList<LibraryNode> Searching(String ser, SearchMode mode) throws RemoteException {
        return server.Searching(ser, mode, this);
    }

    public Boolean DelBook(int num) throws RemoteException {
        return server.DelBook(num, this);
    }

    /*public void DeleteKol() throws RemoteException {
        server.DeleteKol();
    }*/

    public void unregister(ClientInterface client) throws RemoteException {
        server.unregister(client);
    }

    public void regAll() throws RemoteException {
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


    public int update() throws RemoteException {

        ArrayList<LibraryNode> data1 = new ArrayList<LibraryNode>();
        try {
            data1.addAll(server.Print(this));
        } catch (RemoteException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        data.clear();

        data.addAll(data1);

        return 0;
    }

    public ObservableList<LibraryNode> print() throws RemoteException {

        return data;
    }


}