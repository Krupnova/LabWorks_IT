package main;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface RMI_Interface extends Remote {

    public boolean DelBook(int count, ClientInterface client) throws RemoteException;

    public LinkedList<LibraryNode> Searching(String ser, SearchMode mode, ClientInterface client) throws RemoteException;

    public void AddBook(LibraryNode kon, ClientInterface client) throws RemoteException;

    public LinkedList<LibraryNode> Print(ClientInterface client) throws RemoteException;

    //public void DeleteKol() throws RemoteException;

    public void updateAll(ClientInterface client) throws RemoteException;

    public void register(ClientInterface client, String database) throws RemoteException;

    public void unregister(ClientInterface client) throws RemoteException;


}
