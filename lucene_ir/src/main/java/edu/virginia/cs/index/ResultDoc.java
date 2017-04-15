package edu.virginia.cs.index;

public class ResultDoc {
    private int id;
    private String title = "[no title]";
    private String content = "[no content]";

    public ResultDoc(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public String title() {
        return title;
    }

    public ResultDoc title(String nTitle) {
        title = nTitle;
        return this;
    }

    public String content() {
        return content;
    }

    public ResultDoc content(String nContent) {
        content = nContent;
        return this;
    }
}
