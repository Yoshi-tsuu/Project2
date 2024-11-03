import java.awt.Image;

public class Bird {
    int x, y, width, height;
    Image img;

    public Bird(Image img) {
        this.img = img;
        this.width = 34;
        this.height = 24;
        resetPosition();
    }

    public void resetPosition() {
        this.x = 360 / 8;
        this.y = 640 / 2;
    }
}
