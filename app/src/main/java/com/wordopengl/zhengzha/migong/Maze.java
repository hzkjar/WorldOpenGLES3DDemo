package com.wordopengl.zhengzha.migong;


import java.util.Random;  
import java.util.Stack;  



/*

算法来自
 https://blog.csdn.net/yui/article/details/5916716


*/
public class Maze {  
    private final static int dirUp = 0;  
    private final static int dirRight = 1;  
    private final static int dirDown = 2;  
    private final static int dirLeft = 3;  

    private final static int gridWall = 1;  
    private final static int gridEmpty = 0;  
    private final static int gridBlind = -1;  
    private final static int gridPath = 2;  

    private int width;  
    private int height;  
    private MazePoint[][] matrix;  
    private int[][] maze;  

    /* 
     * constructor, initial width, height and matrix 
     */  
    public Maze(int width, int height) {  
        this.width = width;  
        this.height = height;  
        this.matrix = new MazePoint[height][width];  
        for (int i=0; i<height; i++)  
            for (int j=0; j<width; j++)  
                matrix[i][j] = new MazePoint();  
        this.maze = new int[2*height+1][2*width+1];  
    }

	public void setMaze(int[][] maze)
	{
		this.maze = maze;
	}

	public int[][] getMaze()
	{
		return maze;
	}  

    /* 
     * check if the target neighbor can be visited 
     * if the target point is out of bounds, treat it as already visited 
     */  
    public boolean isNeighborOK(int x, int y, int dir) {  
        boolean isNeighborVisited = false;  
        switch ( dir ) {  
			case dirUp:  
				if ( x <= 0 )  
					isNeighborVisited = true;  
				else  
					isNeighborVisited = matrix[x-1][y].isVisited();  
				break;  
			case dirRight:  
				if ( y >= width - 1 )  
					isNeighborVisited = true;  
				else  
					isNeighborVisited = matrix[x][y+1].isVisited();  
				break;  
			case dirDown:  
				if ( x >= height - 1 )  
					isNeighborVisited = true;  
				else  
					isNeighborVisited = matrix[x+1][y].isVisited();  
				break;  
			case dirLeft:  
				if ( y <= 0 )  
					isNeighborVisited = true;  
				else  
					isNeighborVisited = matrix[x][y-1].isVisited();  
				break;  
        }  
        return !isNeighborVisited;  
    }  

    /* 
     * check if the neighbors have at least one non-visited point 
     */  
    public boolean isNeighborOK(int x, int y) {  
        return (this.isNeighborOK(x, y, dirUp) || this.isNeighborOK(x, y, dirRight) ||  
			this.isNeighborOK(x, y, dirDown) || this.isNeighborOK(x, y, dirLeft));  
    }  

    /* 
     * pick up a random traversal direction 
     * loop until find a correct one 
     */  
    public int getRandomDir(int x, int y) {  
        int dir = -1;  
        Random rand = new Random();  
        if ( isNeighborOK(x, y) ) {  
            do {  
                dir = rand.nextInt(4);  
            } while ( !isNeighborOK(x, y, dir) );  
        }  
        return dir;  
    }  

    /* 
     * push down the wall between the adjacent two points 
     */  
    public void pushWall(int x, int y, int dir) {  
        switch ( dir ) {  
			case dirUp:  
				matrix[x][y].setWallUp(false);  
				matrix[x-1][y].setWallDown(false);  
				break;  
			case dirRight:  
				matrix[x][y].setWallRight(false);  
				matrix[x][y+1].setWallLeft(false);  
				break;  
			case dirDown:  
				matrix[x][y].setWallDown(false);  
				matrix[x+1][y].setWallUp(false);  
				break;  
			case dirLeft:  
				matrix[x][y].setWallLeft(false);  
				matrix[x][y-1].setWallRight(false);  
				break;  
        }  
    }  

