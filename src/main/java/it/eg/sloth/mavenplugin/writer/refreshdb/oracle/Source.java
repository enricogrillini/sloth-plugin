package it.eg.sloth.mavenplugin.writer.refreshdb.oracle;

public class Source {

    String name;
    StringBuilder source;

    public Source(String name, String type) {
        this.name = name;
        this.source = new StringBuilder();
    }

    public String getName() {
        return name;
    }

    public void append(String text) {
        source.append(text);
    }

    public String getSource() {
        return source.toString();
    }
}