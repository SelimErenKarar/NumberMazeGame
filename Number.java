public class Number {
    private int value;
    private String color;
    private int factor;
    private boolean moved;

    public Number(int value) {
        this.value = value;
        if(value == 1 || value == 2 || value == 3) {
            color = "green";
            factor = 1;
        }
        else if (value == 4 || value == 5 || value == 6) {
            color = "yellow";
            factor = 5;
        }
        else if (value == 7 || value == 8 || value == 9) {
            color = "red";
            factor = 25;
        }
        this.moved = false;
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public int getFactor() {
        return factor;
    }
    public void setFactor(int factor) {
        this.factor = factor;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }
}