    /* 
     * depth first search traversal 
     */  
    public void traversal() {  
        int x = 0;  
        int y = 0;  
        Stack<Integer> stackX = new Stack<Integer>();  
        Stack<Integer> stackY = new Stack<Integer>();  
        do {  
            MazePoint p = matrix[x][y];  
            if ( !p.isVisited() ) {  
                p.setVisited(true);  
            }  
            if ( isNeighborOK(x, y) ) {  
                int dir = this.getRandomDir(x, y);  
                this.pushWall(x, y, dir);  
                stackX.add(x);  
                stackY.add(y);  
                switch ( dir ) {  
					case dirUp:  
						x--;  
						break;  
					case dirRight:  
						y++;  
						break;  
					case dirDown:  
						x++;  
						break;  
					case dirLeft:  
						y--;  
						break;  
                }  
            }  
            else {  
                x = stackX.pop();  
                y = stackY.pop();  
            }  
        } while ( !stackX.isEmpty() );  
    }  

    /* 
     * create the maze by the point matrix 
     * only use the right wall and down wall of every point 
     */  
    public void create() {  
        for (int j=0; j<2*width+1; j++)  
            maze[0][j] = gridWall;  
        for (int i=0; i<height; i++) {  
            maze[2*i+1][0] = gridWall;  
            for (int j=0; j<width; j++) {  
                maze[2*i+1][2*j+1] = gridEmpty;  
                if ( matrix[i][j].isWallRight() )  
                    maze[2*i+1][2*j+2] = gridWall;  
                else  
                    maze[2*i+1][2*j+2] = gridEmpty;  
            }  
            maze[2*i+2][0] = 1;  
            for (int j=0; j<width; j++) {  
                if ( matrix[i][j].isWallDown() )  
                    maze[2*i+2][2*j+1] = gridWall;  
                else  
                    maze[2*i+2][2*j+1] = gridEmpty;  
                maze[2*i+2][2*j+2] = gridWall;  
            }  
        }  
    }  

    /* 
     * print the matrix 
     */  
    public void print() {  
        for (int i=0; i<2*height+1; i++) {  
            for (int j=0; j<2*width+1; j++)  
                if ( maze[i][j] == gridWall )  
                    System.out.print("W");  
                else if ( maze[i][j] == gridPath )  
                    System.out.print(".");  
                else  
                    System.out.print(" ");  
            System.out.println();  
        }  
    }  

    /* 
     * in the maze array, try to find a break out direction 
     */  
    public int getBreakOutDir(int x, int y) {  
        int dir = -1;  
        if ( maze[x][y+1] == 0 )  
            dir = dirRight;  
        else if ( maze[x+1][y] == 0 )  
            dir = dirDown;  
        else if ( maze[x][y-1] == 0 )  
            dir = dirLeft;  
        else if ( maze[x-1][y] == 0 )  
            dir = dirUp;  
        return dir;  
    }  

    /* 
     * find the path from (1, 1) to (2*height-1, 2*width-1) 
     */  
    public void findPath() {  
        int x = 1;  
        int y = 1;  
        Stack<Integer> stackX = new Stack<Integer>();  
        Stack<Integer> stackY = new Stack<Integer>();  
        do {  
            int dir = this.getBreakOutDir(x, y);  
            if ( dir == -1 ) {  
                maze[x][y] = gridBlind;  
                x = stackX.pop();  
                y = stackY.pop();  
            }  
            else {  
                maze[x][y] = gridPath;  
                stackX.add(x);  
                stackY.add(y);  
                switch ( dir ) {  
					case dirUp:  
						x--;  
						break;  
					case dirRight:  
						y++;  
						break;  
					case dirDown:  
						x++;  
						break;  
					case dirLeft:  
						y--;  
						break;  
                }  
            }  
        } while ( !(x == 2*height-1 && y == 2*width-1) );  
        maze[x][y] = gridPath;  
    }  

    /* 
     * remove all foot print in the maze 
     */  
    public void reset() {  
        for (int i=0; i<2*height+1; i++)  
            for (int j=0; j<2*width+1; j++)  
                if ( maze[i][j] != gridWall )  
                    maze[i][j] = gridEmpty;  
    }  

    
}  
