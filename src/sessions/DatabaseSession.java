package sessions;

import node.LibraryNode;
import node.LibraryNodeTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

// Класс для управления операциями, изменяющими данные в сессиях с базами данных
public class DatabaseSession implements Runnable {
    // База данных
    private LinkedList<LibraryNode> DATABASE;
    // Путь до базы данных
    private final String DATABASE_PATH;
    // Номер сессии
    private final int SESSION;
    // Хранилище запросов пользователей
    private RequestHandler requestHandler;
    // Переменная, управляющая телом потока
    private boolean isStopped = false;

    // Конструктор
    public DatabaseSession(LinkedList<LibraryNode> database, String DATABASE_PATH, RequestHandler requestHandler) {
        this.DATABASE = database;
        this.DATABASE_PATH = DATABASE_PATH;
        this.SESSION = DATABASE_PATH.hashCode();
        this.requestHandler = requestHandler;
    }

    // Вывод базы по запросу
    public LinkedList<LibraryNode> getDatabase() {
        return DATABASE;
    }

    // Добавление в базу
    public void addNode(LibraryNode source) {
        if (!isCollisionOccurs(source)) {
            DATABASE.add(source);
            this.checkID();
        }
    }

    // Метод, разрешающий возникновение коллизий внутри базы данных
    private boolean isCollisionOccurs(LibraryNode source) {
        for (LibraryNode node: DATABASE) {
            if (nodeComparator(node, source))
                return true;
        }

        return false;
    }

    // Утилита для сравнения двух записей
    private boolean nodeComparator (LibraryNode a, LibraryNode b) {
        if (
                !a.getName().equals(b.getName()) ||
                        !a.getAuthor().equals(b.getAuthor()) ||
                        a.getDate() != b.getDate() ||
                        a.getNumber() != b.getNumber()
                )
            return false;
        else
            return true;
    }

    // Удаление из базы (исключение, если база пуста или такого элемента нет)
    public void deleteNode(LibraryNode source) throws IOException {
        if (DATABASE.contains(source)) {
            DATABASE.remove(source);
            this.checkID();
        } else {
            throw new IOException("Attempt to delete non-existing element.");
        }
    }

    // Метод для проверки корректности идентификаторов
    private void checkID () {
        for (int i = 0; i < DATABASE.size(); i++) {
            DATABASE.get(i).setId(i);
        }
    }

