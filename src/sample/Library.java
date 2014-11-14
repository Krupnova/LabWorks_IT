package sample;


import java.io.Serializable;

public class Library implements Serializable {

    int id;
    private long number;
    private String name;
    private long date;
    private String author;

    public Library(int id) {
        this.id = id;
    }

    public Library(int id, long number, String name, long date, String author) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.date = date;
        this.author = author;
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
