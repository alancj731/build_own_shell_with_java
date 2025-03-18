import java.io.FileWriter;
import java.io.IOException;

abstract class Writter {
    public abstract void write();

}

class StdWriter extends Writter {
    private String content;

    StdWriter(String content) {
        this.content = content;
    }

    public void write() {
        if (this.content == null || this.content.length() == 0) {
            return;
        }
        System.out.print(this.content);
    }
}

class ErrWriter extends Writter {
    private String content;

    ErrWriter(String content) {
        this.content = content;
    }

    public void write() {
        if (this.content == null || this.content.length() == 0) {
            return;
        }
        System.err.print(this.content);
    }
}

class FWriter extends Writter {
    private String content;
    private String fileName;

    FWriter(String content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public void write() {
        if (this.content == null || this.content.length() == 0) {
            return;
        }
        try (FileWriter writer = new FileWriter(this.fileName)) {
            writer.write(this.content);
        } catch (IOException e) {
        }
    }
}

class FAppender extends Writter {

    private String content;
    private String fileName;

    FAppender(String content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public void write() {
        if (this.content == null || this.content.length() == 0) {
            return;
        }
        try (FileWriter writer = new FileWriter(this.fileName, true)) {
            writer.append(this.content);
        } catch (IOException e) {
        }
    }
}