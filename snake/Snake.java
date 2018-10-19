import bridges.base.NamedColor;
import babybridges.game.NGCKGame;

class Snake extends NGCKGame {

	java.util.Random random = new java.util.Random();
	int gridColumns = grid.getDimensions()[0];
	int gridRows = grid.getDimensions()[1];

	final long FRAMERATE = 1000000000 / 15;
	int startX = gridColumns/3;
	int startY = gridRows/2;
	int startLength = 3;

	long frameTime;
	long nextFrameTime;
	Block head;
	Block tail;
	Direction dir;

	Block apple;

	Direction lastDir;

	NamedColor bg = NamedColor.forestgreen;
	NamedColor bc = NamedColor.green;
	NamedColor fg = NamedColor.silver;
	NamedColor hc = NamedColor.white;
	NamedColor ac = NamedColor.red;

	public Snake(int assid, String login, String apiKey) {
		super(assid, login, apiKey);


	}

	public void handleInput() {
		if (KeyLeft() && dir != Direction.EAST && lastDir != Direction.EAST) {
			dir = Direction.WEST;
		} else if (KeyUp() && dir != Direction.SOUTH && lastDir != Direction.SOUTH) {
			dir = Direction.NORTH;
		} else if (KeyDown() && dir != Direction.NORTH && lastDir != Direction.NORTH) {
			dir = Direction.SOUTH;
		} else if (KeyRight() && dir != Direction.WEST && lastDir != Direction.WEST) {
			dir = Direction.EAST;
		}
	}

	public void updatePosition() {
	
		Block current = head.next;
		int nextX = head.x;
		int nextY = head.y;
		Block nextPos = head;

		while (current != null) {
			int tempX = current.x;
			int tempY = current.y;
			current.x = nextX;
			current.y = nextY;
			nextX = tempX;
			nextY = tempY;
			current = current.next;
		}
		
		switch(dir) {
			case NORTH:
				head.y--;
				if (head.y < 0)
					head.y = gridRows - 1;
				break;

			case SOUTH:
				head.y++;
				if (head.y == gridRows)
					head.y = 0;
				break;

			case EAST:
				head.x++;
				if (head.x == gridColumns)
					head.x = 0;
				break;

			case WEST:
				head.x--;
				if (head.x < 0)
					head.x = gridColumns - 1;
				break;
		}
	}

	public void plantApple() {
		int x;
		int y;
		while (true) {
			x = Math.abs(random.nextInt() % 29);
			y = Math.abs(random.nextInt() % 29);

			boolean noCollision = true;

			Block current = head;
			while (current != null) {
				if (current.x == x && current.y == y) {
					noCollision = false;
					break;
				}
				current = current.next;
			}
			if (noCollision)
				break;
		}
		apple.x = x;
		apple.y = y;
	}

	public void detectApple() {
		if (head.x == apple.x &&
			head.y == apple.y) {
			Block newTail = new Block(tail.x, tail.y);
			tail.next = newTail;
			tail = newTail;
			plantApple();
			}
	}

	public void detectDeath() {
		Block current = head.next;
		while (current != null) {
			if (head.x == current.x &&
				head.y == current.y)
				System.exit(0);
			current = current.next;
		}
	}

	public void paint() {
		for (int i = 0; i < gridColumns; ++i) {
			for (int j = 0; j < gridRows; ++j) {
				if (i % 2 == j % 2)
					SetBGColor(i, j, bg);
				else
					SetBGColor(i, j, bc);
			}
		}
		
		SetBGColor(head.x, head.y, hc);
		SetBGColor(apple.x, apple.y, ac);

		Block current = head.next;;
		while (current != null) {
			SetBGColor(current.x, current.y, fg);
			current = current.next;
		}
	}

	// Set up the first state of the game grid
	public void initialize(){ 

		for (int i = 0; i < gridColumns; ++i) {
			for (int j = 0; j < gridRows; ++j) {
				if (i % 2 == j % 2)
					SetBGColor(i, j, bg);
				else
					SetBGColor(i, j, bc);
			}
		}

		head = new Block(startX, startY);
		Block current = head;

		for (int i = 0; i < startLength; ++i) {
			SetBGColor(startX - i, startY, fg);
			if (i > 0) {
				current.next = new Block(startX - i, startY);
				current = current.next;
			}
		}
		tail = current;

		frameTime = System.nanoTime();
		nextFrameTime = frameTime + FRAMERATE;
		dir = Direction.EAST;
		lastDir = dir;
		apple = new Block();
		plantApple();
	}
	
	// Game loop will run many times per second.
	public void GameLoop() {
		
		handleInput();
		if (System.nanoTime() > nextFrameTime) {
			frameTime = System.nanoTime();
			nextFrameTime = frameTime + FRAMERATE;

			lastDir = dir;

			detectApple();

			updatePosition();

			paint();

			detectDeath();

		}
	}

	public static void main(String args[]) {
		Snake game = new Snake(21, "Goncharow", "1198800260219");
		game.setTitle("snake");

		game.start();
	}
}

enum Direction {
	NORTH, 
	SOUTH,
	EAST,
	WEST
}

class Block {
	public Block next;
	public int x;
	public int y;

	public Block() {
		this(-1, -1, null);
	}

	public Block(int x, int y) {
		this(x, y, null);
	}

	public Block(int x, int y, Block next) {
		this.x = x;
		this.y = y;
		this.next = next;
	}
}
