package main;

import sessions.DatabaseSession;
import sessions.RequestHandler;
import sessions.RequestType;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends UnicastRemoteObject implements RMI_Interface {
    // Список баз данных
    private static final String [] DATABASES = {
            "src/databases/DATABASE1.xml",
            "src/databases/DATABASE2.xml",
            "src/databases/DATABASE3.xml"
    };
    // Порт, на котором работает сервер
    private static final int PORT = 1099;
    // Версионное число
    private static final long serialVersionUID = 1L;
    // Список клиентов (клиент - id сессии)
    private LinkedHashMap<ClientInterface, Integer> clientsList = new LinkedHashMap<ClientInterface, Integer>();
    // Список открытых сессий (id сессии - количество подключённых к ней клиентов)
    private LinkedHashMap<Integer, Integer> activeSessions = new LinkedHashMap<Integer, Integer>();
    // Хранилище сессий
    private ExecutorService sessionsHolder = Executors.newFixedThreadPool(DATABASES.length);
    // Объект для хранения запросов от пользователей
    private RequestHandler requestHandler = new RequestHandler();
    // Хранилище баз данных (id сессии - база данных)
    public LinkedHashMap<Integer, LinkedList<LibraryNode>> databases = new LinkedHashMap<Integer, LinkedList<LibraryNode>>();

    // Конструктор
    public Server() throws RemoteException {
        super();
    }

    // Доабвление записи
    public void AddBook(LibraryNode lib, ClientInterface client) throws RemoteException {
        // Помещаем запрос на добавление в обработку в очередь
        requestHandler.addRequest(lib, clientsList.get(client), RequestType.ADD);
    }

    // Удаление записи
    public boolean DelBook(int nodeIndex, ClientInterface client) throws RemoteException {
        // Если база данных в хранилище по ключу сессии клиента пуста или неверно введён ключ
        if (
                (databases.get(clientsList.get(client)).isEmpty()) ||
                        (nodeIndex < 0) || (nodeIndex >= databases.get(clientsList.get(client)).size())
                ) {
            // Сразу возвращаем false
            return false;
        } else {
            // В противном случае помещаем запрос на удаление в обработку
            requestHandler.addRequest(
                    // Берём напрямую элемент из базы
                    databases.get(clientsList.get(client)).get(nodeIndex),
                    // Берём ID сессии
                    clientsList.get(client),
                    RequestType.DELETE
            );
            return true;
        }
    }

    // Поиск в базе данных (синхронизировано, т.к. databases в общем доступе)
    public synchronized LinkedList<LibraryNode> Searching(String source, SearchMode mode, ClientInterface client)
            throws RemoteException {
        LinkedList<LibraryNode> result = new LinkedList<LibraryNode>();

        switch (mode) {
            case BY_NUMBER:
                for (LibraryNode elem : databases.get(clientsList.get(client))) {
                    if (elem.getNumber() == Integer.parseInt(source)) {
                        result.add(elem);
                    }
                }
                return result;
            case BY_NAME:
                for (LibraryNode elem : databases.get(clientsList.get(client))) {
                    if (elem.getName().equals(source)) {
                        result.add(elem);
                    }
                }
                return result;
            case BY_DATE:
                for (LibraryNode elem : databases.get(clientsList.get(client))) {
                    if (elem.getDate() == Integer.parseInt(source)) {
                        result.add(elem);
                    }
                }
                return result;
            case BY_AUTHOR:
                for (LibraryNode elem : databases.get(clientsList.get(client))) {
                    if (elem.getAuthor().equals(source)) {
                        result.add(elem);
                    }
                }
                return result;
            default:
                return null;
        }
    }

    // Вывод на экран (синхронизировано, т.к. databases в общем доступе)
    public synchronized LinkedList<LibraryNode> Print(ClientInterface client) throws RemoteException {
        return databases.get(clientsList.get(client));
    }

    // Регистрация клиентов
    public void register(ClientInterface client, String database) throws RemoteException {
        // Ищем, открыта ли сессия с данной базой данных
        if (activeSessions.containsKey(database.hashCode())) {
            // Обновляем запись о сессии
            activeSessions.put(database.hashCode(), activeSessions.get(database.hashCode()) + 1);
            // Выводим уведомление
            System.out.println("Server: User had joined to the session #: " + database.hashCode());
        } else {
            // Создаём объект для базы данных
            LinkedList<LibraryNode> tmp = new LinkedList<LibraryNode>();
            // Помещаем объект в хранилище
            databases.put(database.hashCode(), tmp);
            // Помещаем задачу в пул потоков
            sessionsHolder.submit(new DatabaseSession(
                    databases.get(database.hashCode()),
                    database,
                    requestHandler
            ));
            // Помещаем запись об активной сессии в список
            activeSessions.put(database.hashCode(), 1);
            // Выводим уведомление
            System.out.println("Server: User had created new database session #: " + database.hashCode());
        }

        // Помещаем запись о клиенте в хэш-мап
        clientsList.put(client, database.hashCode());
    }

    // Выход клиента из сессии
    public void unregister(ClientInterface client) throws RemoteException {
        // Возьмём номер сесси пользователя
        int currentSession = clientsList.get(client);
        // Изменяем состояние сессии, связанной с клиентом (вычитаем 1)
        activeSessions.put(currentSession, activeSessions.get(currentSession) - 1);
        // Если после изменения состояния клиентов, работающих с сессией, не осталось - закрываем сессию.
        if (activeSessions.get(currentSession) == 0) {
            // Помещаем запрос на закрытие сессии
            requestHandler.addRequest(null, currentSession, RequestType.CLOSE);
            // Убираем сессию из списка активных
            activeSessions.remove(currentSession);
            // Уничтожаем данные о БД
            databases.remove(currentSession);
            // Выводим уведомление
            System.out.println("Server: Last user had left session #: " + currentSession + ", session will be closed.");
        } else {
            // В противном случае просто сохраним базу после выхода пользователя
            requestHandler.addRequest(null, currentSession, RequestType.SAVE);
            // Выводим уведомление
            System.out.println("Server: User had left session #: " + currentSession);
        }

        // Убираем пользователя из списка
        clientsList.remove(client);
    }

    // Обновление данных на всех клиентах
    public void updateAll(ClientInterface client) throws RemoteException {
        // Проходим по всем записям в clientList используя
        for (Map.Entry<ClientInterface, Integer> clientNode : clientsList.entrySet()) {
            // И вызываем метод update для клиентов в этой же сессии
            if (clientNode.getValue().equals(clientsList.get(client)))
                clientNode.getKey().update();
        }
    }

    // Непонятная процедура, пока что закомменчена
    /*public void DeleteKol() throws RemoteException {
        tmpStorage = new LinkedList<LibraryNode>();
    }*/

    public static void main(String args[]) throws RemoteException, MalformedURLException {
        try {
            Registry reg = LocateRegistry.createRegistry(PORT);
            reg.rebind("server", new Server());
            System.out.println("server started");
        } catch (Exception e) {
            System.out.println("Исключение: " + e);
        }
    }
}