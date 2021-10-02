import java.io.Serializable;

public class Cell implements Serializable {

    public enum KindOfCell {BodyCell, HeadCell, FreeCell, VoidCell, HideCell}

    public int hDirection;
    public int vDirection;
    public KindOfCell kindOfCell;
    public int x;
    public int y;
    public static final int WIDTH_OF_CELL = 20;
    public static final int pDirection = 20;
    public static final int nDirection = -20;
    public static final int fDirection = 0;

    public Cell(int x, int y, KindOfCell kindOfCell, int hDirection, int vDirection) {

        manageThrows(x, y, kindOfCell, hDirection, vDirection);

        this.hDirection = hDirection;
        this.vDirection = vDirection;
        this.kindOfCell = kindOfCell;
        this.x = x;
        this.y = y;
    }

    public Cell(int x, int y, KindOfCell kindOfCell) {

        this(x, y, kindOfCell, fDirection, fDirection);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHDirection() {
        return hDirection;
    }

    public void setHDirection(int hDirection) {
        this.hDirection = hDirection;
    }

    public int getVDirection() {
        return vDirection;
    }

    public void setVDirection(int vDirection) {
        this.vDirection = vDirection;
    }

    private void manageThrows(int x, int y, KindOfCell kindOfCell, int hDirection, int vDirection) {

        if (kindOfCell != KindOfCell.BodyCell && kindOfCell != KindOfCell.FreeCell
                && kindOfCell != KindOfCell.HeadCell && kindOfCell != KindOfCell.VoidCell
                && kindOfCell != KindOfCell.HideCell)
            throw new IllegalArgumentException("Cell kind is wrong!");
        if (hDirection != pDirection && hDirection != nDirection && hDirection != 0)
            throw new IllegalArgumentException("Cell horizontal direction is wrong!");
        if (vDirection != pDirection && vDirection != nDirection && hDirection != 0)
            throw new IllegalArgumentException("Cell horizontal direction is wrong!");
        if (kindOfCell == KindOfCell.FreeCell && (hDirection != 0 || vDirection != 0))
            throw new IllegalArgumentException("FreeCell should be fix!");
        if (kindOfCell != KindOfCell.FreeCell && (hDirection != 0 && vDirection != 0))
            throw new IllegalArgumentException("Just freeCell should be fix!");
    }
}
