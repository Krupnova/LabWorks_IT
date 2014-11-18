package sessions;

import node.LibraryNode;

// Структура пользовательского запроса
public class Request {
    // То, что нужно обработать
    private LibraryNode requestNode;
    // Идентификатор сессии
    private int sessionID;
    // Тип запроса
    private RequestType requestType;

    public Request(LibraryNode requestNode, int sessionID, RequestType requestType) {
        this.requestNode = requestNode;
        this.sessionID = sessionID;
        this.requestType = requestType;
    }

    public LibraryNode getRequestNode() {
        return requestNode;
    }

    public int getSessionID() {
        return sessionID;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestNode=" + requestNode +
                ", sessionID=" + sessionID +
                ", requestType=" + requestType +
                '}';
    }
}
