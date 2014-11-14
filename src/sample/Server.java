package sample;


import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;

import java.io.BufferedReader;
import java.io.File;
//import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.rmi.*;
//import static java.rmi.Naming.list;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
//import static java.util.Collections.list;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Server extends UnicastRemoteObject implements RMI {

    File f1 = new File("dom.xml");
    private static final long serialVersionUID = 1L;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static ArrayList<Library> al = new ArrayList<Library>();
    public ArrayList<Library> list;
    private Library lib;
    public ArrayList<Client2> clients = new ArrayList<Client2>();

    public Server() throws RemoteException {
        super();
    }

    public void AddBook(Library lib) throws RemoteException {
        al.add(lib);
        System.out.println(al);
        f1.delete();
        File f1 = new File("dom.xml");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = docFactory.newDocumentBuilder();

            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement(TAGS.BOOK_TAG);
            doc.appendChild(rootElement);

            for (Library item : al) {

                Element library = doc.createElement(TAGS.LIBRARY_TAG);
                rootElement.appendChild(library);

                Attr attr = doc.createAttribute(TAGS.ID_TAG);
                attr.setValue(String.valueOf(item.getId()));
                library.setAttributeNode(attr);

                Element number = doc.createElement(TAGS.NUMBER_TAG);
                number.appendChild(doc.createTextNode(String.valueOf(item.getNumber())));
                library.appendChild(number);

                Element name = doc.createElement(TAGS.NAME_TAG);
                name.appendChild(doc.createTextNode(item.getName()));
                library.appendChild(name);

                Element date = doc.createElement(TAGS.DATE_TAG);
                date.appendChild(doc.createTextNode(String.valueOf(item.getDate())));
                library.appendChild(date);

                Element author = doc.createElement(TAGS.AUTHOR_TAG);
                author.appendChild(doc.createTextNode(item.getAuthor()));
                library.appendChild(author);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(getClass().getResource("dom.xml").getPath()));

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }

    public boolean DelBook(int count) throws RemoteException {

        if (!al.isEmpty()) {

            if (count >= 0 && count < al.size()) {
                al.remove(count);
                f1.delete();
                File f1 = new File("dom.xml");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                try {

                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = docFactory.newDocumentBuilder();

                    Document doc = dBuilder.newDocument();
                    Element rootElement = doc.createElement(TAGS.BOOK_TAG);
                    doc.appendChild(rootElement);

                    for (Library item : al) {

                        Element library = doc.createElement(TAGS.LIBRARY_TAG);
                        rootElement.appendChild(library);

                        Attr attr = doc.createAttribute(TAGS.ID_TAG);
                        attr.setValue(String.valueOf(item.getId()));
                        library.setAttributeNode(attr);

                        Element number = doc.createElement(TAGS.NUMBER_TAG);
                        number.appendChild(doc.createTextNode(String.valueOf(item.getNumber())));
                        library.appendChild(number);

                        Element name = doc.createElement(TAGS.NAME_TAG);
                        name.appendChild(doc.createTextNode(item.getName()));
                        library.appendChild(name);

                        Element date = doc.createElement(TAGS.DATE_TAG);
                        date.appendChild(doc.createTextNode(String.valueOf(item.getDate())));
                        library.appendChild(date);

                        Element author = doc.createElement(TAGS.AUTHOR_TAG);
                        author.appendChild(doc.createTextNode(item.getAuthor()));
                        library.appendChild(author);
                    }

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File(getClass().getResource("dom.xml").getPath()));

                    transformer.transform(source, result);

                    System.out.println("Файл сохранен!");

                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                } catch (TransformerException tfe) {
                    tfe.printStackTrace();
                }

                System.out.println("Объект удален");
                try {

                } catch (Exception ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }

            System.out.println("Объект не удален. Повторите попытку");
            return false;
        } else {
            System.out.println("Массив пуст");
        }
        return false;
    }


    public ArrayList<Library> Searching(String ser, int mode) throws RemoteException {

        list = new ArrayList<Library>();
        al = new ArrayList<Library>();
        al = Print();
        System.out.println(al);
        int i = 0;

        System.out.println("Search");

        switch (mode) {
            case 1:
                while (i < al.size()) {
                    if (al.get(i).getNumber() == Integer.parseInt(ser)) {
                        list.add(al.get(i));
                    }
                    i++;
                }
                break;
            case 2:
                while (i < al.size()) {
                    if (al.get(i).getName().equals(ser)) {
                        list.add(al.get(i));
                    }
                    i++;
                }
                break;
            case 3:
                while (i < al.size()) {
                    if (al.get(i).getDate() == Integer.parseInt(ser)) {
                        list.add(al.get(i));
                    }
                    i++;
                }
                break;
            case 4:
                while (i < al.size()) {
                    if (al.get(i).getAuthor().equals(ser)) {
                        list.add(al.get(i));
                    }
                    i++;
                }
                break;

        }

        System.out.println(list);
        return list;

    }

    public void ReadXL() throws RemoteException {

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(getClass().getResourceAsStream("dom.xml"));

            NodeList nList = doc.getElementsByTagName(TAGS.LIBRARY_TAG);

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    DeferredElementImpl el = (DeferredElementImpl) nNode;
                    String author = el.getElementsByTagName(TAGS.AUTHOR_TAG).item(0).getTextContent();
                    int id = Integer.parseInt(el.getAttribute(TAGS.ID_TAG));
                    long number = Long.parseLong(el.getElementsByTagName(TAGS.NUMBER_TAG).item(0).getTextContent());
                    String name = el.getElementsByTagName(TAGS.NAME_TAG).item(0).getTextContent();
                    long date = Long.parseLong(el.getElementsByTagName(TAGS.DATE_TAG).item(0).getTextContent());

                    lib = new Library(id, number, name, date, author);
                }
                al.add(lib);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Library> Print() throws RemoteException {
        ArrayList<Library> al1 = new ArrayList();
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(getClass().getResourceAsStream("dom.xml"));

            NodeList nList = doc.getElementsByTagName(TAGS.LIBRARY_TAG);

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    DeferredElementImpl el = (DeferredElementImpl) nNode;
                    String author = el.getElementsByTagName(TAGS.AUTHOR_TAG).item(0).getTextContent();
                    int id = Integer.parseInt(el.getAttribute(TAGS.ID_TAG));
                    long number = Long.parseLong(el.getElementsByTagName(TAGS.NUMBER_TAG).item(0).getTextContent());
                    String name = el.getElementsByTagName(TAGS.NAME_TAG).item(0).getTextContent();
                    long date = Long.parseLong(el.getElementsByTagName(TAGS.DATE_TAG).item(0).getTextContent());

                    lib = new Library(id, number, name, date, author);
                }
                al1.add(lib);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return al1;
    }



    public Boolean register(Client2 client) throws RemoteException {
        clients.add(client);

        return true;
    }

    public void unregister(Client2 client) throws RemoteException {

        clients.remove(client);


    }

    public void registerAll() throws RemoteException {
        for (Client2 client : clients) {
            client.update();

        }
    }


    public static void main(String args[]) throws RemoteException, MalformedURLException {
        try {

            Registry reg = LocateRegistry.createRegistry(1099);
            reg.rebind("server", new Server());
            System.out.println("server started");
        } catch (Exception e) {
            System.out.println("Исключение: " + e);
        }
    }

    public void DeleteKol() throws RemoteException {
        al = new ArrayList<Library>();
    }
}
