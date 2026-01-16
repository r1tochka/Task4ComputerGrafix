package modelviewer.io;

public class ObjReaderException extends Exception {
    private int lineNumber;

    public ObjReaderException(String message) {
        super(message);
        this.lineNumber = 0;
    }

    public ObjReaderException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getMessage() {
        if (lineNumber > 0) {
            return "Line " + lineNumber + ": " + super.getMessage();
        }
        return super.getMessage();
    }
}
