package sessions;

import main.LibraryNode;

import java.util.LinkedList;

// Управление пользовательскими запросами
public class RequestHandler {
    // Структура, хранящая и оперирующая запросами
    private LinkedList<Request> userRequests = new LinkedList<Request>();

    // Получить тип запроса
    public RequestType getRequestType(int requestIndex) {
        return userRequests.get(requestIndex).getRequestType();
    }

    // Добавление запроса
    public synchronized void addRequest (LibraryNode node, int sessionID, RequestType type) {
        userRequests.add(new Request(node, sessionID, type));
    }

    // Удаление запроса
    public synchronized void deleteRequest (Request request) {
        userRequests.remove(request);
    }

    // Поиск запроса
    public synchronized int searchRequest (int sessionID) {
        for (Request rq : userRequests) {
            if (sessionID == rq.getSessionID())
                return userRequests.indexOf(rq);
        }

        return -1;
    }

    // Получение запроса
    public synchronized Request getRequest (int requestIndex) {
        return userRequests.get(requestIndex);
    }
}
