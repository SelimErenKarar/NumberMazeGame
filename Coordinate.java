
public class Coordinate {
	private int x;
	private int y;
	private Coordinate previous;
	private int value;
	
	public Coordinate(int x, int y, Coordinate previous, int value) {
		this.x = x;
		this.y = y;
		this.previous = previous;
		this.value = value;
	}

	public Coordinate(int x, int y, int  value) {
		this.x = x;
		this.y = y;
		this.value = value;
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

	public Coordinate getPrevious() {
		return previous;
	}
	public void setPrevious(Coordinate previous) {
		this.previous = previous;
	}
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
}
