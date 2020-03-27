import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConnectFourClickListener extends MouseAdapter {

    private static final short LO_BOUNDS = 0;
    private static final short HI_BOUNDS = 7;

    private int pixelClicked;
    private ConnectFour component;


    @Override
    public void mouseClicked(MouseEvent e) {
        pixelClicked = e.getX();
        int col = pixelClicked / 100;
        if (col <= HI_BOUNDS && col >= LO_BOUNDS) {
            component.setCol(col);
            component.repaint();
        }
    }

    public int getPixel() {
        return pixelClicked;
    }



    public void setComponent(ConnectFour game) {
        component = game;
    }
}

