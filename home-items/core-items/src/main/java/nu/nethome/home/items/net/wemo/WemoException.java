package nu.nethome.home.items.net.wemo;

public class WemoException extends Throwable {
    public WemoException(Exception e) {
        super(e);
    }

    public WemoException(String text) {
        super(text);
    }
}