    // Методы чтения и записи (файлы)
    public void SAXReader () {
        File fXML = new File(DATABASE_PATH);
        DATABASE.clear();

        if (fXML.length() == 0) return;

        try {
            SAXParserFactory SAXFactory = SAXParserFactory.newInstance();
            SAXParser SAXReader = SAXFactory.newSAXParser();

            DefaultHandler SAXHandler = new DefaultHandler() {
                LibraryNode readerRecord = null;
                String readerString = null;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equals(LibraryNodeTags.BOOK_TAG)) {
                        readerRecord = new LibraryNode();
                        readerRecord.setId(Integer.parseInt(attributes.getValue("id")));
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equals(LibraryNodeTags.BOOK_TAG)) {
                        DATABASE.add(readerRecord);
                        //For all other end tags the record has to be updated.
                    } else if (qName.equals(LibraryNodeTags.NUMBER_TAG)) {
                        readerRecord.setNumber(Long.parseLong(readerString));
                    } else if (qName.equals(LibraryNodeTags.NAME_TAG)) {
                        readerRecord.setName(readerString);
                    } else if (qName.equals(LibraryNodeTags.AUTHOR_TAG)) {
                        readerRecord.setAuthor(readerString);
                    } else if (qName.equals(LibraryNodeTags.DATE_TAG)) {
                        readerRecord.setDate(Long.parseLong(readerString));
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    readerString = String.copyValueOf(ch, start, length).trim();
                }
            };

            SAXReader.parse(fXML, SAXHandler);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DOMWriter() {
        File fXML = new File(DATABASE_PATH);

        if (DATABASE.isEmpty()) return;

        try {
            DocumentBuilderFactory DOMWriterFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder DOMWriterDocumentBuilder = DOMWriterFactory.newDocumentBuilder();

            // Make a document for work
            Document DOMWriterDocument = DOMWriterDocumentBuilder.newDocument();
            // Creating document root
            Element rootElement = DOMWriterDocument.createElement(LibraryNodeTags.LIBRARY_TAG);
            DOMWriterDocument.appendChild(rootElement);

            if (DATABASE.isEmpty() == false) {
                for (LibraryNode writerRecord : DATABASE) {
                    // Creating sub-element
                    Element subElement = DOMWriterDocument.createElement(LibraryNodeTags.BOOK_TAG);
                    // Setting id atribute
                    subElement.setAttribute("id", Integer.toString(DATABASE.indexOf(writerRecord)));
                    rootElement.appendChild(subElement);

                    Element subElementNumber = DOMWriterDocument.createElement(LibraryNodeTags.NUMBER_TAG);
                    subElementNumber.appendChild(DOMWriterDocument.createTextNode(String.valueOf(writerRecord.getNumber())));
                    subElement.appendChild(subElementNumber);

                    Element subElementName = DOMWriterDocument.createElement(LibraryNodeTags.NAME_TAG);
                    subElementName.appendChild(DOMWriterDocument.createTextNode(writerRecord.getName()));
                    subElement.appendChild(subElementName);

                    Element subElementAuthor = DOMWriterDocument.createElement(LibraryNodeTags.AUTHOR_TAG);
                    subElementAuthor.appendChild(DOMWriterDocument.createTextNode(writerRecord.getAuthor()));
                    subElement.appendChild(subElementAuthor);

                    Element subElementDate = DOMWriterDocument.createElement(LibraryNodeTags.DATE_TAG);
                    subElementDate.appendChild(DOMWriterDocument.createTextNode(String.valueOf(writerRecord.getDate())));
                    subElement.appendChild(subElementDate);
                }
            }

            // Then we need to write data into XML
            // Making an instance of transformer factory
            TransformerFactory TWriterFactory = TransformerFactory.newInstance();
            // Making new transformer
            Transformer TWriter = TWriterFactory.newTransformer();
            //TWriter.setOutputProperty(OutputKeys.INDENT, "yes");
            TWriter.setOutputProperty(OutputKeys.INDENT, "yes");
            TWriter.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            // Creating DOM source
            DOMSource DOMWriterSource = new DOMSource(DOMWriterDocument);
            // Creating output file stream
            StreamResult fileOutput = new StreamResult(fXML);
            // Writing
            TWriter.transform(DOMWriterSource, fileOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Главный метод потока
    @Override
    public void run() {
        try {
            // В начале работы сессии прочитаем базу из файла
            this.SAXReader();
            // Выводим текстовое сообщение об открытой сессии
            System.out.println("DatabaseSession: Session opened on path: " + DATABASE_PATH + ", session ID: " + SESSION);
            // Выполняем какую-то работу
            while (!isStopped) {
                // Получим индекс запроса
                int requestIdx = requestHandler.searchRequest(SESSION);
                // Если индекс равен -1 - значит запросов пока нет
                if (requestIdx == -1) {
                    // Продолжаем
                    continue;
                // В противном случае
                } else {
                    // Получаем запрос
                    Request task = requestHandler.getRequest(requestIdx);
                    // Выводим сообщение о полученном запросе
                    System.out.println("DatabaseSession: Got " + task.getRequestType() + " request at session #: " + SESSION);
                    // Выбираем и производим нужное нам действие c выводом на экран
                    switch (task.getRequestType()) {
                        case ADD:
                            this.addNode(task.getRequestNode());
                            System.out.println("DatabaseSession: ADD request complete at session #: " + SESSION);
                            break;
                        case DELETE:
                            this.deleteNode(task.getRequestNode());
                            System.out.println("DatabaseSession: DELETE request complete at session #: " + SESSION);
                            break;
                        case CLOSE:
                            this.isStopped = true;
                            System.out.println("DatabaseSession: CLOSE request complete at session #: " + SESSION);
                            break;
                        case SAVE:
                            this.DOMWriter();
                            System.out.println("DatabaseSession: SAVE request complete at session #: " + SESSION);
                            break;
                    }
                    // Удаляем выполненный запрос
                    requestHandler.deleteRequest(task);
                }
            }
            // По завершению сессии - сохраняем изменения в файле
            this.DOMWriter();
            // По окончанию работы - очищаем БД из памяти
            this.DATABASE.clear();
            // Выводим уведомление о закрытии сессии
            System.out.println("DatabaseSession: Session #: " + SESSION + " was closed.");
            System.out.println("DatabaseSession: Database: " + DATABASE_PATH + " was successfully saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}