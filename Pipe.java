import java.awt.Image;

public class Pipe {
    int x, y, width, height;
    Image img;
    boolean passed = false;

    public static final int PIPE_WIDTH = 64;
    public static final int PIPE_HEIGHT = 512;

    public Pipe(Image img, int x, int y) {
        this.img = img;
        this.x = x;
        this.y = y;
        this.width = PIPE_WIDTH;
        this.height = PIPE_HEIGHT;
    }
}
