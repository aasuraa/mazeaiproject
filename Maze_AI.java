package mazeaiproject;
 
import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
  
public class Maze_AI {
 
    public static JFrame mazeFrame;  // The main form of the program
   
    public static void main(String[] args) {
        int width  = 700;
        int height = 570;
        mazeFrame = new JFrame("Lorina's and Sagar's Maze");
        
        MazePanel initialMaze = new MazePanel(width, height);
        mazeFrame.setContentPane(initialMaze);
        initialMaze.initializeGrid(Boolean.TRUE);
        mazeFrame.getContentPane().setBackground(Color.pink);
        //The pack method packs the components within the window based on the component’s preferred sizes.
        mazeFrame.pack();
        mazeFrame.setResizable(false);
 
        // the form is located in the center of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double ScreenHeight = screenSize.getHeight();
        int x = ((int)screenWidth-width)/2;
        int y = ((int)ScreenHeight-height)/2;
 
        mazeFrame.setLocation(x,y);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);
    } 
         

    /* The JPanel class resides in the package javax.swing so we have to import this package.
     * The "MazePanel" class will contain all the functionality of the program. 
     */
    public static class MazePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
       //CONSTANTS 
        private final static int
            EMPTY    = 0,  // empty cell
            OBST     = 1,  // cell with obstacle
            ROBOT    = 2,  // the position of the robot
            TARGET   = 3,  // the position of the target
            FRONTIER = 4,  // cells that form the frontier (OPEN SET)
            CLOSED   = 5,  // cells that form the CLOSED SET
            ROUTE    = 6;  // cells that form the robot-to-target path
         
        // Messages to the user
        private final static String
            msgLegend = " ROBOT= Red | TARGET= Green | FRONTIER= Blue | CLOSED SET= L.B.",
            msgNoSolution =
                "There is no path to the target !!!" ;
        
        String msgDfs = msgLegend, msgBfs, msgAStar;
 
        //variables of the class MazePanel
         
        int rows    = 41,           // the number of rows of the grid
            columns = 41,           // the number of columns of the grid
            squareSize = 500/rows;  // the cell size in pixels
         
 
        ArrayList<Cell> openSet   = new ArrayList();// the OPEN SET
        ArrayList<Cell> closedSet = new ArrayList();// the CLOSED SET
        
          
        Cell robotStart; // the initial position of the robot
        Cell targetPos;  // the position of the target
       
        JLabel message1;  // message to the user
        JLabel message2;
        JLabel message3;
        // buttons for selecting the algorithm
        JRadioButton dfs, bfs, aStar;
         
       
 
        int[][] grid;        // the grid
        boolean found;       // flag that the goal was found
        boolean searching;   // flag that the search is in progress
        boolean endOfSearch; // flag that the search came to an end
        int delay = 50;           // time delay of animation (in msec)
        int expanded;        // the number of nodes that have been expanded
        long startTime, endTime, totalTime;
        double timesec; // total time of execution when Animation is selected
        
         
        // the object that controls the animation
        RepaintAction action = new RepaintAction();
         
        // the Timer which governs the execution speed of the animation
        Timer timer;
      

        /**
         * The creator of the panel
         * @param width  the width of the panel.
         * @param height the height of the panel.
         */
        public MazePanel(int width, int height) {
       
            setLayout(null);
             
            MouseHandler listener = new MouseHandler();
            addMouseListener(listener);
            addMouseMotionListener(listener);
 
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.red));
 
            setPreferredSize( new Dimension(width,height) );
 
            grid = new int[rows][columns];
 
            // We create the contents of the panel
            
            message1 = new JLabel(msgDfs, JLabel.CENTER);
            message1.setForeground(Color.black);
            message1.setFont(new Font("Helvetica",Font.PLAIN,14));
 
            message2 = new JLabel(msgBfs, JLabel.CENTER);
            message2.setForeground(Color.black);
            message2.setFont(new Font("Helvetica",Font.PLAIN,14)); 
            
            message3 = new JLabel(msgAStar, JLabel.CENTER);
            message3.setForeground(Color.black);
            message3.setFont(new Font("Helvetica",Font.PLAIN,14));
            
            JButton mazeButton = new JButton("Maze");
            mazeButton.addActionListener(new ActionHandler());
            mazeButton.setBackground(Color.lightGray);
            mazeButton.setToolTipText
                    ("Creates a random maze");
            mazeButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    mazeButtonActionPerformed(evt);
                }
            });
 
            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionHandler());
            clearButton.setBackground(Color.lightGray);
            clearButton.setToolTipText
                    ("First click: clears search, Second click: clears obstacles");
 
            JButton stepButton = new JButton("Step-by-Step");
            stepButton.addActionListener(new ActionHandler());
            stepButton.setBackground(Color.lightGray);
            stepButton.setToolTipText
                    ("The search is performed step-by-step for every click");
 
            JButton animationButton = new JButton("Animation");
            animationButton.addActionListener(new ActionHandler());
            animationButton.setBackground(Color.lightGray);
            animationButton.setToolTipText
                    ("The search is performed automatically");
 
                                                
            // ButtonGroup that synchronizes the three RadioButtons
            // choosing the algorithm, so that only one
            // can be selected anytime
            ButtonGroup algoGroup = new ButtonGroup();
            
 
            dfs = new JRadioButton("DFS");
            dfs.setToolTipText("Depth First Search algorithm");
            algoGroup.add(dfs);
            dfs.addActionListener(new ActionHandler());
 
            bfs = new JRadioButton("BFS");
            bfs.setToolTipText("Breadth First Search algorithm");
            algoGroup.add(bfs);
            bfs.addActionListener(new ActionHandler());
 
            aStar = new JRadioButton("A*");
            aStar.setToolTipText("A* algorithm");
            algoGroup.add(aStar);
            aStar.addActionListener(new ActionHandler());
 
           
 
            JPanel algoPanel = new JPanel();
            algoPanel.setBorder(javax.swing.BorderFactory.
                    createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
                    "Algorithms", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.TOP, new java.awt.Font("Helvetica", 0, 14)));
            
             
            dfs.setSelected(true);  // DFS is initially selected 
             
            // we add the contents of the panel
            add(message1);
            add(message2);
            add(message3);
            
            add(mazeButton);
            add(clearButton);
            add(stepButton);
            add(animationButton);
           
            add(dfs);
            add(bfs);
            add(aStar);
            
            add(algoPanel);
            
            
            // we regulate the sizes and positions
            message1.setBounds(0, 510, 500, 20);
            message2.setBounds(0, 530, 500, 20);
            message3.setBounds(0, 550, 500, 20);
           
          
            mazeButton.setBounds(520, 95, 170, 25);
            clearButton.setBounds(520, 125, 170, 25);
            stepButton.setBounds(520, 155, 170, 25);
            animationButton.setBounds(520, 185, 170, 25);
           
            dfs.setBounds(530, 270, 70, 25);
            bfs.setBounds(600, 270, 70, 25);
            aStar.setBounds(530, 295, 70, 25);
            
            algoPanel.setLocation(520,250);
            algoPanel.setSize(170, 100);
            
           
            // we create the timer
            timer = new Timer(delay, action);
             
            // We attach to cells in the grid initial values.
            // Here is the first step of the algorithms
            fillGrid();
 
        } // end constructor
               
        /**
         * Function executed if the user presses the button "Maze"
         */
        private void mazeButtonActionPerformed(java.awt.event.ActionEvent evt) {
            initializeGrid(true);
        } // end mazeButtonActionPerformed()
     
        /**
         * Creates a new clean grid or a new maze
         */
        private void initializeGrid(Boolean makeMaze) {                                           
            
            squareSize = 500/(rows > columns ? rows : columns);
            
            grid = new int[rows][columns];
            
            // initial positions of target and robot
            robotStart = new Cell(rows-2,1);
            targetPos = new Cell(1,columns-2);
            dfs.setEnabled(true);
            dfs.setSelected(true);
            bfs.setEnabled(true);
            aStar.setEnabled(true);
           
           
            if (makeMaze) {
                MyMaze maze = new MyMaze(rows/2,columns/2);
            } else {
                fillGrid();
            }
        } // end initializeGrid()
                     
        /**
         * Gives initial values ​​for the cells in the grid.
         * With the first click on button 'Clear' clears the data
         * of any search was performed (Frontier, Closed Set, Route)
         * and leaves intact the obstacles and the robot and target positions
         * in order to be able to run another algorithm
         * with the same data.
         * With the second click removes any obstacles also.
         */
        private void fillGrid() {
            if (searching || endOfSearch){
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < columns; c++) {
                        if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE) {
                            grid[r][c] = EMPTY;
                        }
                        if (grid[r][c] == ROBOT){
                            robotStart = new Cell(r,c);
                        }
                        if (grid[r][c] == TARGET){
                            targetPos = new Cell(r,c);
                        }
                    }
                }
                searching = false;
            } else {
                message1.setText(msgLegend);
                message2.setText("");
                message3.setText("");
                      
                robotStart = new Cell(rows-2,1);
                targetPos = new Cell(1,columns-2);
            }
            if (aStar.isSelected()){
                robotStart.g = 0;
                robotStart.h = 0;
                robotStart.f = 0;
            }
            expanded = 0;
            found = false;
            searching = false;
            endOfSearch = false;
          
            // The first step of the algorithms is here
            // 1. OPEN SET: = [So], CLOSED SET: = []
            openSet.removeAll(openSet);
            openSet.add(robotStart);
            closedSet.removeAll(closedSet);
          
            grid[targetPos.row][targetPos.col] = TARGET; 
            grid[robotStart.row][robotStart.col] = ROBOT;
           
            timer.stop();
            repaint();
             
        } // end fillGrid()


        /* 
         * The "Cell" class will define all the characteristics of a cell.         
         */
        private class Cell {
            int row;   // the row number of the cell(row 0 is the top)
            int col;   // the column number of the cell (Column 0 is the left)
            int g;     // the value of the function g of A* algorithm (actual distance traveled)
            int h;     // the value of the function h of A* algorithm (remaining distance traveled)
            int f;     // the value of the function f of A* algorithm (g+h)
                       
            Cell prev; // Each state corresponds to a cell
                       // and each state has a predecessor which
                       // is stored in this variable
             
            public Cell(int row, int col){
               this.row = row;
               this.col = col;
            }
        } // end class Cell
       
        /**
         * We are going to use Java Comparator interface to order the objects of 
         * "Cell" class according their 'f' field.
         * This interface is found in java.util package so we have to import this package
         */
        private class CellComparatorByF implements Comparator<Cell>{
            //Comparator interface contains 2 methods compare(Object obj1,Object obj2) 
            //and equals(Object element).
            @Override
            public int compare(Cell cell1, Cell cell2){
                return cell1.f-cell2.f;
            }
        } // end class CellComparatorByF
       
             
       
        // Add/remove obstacles or move the robot/target
         
        private class MouseHandler implements MouseListener, MouseMotionListener {
            private int cur_row, cur_col, cur_val;
            @Override
            public void mousePressed(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found) {
                    cur_row = row;
                    cur_col = col;
                    cur_val = grid[row][col];
                    if (cur_val == EMPTY){
                        grid[row][col] = OBST;
                    }
                    if (cur_val == OBST){
                        grid[row][col] = EMPTY;
                    }
                }
                repaint();
            }
 
            @Override
            public void mouseDragged(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns && !searching && !found){
                    if ((row*columns+col != cur_row*columns+cur_col) && (cur_val == ROBOT || cur_val == TARGET)){
                        int new_val = grid[row][col];
                        if (new_val == EMPTY){
                            grid[row][col] = cur_val;
                            if (cur_val == ROBOT) {
                                robotStart.row = row;
                                robotStart.col = col;
                            } else {
                                targetPos.row = row;
                                targetPos.col = col;
                            }
                            grid[cur_row][cur_col] = new_val;
                            cur_row = row;
                            cur_col = col;
                            if (cur_val == ROBOT) {
                                robotStart.row = cur_row;
                                robotStart.col = cur_col;
                            } else {
                                targetPos.row = cur_row;
                                targetPos.col = cur_col;
                            }
                            cur_val = grid[row][col];
                        }
                    } 
                }
                repaint();
            }
 
            @Override
            public void mouseReleased(MouseEvent evt) { }
            @Override
            public void mouseEntered(MouseEvent evt) { }
            @Override
            public void mouseExited(MouseEvent evt) { }
            @Override
            public void mouseMoved(MouseEvent evt) { }
            @Override
            public void mouseClicked(MouseEvent evt) { }
             
        } // end nested class MouseHandler
         
        /**
         * When the user presses a button performs the corresponding functionality
         */
        private class ActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String cmd = evt.getActionCommand();
                if (cmd.equals("Clear")) {
                    fillGrid();
                    dfs.setEnabled(true);
                    bfs.setEnabled(true);
                    aStar.setEnabled(true);
                                       
                   
                } else if (cmd.equals("Step-by-Step") && !found && !endOfSearch) {
                    
                    searching = true;                   
                    dfs.setEnabled(false);
                    bfs.setEnabled(false);
                    aStar.setEnabled(false);
                    
                  
                    timer.stop();
                    // Here we decide whether we can continue the
                    // 'Step-by-Step' search or not.
                    
                    // Here we have the second step of all 3 algorithms:
                    // 2. If OPEN SET = [], then terminate. There is no solution.
                    if (  openSet.isEmpty()) {
                        endOfSearch = true;
                        grid[robotStart.row][robotStart.col]=ROBOT;
                        message1.setText(msgNoSolution);
                    } else {
                        expandNode();
                        if (found) {
                            endOfSearch = true;
                            plotRoute();
                        }
                    }
                    repaint();
                } else if (cmd.equals("Animation") && !endOfSearch) {
                   
                    searching = true;                    
                    dfs.setEnabled(false);
                    bfs.setEnabled(false);
                    aStar.setEnabled(false);
                   
                    timer.setDelay(delay);
                    timer.start();
                    startTime = System.nanoTime();
                }
            }
        } // end nested class ActionHandler
    
        /**
         * The class that is responsible for the animation
         */
        private class RepaintAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                // Here we decide whether we can continue or not
                // the search with 'Animation'.
                
                // Here we have the second step for all 3 algorithms:
                // 2. If OPEN SET = [], then terminate. There is no solution.
                if (  openSet.isEmpty()) {
                    endOfSearch = true;
                    endTime   = System.nanoTime();
                    totalTime = endTime - startTime;
                    timesec=TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS) / 1000.0;
                    grid[robotStart.row][robotStart.col]=ROBOT;
                    if(dfs.isSelected() || bfs.isSelected()){
                        message1.setText(msgNoSolution);
                    } else {
                        message1.setText("No shorter Path!");
                    }
                } else {
                    /**
                     * For dfs and bfs the search can end when a path is found
                     * bfs will actually find the optimal path because in a maze the, if the goal exists, nodes point towards it                     
                     */
                        expandNode();
                        if(found){
                            if(dfs.isSelected() || bfs.isSelected()){
                                timer.stop();
                            }
                            endTime   = System.nanoTime();
                            totalTime = endTime - startTime;
                            timesec=TimeUnit.MILLISECONDS.convert(totalTime, TimeUnit.NANOSECONDS) / 1000.0;
                            endOfSearch = true;
                            plotRoute();
                            
                            //AStar has to still look for optimal and stop is distance travelled is more than found distance
                            if(aStar.isSelected()){
                                if(openSet.isEmpty()){
                                    timer.stop();
                                    message1.setText("No shorter path found!");
                                } else {
                                    try{
                                        calculateDistance();
                                    } catch(Exception e){}
                                }
                            }
                        }
                    
                }
                repaint();
            }
        } // end nested class RepaintAction
       
       
 
        /**
         * Creates a random maze
         * 
         * The code of the class is an adaptation to a question posted by user nazar_art at stackoverflow.com:
         * http://stackoverflow.com/questions/18396364/maze-generation-arrayindexoutofboundsexception
         */
        private class MyMaze {
            private int dimensionX, dimensionY; // dimension of maze
            private int gridDimensionX, gridDimensionY; // dimension of output grid
            private char[][] mazeGrid; // output grid
            private Cell[][] cells; // 2d array of Cells
            private Random random = new Random(); // The random object
 
            // initialize with x and y the same
            public MyMaze(int aDimension) {
                // Initialize
                this(aDimension, aDimension);
            }
            // constructor
            public MyMaze(int xDimension, int yDimension) {
                dimensionX = xDimension;
                dimensionY = yDimension;
                gridDimensionX = xDimension * 2 + 1;
                gridDimensionY = yDimension * 2 + 1;
                mazeGrid = new char[gridDimensionX][gridDimensionY];
                init();
                generateMaze();
            }
 
            private void init() {
                // create cells
                cells = new Cell[dimensionX][dimensionY];
                for (int x = 0; x < dimensionX; x++) {
                    for (int y = 0; y < dimensionY; y++) {
                        cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
                    }
                }
            }
 
            // inner class to represent a cell
            private class Cell {
                int x, y; // coordinates
                // cells this cell is connected to
                ArrayList<Cell> neighbors = new ArrayList<>();
                // impassable cell
                boolean wall = true;
                // if true, has yet to be used in generation
                boolean open = true;
                // construct Cell at x, y
                Cell(int x, int y) {
                    this(x, y, true);
                }
                // construct Cell at x, y and with whether it isWall
                Cell(int x, int y, boolean isWall) {
                    this.x = x;
                    this.y = y;
                    this.wall = isWall;
                }
                // add a neighbor to this cell, and this cell as a neighbor to the other
                void addNeighbor(Cell other) {
                    if (!this.neighbors.contains(other)) { // avoid duplicates
                        this.neighbors.add(other);
                    }
                    if (!other.neighbors.contains(this)) { // avoid duplicates
                        other.neighbors.add(this);
                    }
                }
                // used in updateGrid()
                boolean isCellBelowNeighbor() {
                    return this.neighbors.contains(new Cell(this.x, this.y + 1));
                }
                // used in updateGrid()
                boolean isCellRightNeighbor() {
                    return this.neighbors.contains(new Cell(this.x + 1, this.y));
                }
                // useful Cell equivalence
                @Override
                public boolean equals(Object other) {
                    if (!(other instanceof Cell)) {
                        return false;
                    }
                    Cell otherCell = (Cell) other;
                    return (this.x == otherCell.x && this.y == otherCell.y);
                }
 
                // should be overridden with equals
                @Override
                public int hashCode() {
                    // random hash code method designed to be usually unique
                    return this.x + this.y * 256;
                }
 
            }
            // generate from upper left (In computing the y increases down often)
            private void generateMaze() {
                generateMaze(0, 0);
            }
            // generate the maze from coordinates x, y
            private void generateMaze(int x, int y) {
                generateMaze(getCell(x, y)); // generate from Cell
            }
            private void generateMaze(Cell startAt) {
                // don't generate from cell not there
                if (startAt == null) {
                    return;
                }
                startAt.open = false; // indicate cell closed for generation
                ArrayList<Cell> cellsList = new ArrayList<>();
                cellsList.add(startAt);
 
                while (!cellsList.isEmpty()) {
                    Cell cell;
                    // this is to reduce but not completely eliminate the number
                    // of long twisting halls with short easy to detect branches
                    // which results in easy mazes
                    switch ( random.nextInt(10) )
                    {
                        case 0:
                            cell = cellsList.remove(random.nextInt(cellsList.size()));  //remove random value of within the size
                            break;
                        default:
                            cell = cellsList.remove(cellsList.size() - 1);
                            break;
                    }
                    // for collection
                    ArrayList<Cell> neighbors = new ArrayList<>();
                    // cells that could potentially be neighbors
                    Cell[] potentialNeighbors = new Cell[]{
                        getCell(cell.x + 1, cell.y),    //right
                        getCell(cell.x, cell.y + 1),    //down
                        getCell(cell.x - 1, cell.y),    //left
                        getCell(cell.x, cell.y - 1)     //up
                    };
                    for (Cell other : potentialNeighbors) {
                        // skip if outside, is a wall or is not opened
                        if (other==null || other.wall || !other.open) {
                            continue;
                        }
                        neighbors.add(other);
                    }
                    if (neighbors.isEmpty()) {
                        continue;
                    }
                    // get random cell
                    Cell selected = neighbors.get(random.nextInt(neighbors.size()));
                    // add as neighbor
                    selected.open = false; // indicate cell closed for generation
                    cell.addNeighbor(selected);
                    cellsList.add(cell);
                    cellsList.add(selected);
                }
                updateGrid();
            }
            // used to get a Cell at x, y; returns null out of bounds
            public Cell getCell(int x, int y) {
                try {
                    return cells[x][y];
                } catch (ArrayIndexOutOfBoundsException e) { // catch out of bounds
                    return null;
                }
            }
            // draw the maze
            public void updateGrid() {
                char backChar = ' ', wallChar = 'X', cellChar = ' ';
                // fill background
                for (int x = 0; x < gridDimensionX; x ++) {
                    for (int y = 0; y < gridDimensionY; y ++) {
                        mazeGrid[x][y] = backChar;
                    }
                }
                // build walls
                for (int x = 0; x < gridDimensionX; x ++) {
                    for (int y = 0; y < gridDimensionY; y ++) {
                        if (x % 2 == 0 || y % 2 == 0) {
                            mazeGrid[x][y] = wallChar;
                        }
                        if(x % 7 == 0 && y % 9 == 0){
                            mazeGrid[x][y] = cellChar;
                        }
                        if(x == 0 || y == 0 || x == rows-1){
                            mazeGrid[x][y] = wallChar;
                        }
                    }
                }
                // make meaningful representation
                for (int x = 0; x < dimensionX; x++) {
                    for (int y = 0; y < dimensionY; y++) {
                        Cell current = getCell(x, y);
                        int gridX = x * 2 + 1, gridY = y * 2 + 1;
                        mazeGrid[gridX][gridY] = cellChar;
                        if (current.isCellBelowNeighbor()) {
                            mazeGrid[gridX][gridY + 1] = cellChar;
                        }
                        if (current.isCellRightNeighbor()) {
                            mazeGrid[gridX + 1][gridY] = cellChar;
                        }
                    }
                }
                 
                // We create a clean grid ...
                searching = false;
                endOfSearch = false;
                fillGrid();
                // ... and copy into it the positions of obstacles
                // created by the maze construction algorithm
                for (int x = 0; x < gridDimensionX; x++) {
                    for (int y = 0; y < gridDimensionY; y++) {
                        if (mazeGrid[x][y] == wallChar && grid[x][y] != ROBOT && grid[x][y] != TARGET){
                            grid[x][y] = OBST;
                        }
                    }
                }
            }
        } // end nested class MyMaze
               
     
        /**
         * Expands a node and creates his successors
         */
        private void expandNode(){
           
                Cell current;
                if (dfs.isSelected() || bfs.isSelected()) {
                    // Here is the 3rd step of the algorithms DFS and BFS
                    // 3. Remove the first state, Si, from OPEN SET 
                    current = openSet.remove(0);
                } else {
                    // Here is the 3rd step of the algorithm A* 
                    // 3. Remove the state, Si, from OPEN SET, 
                    //for which f(Si) ≤ f(Sj) for all other (Sj)                
                    // (first sort OPEN SET list with respect to 'f')
                    Collections.sort(openSet, new CellComparatorByF());
                    current = openSet.remove(0);
                }
                // ... and add it to CLOSED SET.
                closedSet.add(0,current);
                // Update the color of the cell
                grid[current.row][current.col] = CLOSED;
                // If the selected node is the target ...
                if (current.row == targetPos.row && current.col == targetPos.col) {
                    // ... then terminate
                    Cell last = targetPos;
                    last.prev = current.prev;
                    closedSet.add(last);
                    found = true;
                    return;
                }
                // Count nodes that have been expanded.
                expanded++;
                // Here is the 4rd step of the algorithms
                // 4. Create the successors of Si.
                //    Each successor has a pointer to the Si, as its predecessor.
                //    In the case of DFS and BFS algorithms, successors should not
                //    belong neither to the OPEN SET nor the CLOSED SET.
                ArrayList<Cell> succesors;
                succesors = createSuccesors(current);
                // Here is the 5th step of the algorithms
                // 5. For each successor of Si, ...
                for (Cell cell: succesors){
                    // ... if we are running DFS ...
                    if (dfs.isSelected()) {
                        // ... add the successor at the beginning of the list OPEN SET
                        openSet.add(0, cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                    // ... if we are runnig BFS ...
                    } else if (bfs.isSelected()){
                        // ... add the successor at the end of the list OPEN SET
                        openSet.add(cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        
                    // ... if we are running A* algorithm calculate the value f(Sj)
                    } else if (aStar.isSelected()){
                        
                        int dxg = current.col-cell.col;
                        int dyg = current.row-cell.row;
                        int dxh = targetPos.col-cell.col;
                        int dyh = targetPos.row-cell.row;
                        
                        // calculate the Manhattan distances and not the Euclidean
                        // because the diagonal moves are not allowed
                             
                        cell.g = current.g+Math.abs(dxg)+Math.abs(dyg); //current to new cell mostly 1 + distance travelled
                            
                        cell.h = Math.abs(dxh)+Math.abs(dyh);       //new cell to target, Manhattan; up or right is the priority always
                        
                        cell.f = (int) Math.ceil((.25)*cell.g + (.75)*cell.h);
                        
                        // ... If Sj is neither in the OPEN SET nor in the CLOSED SET states ...
                        int openIndex   = isInList(openSet,cell);
                        int closedIndex = isInList(closedSet,cell);
                        if (openIndex == -1 && closedIndex == -1) {
                            // ... then add Sj in the OPEN SET ...
                            // ... evaluated as f(Sj)
                            openSet.add(cell);
                            // Update the color of the cell
                            grid[cell.row][cell.col] = FRONTIER;
                        // Else ...
                        } else {
                            // ... if already belongs to the OPEN SET, then ...
                            if (openIndex > -1){
                                // ... compare the new value assessment with the old one. 
                                // If old <= new ...
                                if (openSet.get(openIndex).f <= cell.f) {
                                    // ... then eject the new node with state Sj.
                                    // (ie do nothing for this node).
                                // Else, ...
                                } else {
                                    // ... remove the element (Sj, old) from the list
                                    // to which it belongs ...
                                    openSet.remove(openIndex);
                                    // ... and add the item (Sj, new) to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            // ... if already belongs to the CLOSED SET, ...
                            } else {
                                // ... compare the new value assessment with the old one. 
                                // If old <= new ...
                                if (closedSet.get(closedIndex).f <= cell.f) {
                                    // ... then eject the new node with state Sj.
                                    // (ie do nothing for this node).
                                // Else, ...
                                } else {
                                    // ... remove the element (Sj, old) from the list
                                    // to which it belongs ...
                                    closedSet.remove(closedIndex);
                                    // ... and add the item (Sj, new) to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            }
                        }
                    }
                }
            
        } //end expandNode()
         
        /**
         * Creates the successors of a state/cell
         * 
         * @param current       the cell for which we ask successors        
         * @return              the successors of the cell as a list
         */
        private ArrayList<Cell> createSuccesors(Cell current){
            int r = current.row;
            int c = current.col;
            // We create an empty list for the successors of the current cell.
            ArrayList<Cell> temp = new ArrayList<>();
           
            // The priority is 1: Left 2: Up 3: Down 4: Right
             
                if (c > 0 && grid[r][c-1] != OBST && 
                        // and (only in the case DFS or BFS is running)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((dfs.isSelected() || bfs.isSelected()) ? 
                              isInList(openSet,new Cell(r,c-1)) == -1 &&
                              isInList(closedSet,new Cell(r,c-1)) == -1: true)) {
                    Cell cell = new Cell(r,c-1);

                       // ... update the pointer of the left-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the left-side cell to the successors of the current one. 
                        temp.add(cell);

                }
                // If not at the topmost limit of the grid 
                // and the UP-SIDE cell is not an obstacle ...
                if (r > 0 && grid[r-1][c] != OBST &&
                        // and (only in the case DFS or BFS is running)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((dfs.isSelected() || bfs.isSelected()) ? 
                              isInList(openSet,new Cell(r-1,c)) == -1 &&
                              isInList(closedSet,new Cell(r-1,c)) == -1 : true)) {
                    Cell cell = new Cell(r-1,c);

                        // ... update the pointer of the up-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the up-side cell to the successors of the current one. 
                        temp.add(cell);

                }
                // If not at the lowermost limit of the grid
                // and the DOWN-SIDE cell is not an obstacle ...
                if (r < rows-1 && grid[r+1][c] != OBST &&
                        // and (only in the case DFS or BFS is running)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((dfs.isSelected() || bfs.isSelected()) ? 
                              isInList(openSet,new Cell(r+1,c)) == -1 &&
                              isInList(closedSet,new Cell(r+1,c)) == -1: true)) {
                    Cell cell = new Cell(r+1,c);

                       // ... update the pointer of the down-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the down-side cell to the successors of the current one. 
                        temp.add(cell);

                }
                
                // If not at the rightmost limit of the grid
                // and the RIGHT-SIDE cell is not an obstacle ...
                if (c < columns-1 && grid[r][c+1] != OBST &&
                        // and (only in the case DFS or BFS is running)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((dfs.isSelected() || bfs.isSelected())? 
                              isInList(openSet,new Cell(r,c+1)) == -1 &&
                              isInList(closedSet,new Cell(r,c+1)) == -1: true)) {
                    Cell cell = new Cell(r,c+1);

                        // ... update the pointer of the right-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the right-side cell to the successors of the current one. 
                        temp.add(cell);

                }
                        
            // When DFS algorithm is in use, cells are added one by one at the beginning of the
            // OPEN SET list. Because of this, we must reverse the order of successors formed,
            // so the successor corresponding to the highest priority, to be placed
            // the first in the list.
            // For A* there is no issue, because the list is sorted
            // according to 'f'  before extracting the first element of.
            if (dfs.isSelected()){
                Collections.reverse(temp);
            }
            return temp;
        } // end createSuccesors()
        
        /**
         * Returns the index of the cell 'current' in the list 'list'
         *
         * @param list    the list in which we seek
         * @param current the cell we are looking for
         * @return        the index of the cell in the list
         *                if the cell is not found returns -1
         */
        private int isInList(ArrayList<Cell> list, Cell current){
            int index = -1;
            for (int i = 0 ; i < list.size(); i++) {
                if (current.row == list.get(i).row && current.col == list.get(i).col) {
                    index = i;
                    break;
                }
            }
            return index;
        } // end isInList()
         
        private void calculateDistance(){
            int gValue;
            if(!openSet.isEmpty()){
                for(Cell cell : openSet){
                    gValue = cell.g - aStarDistance;
                    if(gValue > 0){
                        openSet.remove(cell);
                    }
                }
            }
        }       
        
        /**
         * Calculates the path from the target to the initial position
         * of the robot, counts the corresponding steps
         * and measures the distance traveled.
         */
        int aStarDistance = 0;
        private void plotRoute(){
            int distance = 0;
            
            searching = false;
            endOfSearch = true;
            
            
            int index = isInList(closedSet,targetPos);
            Cell cur = closedSet.get(index);
            grid[cur.row][cur.col]= TARGET;
            do {
                distance++;                
                cur = cur.prev;
                grid[cur.row][cur.col] = ROUTE;
                
            } while (!(cur.row == robotStart.row && cur.col == robotStart.col));
            grid[robotStart.row][robotStart.col]=ROBOT;
            
            
            
            if(dfs.isSelected()){
                msgDfs = String.format("Nodes expanded: %d, Distance: %d, Time in sec: %.2f\n",
                         expanded,distance, timesec); 
                message1.setText(msgDfs);
            }
            if(bfs.isSelected()){
                msgBfs = String.format(" Nodes expanded: %d, Distance: %d, Time in sec: %.2f\n",
                         expanded,distance, timesec); 
                message2.setText(msgBfs);
            }
            if(aStar.isSelected()){
                aStarDistance = distance;
                msgAStar = String.format(" Nodes expanded: %d, Distance: %d, Time in sec: %.2f\n",
                         expanded,distance, timesec); 
                message3.setText(msgAStar);
            }
           
        } // end plotRoute()
         
 
        /**
         * paints the grid
         */
        @Override
        public void paintComponent(Graphics g) {
 
            super.paintComponent(g);  // Fills the background color.
 
            g.setColor(Color.DARK_GRAY);
            g.fillRect(10, 10, columns*squareSize+1, rows*squareSize+1);
 
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    switch ( grid[r][c] )
                    {
                        case EMPTY:
                            g.setColor(Color.WHITE);
                            break;
                        case ROBOT:
                            g.setColor(Color.RED);
                            break;
                        case TARGET:
                            g.setColor(Color.GREEN);
                            break;
                        case OBST:
                            g.setColor(Color.BLACK);
                            break;
                        case FRONTIER:
                            g.setColor(Color.BLUE);
                            break;
                        case CLOSED:
                            g.setColor(Color.CYAN);
                            break;
                        case ROUTE:
                            g.setColor(Color.YELLOW);
                            break;
                        default:
                            break;
                    }
                    g.fillRect(11 + c*squareSize, 11 + r*squareSize, squareSize - 1, squareSize - 1);
                }
            }                        
            
        } // end paintComponent()
                       
         
    } // end nested classs MazePanel
   
} // end class Maze
