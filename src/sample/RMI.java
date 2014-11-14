package sample;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface RMI extends Remote {

    public boolean DelBook(int count) throws RemoteException;

    public ArrayList<Library> Searching(String ser, int mode) throws RemoteException;

    public void AddBook(Library kon) throws RemoteException;

    public List<Library> Print() throws RemoteException;

    public void ReadXL() throws RemoteException;

    public void DeleteKol() throws RemoteException;

    public void registerAll() throws RemoteException;

    public Boolean register(Client2 client) throws RemoteException;

    public void unregister(Client2 client) throws RemoteException;


}
