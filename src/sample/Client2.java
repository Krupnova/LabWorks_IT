package sample;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client2 extends Remote {
    public int update() throws RemoteException;

}
