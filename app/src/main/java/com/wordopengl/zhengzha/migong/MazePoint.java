package com.wordopengl.zhengzha.migong;

import java.util.*;   
import java.lang.*;   
public class MazePoint {  
    private boolean isVisited = false;  
    private boolean wallUp = true;  
    private boolean wallRight = true;  
    private boolean wallDown = true;  
    private boolean wallLeft = true;  

    public boolean isVisited() {  
        return isVisited;  
    }  
    public void setVisited(boolean isVisited) {  
        this.isVisited = isVisited;  
    }  
    public boolean isWallUp() {  
        return wallUp;  
    }  
    public void setWallUp(boolean wallUp) {  
        this.wallUp = wallUp;  
    }  
    public boolean isWallRight() {  
        return wallRight;  
    }  
    public void setWallRight(boolean wallRight) {  
        this.wallRight = wallRight;  
    }  
    public boolean isWallDown() {  
        return wallDown;  
    }  
    public void setWallDown(boolean wallDown) {  
        this.wallDown = wallDown;  
    }  
    public boolean isWallLeft() {  
        return wallLeft;  
    }  
    public void setWallLeft(boolean wallLeft) {  
        this.wallLeft = wallLeft;  
    }  
}  
