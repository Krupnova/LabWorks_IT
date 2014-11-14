package sample;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends UnicastRemoteObject implements Client2 {
    RMI server;
    final static ObservableList<Library> data = FXCollections.observableArrayList();

    public Client() throws RemoteException {
        super();
    }

    public void lib(Client client) throws RemoteException, NotBoundException {
        String objectName = "server";
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);


        server = (RMI) registry.lookup(objectName);
        server.register(client);
    }

    public List Print() throws RemoteException {
        List data1;
        data1 = server.Print();
        return data1;
    }

    public void AddBook(Library book) {
        try {
            server.AddBook(book);
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ArrayList Searching(String ser, int mode) throws RemoteException {
        ArrayList lib;
        lib = server.Searching(ser, mode);
        return lib;
    }

    public Boolean DelBook(int num) throws RemoteException {
        return server.DelBook(num);
    }

    public void DeleteKol() throws RemoteException {
        server.DeleteKol();
    }

    public void ReadXL() throws RemoteException {
        server.ReadXL();
    }

    public void unregister(Client2 client) throws RemoteException {
        server.unregister(client);
    }

    public void regAll() throws RemoteException {
        server.registerAll();
    }

    public int update() throws RemoteException {

        ArrayList<Library> data1 = new ArrayList<Library>();
        try {
            data1 = (ArrayList<Library>) server.Print();
        } catch (RemoteException ex) {
            Logger.getLogger(Client3.class.getName()).log(Level.SEVERE, null, ex);
        }

        data.clear();

        data.addAll(data1);

        return 0;
    }

    public ObservableList<Library> print() throws RemoteException {

        return data;
    }


}