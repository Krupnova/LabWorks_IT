package node;

import java.io.Serializable;

public class LibraryNode implements Serializable {

    private int id;
    private long number, date;
    private String name, author;

    public LibraryNode() {
    }

    public LibraryNode(int id, long number, String name, long date, String author) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.date = date;
        this.author = author;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String toString() {
        return getId() + " - " + getNumber() + " - " + getName() + " - " + getDate() + " - " + getAuthor();
    }

}
