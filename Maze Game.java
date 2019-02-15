import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import java.nio.channels.OverlappingFileLockException;

import javalib.worldimages.*;

// Vertex are a point in the maze
class Vertex {

  /*
   * TEMPLATE Fields: ... this.coords ... -- Posn ... this.outEdges ... --
   * ArrayList<Edge> ... this.name ... -- String Methods: ... this.addEdge(Edge)
   * ... -- void Methods on fields:
   */

  Posn coords;
  ArrayList<Edge> outEdges;
  String name;

  Vertex(Posn coords, ArrayList<Edge> outEdges) {
    this.coords = coords;
    this.outEdges = outEdges;
    this.name = this.coords.x + "," + this.coords.y;
  }

  // Adds an edge to the list of outedges
  void addEdge(Edge e) {
    this.outEdges.add(e);
  }

}

// The edges connect the different vertices and are given weights
class Edge {

  /*
   * TEMPLATE Fields: ... this.from ... -- Vertex ... this.to ... -- Vertex ...
   * this.weight ... -- int ... this.midpoint ... -- Posn ... this.vertical ... --
   * boolean Methods: ... this.drawEdge(boolean) ... -- LineImage Methods on
   * fields: ... this.from.addEdge(Edge) ... -- void ... this.to.addEdge(Edge) ...
   * -- void
   */

  Vertex from;
  Vertex to;
  int weight;
  Posn midpoint;
  boolean vertical;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
    this.midpoint = new Posn(this.from.coords.x + (this.to.coords.x - this.from.coords.x),
        this.from.coords.y + (this.to.coords.y - this.from.coords.y));
    this.vertical = this.from.coords.x == this.to.coords.x;
    // this.to.addEdge(this);
    // this.from.addEdge(this);
  }

  // Draws a line. Color is based on if it's a path or not. Designed to cover up
  // borders of cells
  LineImage drawEdge(boolean path, Color c) {
    if (path && this.vertical) {
      return new LineImage(new Posn(0, 100), Color.red);
    } else if (path && !this.vertical) {
      return new LineImage(new Posn(100, 0), Color.blue);
    }

    if (this.vertical) {
      return new LineImage(new Posn(100, 0), c);
    } else {
      return new LineImage(new Posn(0, 100), c);
    }
  }
}

class Graph {

  /*
   * TEMPLATE Fields: ... this.allVerticies ... -- ArrayList<Vertex> ... this.reps
   * ... -- HashMap<String, String> ... this.randomChosen ... -- HashMap<Integer,
   * Boolean> ... this.edgesInTree ... -- ArrayList<Edge> ... this.worklist ... --
   * ArrayList<Edge> ... this.rand ... -- Random ... this.RAND_BOUND ... -- int
   * Methods: ... this.randVert(int) ... -- Vertex ... this.generateGraph() ... --
   * Graph ... this.sortingEdges() ... -- void ... this.kruskals() ... -- void ...
   * this.find(String) ... -- String ... this.union(String, String) ... -- void
   * Methods on fields:
   */

  ArrayList<Vertex> allVertices;
  HashMap<String, String> reps;
  HashMap<Vertex, Vertex> cameFromEdge;
  HashMap<Integer, Boolean> randomChosen;
  ArrayList<Vertex> solved;
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> worklist; // all edges in graph sorted by edge weights
  Random rand;
  static final int RAND_BOUND = 1000;

  int height;
  int width;

  Graph(int h, int w, int seed) {
    this.height = h;
    this.width = w;
    if (seed > 0) {
      this.rand = new Random(seed);
    } else {
      this.rand = new Random();

    }
    this.allVertices = new ArrayList<Vertex>();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = new ArrayList<Edge>();
    this.solved = new ArrayList<Vertex>();
    this.reps = new HashMap<String, String>(this.height * this.width);
    this.randomChosen = new HashMap<Integer, Boolean>(
        (this.height - 1) * this.width + (this.width - 1) * this.height);
    this.generateGraph();
    this.kruskals();

  }

  // Generate a new vertex
  Vertex randVert(int i) {
    return new Vertex(new Posn(i % this.width, ((i - (i % this.width)) / this.width)),
        new ArrayList<Edge>());
  }

  // Generate all the minimum spanning tree using Kruskal's algorithm
  Graph generateGraph() {
    for (int i = 0; i < this.height * this.width; i++) {
      this.allVertices.add(this.randVert(i));
      // System.out.println("coords: " + (i % this.width) + "," + ((i - (i %
      // this.width)) / this.width) + " index: " + i);
      this.reps.put(i % this.width + "," + ((i - (i % this.width)) / this.width),
          i % this.width + "," + ((i - (i % this.width)) / this.width));
    }

    // This generates edges edges horizontally between two side by side cells
    for (int i = 0; i < this.width - 1; i++) {
      for (int j = 0; j < this.height; j++) {
        int r = this.rand.nextInt(RAND_BOUND) + 1;

        while (this.randomChosen.get(r) != null && this.randomChosen.get(r)) {
          r = this.rand.nextInt(RAND_BOUND) + 1;
        }

        this.randomChosen.put(r, true);

        // System.out.println("coords: " + i + "," + j + " index: " + ((j * this.width)
        // + i) + " size: " + this.allVertices.size());

        Edge e = new Edge(this.allVertices.get((j * this.width) + i),
            this.allVertices.get((j * this.width) + i + 1), r);

        this.edgesInTree.add(e);

        // this.allVertices.get((j * this.width) + i).addEdge(e);
        // this.allVertices.get((j * this.width) + i + 1).addEdge(e);
      }
    }

    // This generates edges vertically between two cells above/below
    for (int i = 0; i < this.height - 1; i++) {
      for (int j = 0; j < this.width; j++) {
        int r = this.rand.nextInt(RAND_BOUND) + 1;

        while (this.randomChosen.get(r) != null && this.randomChosen.get(r)) {
          r = this.rand.nextInt(RAND_BOUND) + 1;
        }

        this.randomChosen.put(r, true);

        // System.out.println("coords: " + i + "," + j + " between indices: " + ((j *
        // this.width) + i) + " " + ((j
        // * this.width) + i + this.width));

        if (((j * this.width) + i + this.width) < allVertices.size()) {
          Edge e = new Edge(this.allVertices.get((j * this.width) + i),
              this.allVertices.get((j * this.width) + i + this.width), r);

          this.edgesInTree.add(e);
          // this.allVertices.get(i + (j * this.width)).addEdge(e);
          // this.allVertices.get(i + (j * this.width) + 1).addEdge(e);
        }
      }
    }

    this.sortingEdges();

    return this;
  }

  // Sorts all the edges into the work list that Kruskals algorithm can then pull
  // from
  void sortingEdges() {
    int smallest = 0;
    Edge toAdd = null;

    while (this.worklist.size() < this.edgesInTree.size()) {
      int lastWeight = RAND_BOUND + 1;
      for (Edge e : this.edgesInTree) {
        if (e.weight < lastWeight && e.weight > smallest) {
          lastWeight = e.weight;
          toAdd = e;
        }
      }
      smallest = toAdd.weight;
      this.worklist.add(toAdd);
    }
  }

  // Creates the new list of edges in the minimum spanning tree
  // Does this by linking and unionizing the shortest edges it's given
  // Avoids cycles
  void kruskals() {
    // Empty the list of edges
    this.edgesInTree = new ArrayList<Edge>();

    for (Edge e : this.worklist) {
      if (this.find(e.from.name).equals(this.find(e.to.name))) {
        // Do nothing and discard
      } else {
        this.edgesInTree.add(e);
        e.from.addEdge(e);
        e.to.addEdge(e);
        this.union(this.find(e.from.name), this.find(e.to.name));
      }
    }
  }

  // Traces a given Key through the representatives to find its parent
  String find(String s) {
    String value = s;
    while (value != this.reps.get(value)) {
      value = this.reps.get(value);
    }
    return value;
  }

  // unionizes two things in the hashmap
  void union(String a, String b) {
    this.reps.put(a, b);
  }

  // is there a path from vertex from to vertex to
  ArrayList<Vertex> getMaze(Vertex from, Vertex to, ICollection<Vertex> worklist) {
    ArrayList<Vertex> seen = new ArrayList<Vertex>();
    this.cameFromEdge = new HashMap<Vertex, Vertex>();
    this.cameFromEdge.put(this.allVertices.get(0), this.allVertices.get(0));
    worklist.add(from);

    while (worklist.size() > 0) {
      Vertex next = worklist.remove();

      if (next == to) {
        this.reconstruct(next, this.cameFromEdge);
        return seen;
      } else if (seen.contains(next)) {
        // discard
      } else {
        for (Edge e : next.outEdges) {
          if (e.to.name.equals(next.name)) {
            // if next is the "to" node
            worklist.add(e.from);
            if (!this.cameFromEdge.containsKey(e.from)) {
              this.cameFromEdge.put(e.from, next);
            }
          } else {
            worklist.add(e.to);
            if (!this.cameFromEdge.containsKey(e.to)) {
              this.cameFromEdge.put(e.to, next);
            }
          }
        }
      }
      seen.add(next);

    }
    return null;
  }

  void reconstruct(Vertex v, HashMap<Vertex, Vertex> list) {
    Vertex key = list.get(v);
    this.solved = new ArrayList<Vertex>();

    boolean finished = key.name.equals("0,0");

    while (!finished) {

      this.solved.add(key);

      key = list.get(key);
      System.out.println("Going to: " + key.name);
      finished = key.name.equals("0,0");

    }
  }

}

interface ICollection<T> {
  // adds an item to this collection
  void add(T item);

  // removes an item from this collection
  T remove();

  // counts the number of items in this collection
  int size();
}

class Queue<T> implements ICollection<T> {
  Deque<T> items;

  Queue() {
    this.items = new Deque<T>();
  }

  @Override
  public void add(T item) {
    this.items.addAtTail(item);
  }

  @Override
  public T remove() {
    return this.items.removeFromHead();
  }

  @Override
  public int size() {
    return this.items.size();
  }
}

class Stack<T> implements ICollection<T> {
  Deque<T> items;

  Stack() {
    this.items = new Deque<T>();
  }

  @Override
  public void add(T item) {
    this.items.addAtHead(item);
  }

  @Override
  public T remove() {
    return this.items.removeFromHead();
  }

  @Override
  public int size() {
    return this.items.size();
  }

}

class MazeWorld extends World {

  /*
   * TEMPLATE Fields: ... this.graph ... -- Graph ... this.height ... -- int ...
   * this.width ... -- int ... this.boardRatio ... -- float ... this.CELL_START
   * ... -- int Methods: ... this.makeScene() ... -- WorldScene ...
   * this.drawCell() ... -- WorldImage Methods on fields: ...
   * this.graph.randVert(int) ... -- Vertex ... this.graph.generateGraph() ... --
   * Graph ... this.graph.sortingEdges() ... -- void ... this.graph.kruskals() ...
   * -- void ... this.graph.find(String) ... -- String ...
   * this.graph.union(String, String) ... -- void
   */

  static final int CELL_START = 100;

  Graph graph;

  int height;
  int width;
  int randomSeed;
  int renderIndex;
  float boardRatio; // Not currently used

  // Mode will either be the computer mode or human mode
  HashMap<String, Boolean> modes;

  String direction;

  ArrayList<Vertex> totalPath;
  ArrayList<Vertex> solvedPath;
  ArrayList<Vertex> toDraw;

  HashMap<Vertex, Vertex> manualList;

  Vertex currVert;

  MazeWorld(int height, int width, int seed) {
    this.height = height;
    this.width = width;
    this.randomSeed = seed;

    this.graph = new Graph(this.height, this.width, this.randomSeed);

    this.initializeModes();
    this.totalPath = new ArrayList<Vertex>();
    this.toDraw = new ArrayList<Vertex>();
    this.solvedPath = new ArrayList<Vertex>();
    this.manualList = new HashMap<Vertex, Vertex>();
  }

  void initializeModes() {
    this.modes = new HashMap<String, Boolean>();
    this.modes.put("computerMode", false);
    this.modes.put("drawTree", false);
    this.modes.put("dfs", false);
    this.modes.put("bfs", false);
    this.modes.put("solve", false);
    this.modes.put("rendering", false);
    this.renderIndex = 0;
    this.toDraw = new ArrayList<Vertex>();
    this.currVert = this.graph.allVertices.get(0);
  }

  public void onTick() {
    // THIS IS IF THE COMPUTER IS PLAYING
    if (this.modes.get("computerMode") && this.modes.get("rendering")) {
      if (this.totalPath.size() > 0) {
        this.toDraw.add(this.totalPath.remove(0));
      } else if (!(this.totalPath.size() > 0) && this.modes.get("dfs") || this.modes.get("bfs")) {
        this.modes.replace("rendering", false);
      }
    }
    // THIS IS IF THE HUMAN IS PLAYING
    else {
      if (this.currVert.equals(this.graph.allVertices.get(this.height * this.width - 1))
          && !this.modes.get("rendering")) {
        this.graph.reconstruct(this.currVert, this.manualList);
        this.solvedPath = this.graph.solved;
        this.modes.replace("rendering", true);

      }

    }
  }

  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      // generate a new random maze
      // Resets all the modes to be false
      this.graph = new Graph(this.height, this.width, this.randomSeed);
      this.initializeModes();
    } else if (key.equals("d")) {
      // solve the maze with depth first search
      this.modes.replace("dfs", true);
      if (this.modes.get("computerMode")) {
        this.totalPath = this.graph.getMaze(this.graph.allVertices.get(0),
            this.graph.allVertices.get(this.height * this.width - 1), new Stack<Vertex>());
        this.solvedPath = this.graph.solved;
      }
    } else if (key.equals("b")) {
      // solve the maze with breadth first search
      this.modes.replace("bfs", true);
      if (this.modes.get("computerMode")) {
        this.totalPath = this.graph.getMaze(this.graph.allVertices.get(0),
            this.graph.allVertices.get(this.height * this.width - 1), new Queue<Vertex>());
        this.solvedPath = this.graph.solved;
      }
    } else if (key.equals("t")) {
      // Draws the tree onto the map. Acts a toggle to turn the tree on or off
      this.modes.replace("drawTree", !this.modes.get("drawTree"));
    } else if (key.equals("c")) {
      // Computer automatically solves the maze
      this.modes.replace("computerMode", true);
      this.modes.replace("rendering", true);
    } else {
      this.discoverCell(key);
    }
  }

  void discoverCell(String key) {
    Vertex last = this.currVert;
    if (key.equals("up")) {
      for (Edge e : this.currVert.outEdges) {
        if (e.to.name.equals(this.currVert.name) && e.vertical) {
          if (e.from.coords.y < this.currVert.coords.y) {
            this.currVert = e.from;
          }
        } else {
          if (e.to.coords.y < this.currVert.coords.y) {
            this.currVert = e.to;
          }
        }
      }
    } else if (key.equals("down")) {
      for (Edge e : this.currVert.outEdges) {
        if (e.to.name.equals(this.currVert.name) && e.vertical) {
          if (e.from.coords.y > this.currVert.coords.y) {
            this.currVert = e.from;
          }
        } else {
          if (e.to.coords.y > this.currVert.coords.y) {
            this.currVert = e.to;
          }
        }
      }
    } else if (key.equals("right")) {
      for (Edge e : this.currVert.outEdges) {
        if (e.to.name.equals(this.currVert.name) && !e.vertical) {
          if (e.from.coords.x > this.currVert.coords.x) {
            this.currVert = e.from;
          }
        } else {
          if (e.to.coords.x > this.currVert.coords.x) {
            this.currVert = e.to;
          }
        }
      }
    } else if (key.equals("left")) {
      for (Edge e : this.currVert.outEdges) {
        if (e.to.name.equals(this.currVert.name) && !e.vertical) {
          if (e.from.coords.x < this.currVert.coords.x) {
            this.currVert = e.from;
          }
        } else {
          if (e.to.coords.x < this.currVert.coords.x) {
            this.currVert = e.to;
          }
        }
      }
    }

    if (!this.toDraw.contains(this.currVert)) {
      this.totalPath.add(this.currVert);
    }

    if (!this.manualList.containsKey(this.currVert)) {
      this.manualList.put(this.currVert, last);
    }
  }

  // Makes a world scene of the game. This simply displays the maze
  public WorldScene makeScene() {
    WorldScene s = new WorldScene(this.width * CELL_START, this.height * CELL_START);

    this.drawMaze(s);

    if (this.modes.get("drawTree")) {
      this.drawTree(s);
    }

    if ((!this.modes.get("rendering") && this.modes.get("computerMode"))
        || (this.modes.get("rendering") && !this.modes.get("computerMode"))) {
      this.solveMaze(s);
    }

    s.placeImageXY(
        new RectangleImage(CELL_START, CELL_START, OutlineMode.SOLID, new Color(0, 100, 0)),
        (CELL_START / 2), (CELL_START / 2));
    s.placeImageXY(
        new RectangleImage(CELL_START, CELL_START, OutlineMode.SOLID, new Color(128, 0, 128)),
        ((this.width - 1) * CELL_START) + (CELL_START / 2),
        ((this.height - 1) * CELL_START) + (CELL_START / 2));

    return s;
  }

  ////////////////////////////////////
  // ALL THE DIFFERENT DRAW METHODS //
  ////////////////////////////////////

  // Draws a single grey cell
  WorldImage drawCell() {
    return new OverlayImage(
        new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
            Color.BLACK),
        new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
            Color.LIGHT_GRAY));
  }

  // This method is given a scene, and draws the maze on it.
  void drawMaze(WorldScene s) {
    s.placeImageXY(new RectangleImage(this.width * CELL_START, this.height * CELL_START,
        OutlineMode.SOLID, Color.lightGray), this.width * CELL_START / 2,
        this.height * CELL_START / 2);

    for (Vertex v : this.graph.allVertices) {
      s.placeImageXY(drawCell(), (v.coords.x * CELL_START) + (CELL_START / 2),
          (v.coords.y * CELL_START) + (CELL_START / 2));
    }

    for (Edge e : this.graph.edgesInTree) {
      if (e.vertical) {
        // Vertical edges
        s.placeImageXY(e.drawEdge(false, Color.LIGHT_GRAY),
            e.midpoint.x * CELL_START + (CELL_START / 2), e.midpoint.y * CELL_START + 0);
      } else {
        // horizontal edges
        s.placeImageXY(e.drawEdge(false, Color.LIGHT_GRAY), e.midpoint.x * CELL_START + 0,
            e.midpoint.y * CELL_START + (CELL_START / 2));
      }
    }

    if (this.modes.get("computerMode") && (this.modes.get("dfs") || this.modes.get("bfs"))) {
      this.drawComputerSearch(s);
    } else if (!this.modes.get("computerMode")) {
      this.drawHumanSearch(s);
    }

  }

  // This method is given a scene and draws the tree onto it.
  void drawTree(WorldScene s) {

    for (Edge e : this.graph.edgesInTree) {
      if (e.vertical) {
        // Vertical edges
        s.placeImageXY(e.drawEdge(true, Color.LIGHT_GRAY),
            e.midpoint.x * CELL_START + (CELL_START / 2), e.midpoint.y * CELL_START + 0);

      } else {
        // horizontal edges
        s.placeImageXY(e.drawEdge(true, Color.LIGHT_GRAY), e.midpoint.x * CELL_START + 0,
            e.midpoint.y * CELL_START + (CELL_START / 2));

      }
    }

  }

  WorldImage drawPathCell(Color c) {
    return new OverlayImage(
        new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
            Color.BLACK),
        new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.SOLID, c));

  }

  // This method is given a scene and draws the search pattern onto it.
  void drawComputerSearch(WorldScene s) {

    for (Vertex v : this.toDraw) {
      s.placeImageXY(drawPathCell(Color.YELLOW), (v.coords.x * CELL_START) + (CELL_START / 2),
          (v.coords.y * CELL_START) + (CELL_START / 2));
      for (Edge e : v.outEdges) {
        if (e.vertical) {
          // Vertical edges
          s.placeImageXY(e.drawEdge(false, Color.YELLOW),
              e.midpoint.x * CELL_START + (CELL_START / 2), e.midpoint.y * CELL_START + 0);

        } else {
          // horizontal edges
          s.placeImageXY(e.drawEdge(false, Color.YELLOW), e.midpoint.x * CELL_START + 0,
              e.midpoint.y * CELL_START + (CELL_START / 2));

        }

      }
    }
  }

  // Draws the path the human searches
  void drawHumanSearch(WorldScene s) {
    for (Vertex v : this.totalPath) {
      s.placeImageXY(drawPathCell(Color.YELLOW), (v.coords.x * CELL_START) + (CELL_START / 2),
          (v.coords.y * CELL_START) + (CELL_START / 2));
      for (Edge e : v.outEdges) {
        if (e.vertical) {
          // Vertical edges
          s.placeImageXY(e.drawEdge(false, Color.YELLOW),
              e.midpoint.x * CELL_START + (CELL_START / 2), e.midpoint.y * CELL_START + 0);

        } else {
          // horizontal edges
          s.placeImageXY(e.drawEdge(false, Color.YELLOW), e.midpoint.x * CELL_START + 0,
              e.midpoint.y * CELL_START + (CELL_START / 2));
        }
      }
    }
    s.placeImageXY(new RectangleImage(CELL_START, CELL_START, OutlineMode.OUTLINE, Color.CYAN),
        (this.currVert.coords.x * CELL_START) + (CELL_START / 2),
        (this.currVert.coords.y * CELL_START) + (CELL_START / 2));
  }

  // This method solves the maze only AFTER it's been searched by human or
  // computer
  void solveMaze(WorldScene s) {
    for (Vertex v : this.solvedPath) {
      s.placeImageXY(drawPathCell(Color.GREEN), (v.coords.x * CELL_START) + (CELL_START / 2),
          (v.coords.y * CELL_START) + (CELL_START / 2));
      for (Edge e : v.outEdges) {
        if (e.vertical) {
          // Vertical edges
          s.placeImageXY(e.drawEdge(false, Color.GREEN),
              e.midpoint.x * CELL_START + (CELL_START / 2), e.midpoint.y * CELL_START + 0);

        } else {
          // horizontal edges
          s.placeImageXY(e.drawEdge(false, Color.GREEN), e.midpoint.x * CELL_START + 0,
              e.midpoint.y * CELL_START + (CELL_START / 2));

        }

      }
    }
  }

}

class ExamplesMaze {
  // TODO fucking fix this stupid thing so i can generate mazes of any height and
  // width
  MazeWorld mworld = new MazeWorld(12, 11, 0);
  Graph g1;

  Vertex v0;
  Vertex v1;
  Vertex v2;

  Edge e0;
  Edge e1;

  void init() {
    this.g1 = new Graph(3, 3, 100);

    this.v0 = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    this.v1 = new Vertex(new Posn(2, 1), new ArrayList<Edge>());
    this.v2 = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    this.e0 = new Edge(this.v1, this.v0, 10);
    this.e1 = new Edge(this.v0, this.v2, 100);
  }

  void addVertexEdges() {
    this.v0.addEdge(this.e0);
    this.v0.addEdge(this.e1);
    this.v2.addEdge(this.e1);
    this.v1.addEdge(this.e0);
  }

  boolean testVertex(Tester t) {
    this.init();
    this.addVertexEdges();
    return t.checkExpect(this.v0.outEdges.contains(this.e0), true)
        && t.checkExpect(this.v0.outEdges.contains(this.e1), true)
        && t.checkExpect(this.v1.outEdges.contains(this.e1), false);
  }

  boolean testEdge(Tester t) {
    this.init();
    return t.checkExpect(this.e0.midpoint, new Posn(1, 1))
        && t.checkExpect(this.e1.midpoint, new Posn(1, 0)) && t.checkExpect(this.e0.vertical, false)
        && t.checkExpect(this.e1.vertical, true)
        && t.checkExpect(this.e0.drawEdge(false, Color.LIGHT_GRAY),
            new LineImage(new Posn(0, 100), Color.LIGHT_GRAY))
        && t.checkExpect(this.e1.drawEdge(false, Color.LIGHT_GRAY),
            new LineImage(new Posn(100, 0), Color.LIGHT_GRAY))
        && t.checkExpect(this.e0.drawEdge(true, Color.blue),
            new LineImage(new Posn(100, 0), Color.blue))
        && t.checkExpect(this.e1.drawEdge(true, Color.red),
            new LineImage(new Posn(0, 100), Color.red));
  }

  boolean testGraph(Tester t) {
    this.init(); // This generates a new graph.
    // Test the different parts of that
    Graph gTest = this.g1.generateGraph();
    return t.checkExpect(gTest.allVertices.size(), 18)
        && t.checkExpect(gTest.edgesInTree.size(), 20)
        && t.checkExpect(gTest.allVertices.get(0).coords, new Posn(0, 0))
        && t.checkExpect(gTest.allVertices.get(1).coords, new Posn(1, 0))
        && t.checkExpect(gTest.allVertices.get(0).name, "0,0")
        && t.checkExpect(gTest.allVertices.get(1).name, "1,0") // Sorting edges is called within
                                                               // generateGraph
        && t.checkExpect(gTest.worklist.size(), 20)
        && t.checkExpect(gTest.worklist.get(0).weight < gTest.worklist.get(1).weight, true)
        && t.checkExpect(gTest.worklist.get(2).weight < gTest.worklist.get(3).weight, true)
        && t.checkExpect(gTest.worklist.get(3).weight > gTest.worklist.get(4).weight, false)
        && t.checkExpect(gTest.worklist.get(0).weight > gTest.worklist.get(6).weight, false);

  }

  boolean testKrukals(Tester t) {
    this.init(); // Check to make sure the
    // edgesInTrees are sorted correctly and are the expected lengths

    boolean toReturn = true;

    for (int i = 1; i < this.g1.edgesInTree.size(); i++) {
      toReturn = this.g1.edgesInTree.get(i).weight > this.g1.edgesInTree.get(i - 1).weight
          && toReturn;
    }

    return toReturn && t.checkExpect(this.g1.edgesInTree.size(), 8)
        && t.checkExpect(this.g1.reps.size(), 9);
  }

  boolean testUnionAndFind(Tester t) {
    this.init(); // Make sure they all point
    this.g1.union(this.g1.find(this.e0.from.name), this.g1.find(this.e0.to.name));
    return t.checkExpect(this.g1.find("0,0"), "2,0") && t.checkExpect(this.g1.find("0,1"), "2,0")
        && t.checkExpect(this.g1.find("0,2"), "2,0") && t.checkExpect(this.g1.find("1,0"), "2,0")
        && t.checkExpect(this.g1.find("1,1"), "2,0") && t.checkExpect(this.g1.find("1,2"), "2,0")
        && t.checkExpect(this.g1.find("2,0"), "2,0") && t.checkExpect(this.g1.find("2,1"), "2,0")
        && t.checkExpect(this.g1.find("2,2"), "2,0");

  }

  boolean testMaze(Tester t) {
    return t.checkExpect(this.mworld.drawCell(),
        new OverlayImage(
            new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
                Color.BLACK),
            new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
                Color.LIGHT_GRAY)))
        && t.checkExpect(this.mworld.drawCell(),
            new OverlayImage(
                new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
                    Color.BLACK),
                new RectangleImage(MazeWorld.CELL_START, MazeWorld.CELL_START, OutlineMode.OUTLINE,
                    Color.LIGHT_GRAY)));
  }

  void testGame(Tester t) {
    this.mworld.bigBang(this.mworld.width * MazeWorld.CELL_START,
        this.mworld.height * MazeWorld.CELL_START, 0.05);
  }
}

//Represents a boolean valued question over values of type T
interface IPred<T> {
// Applies a predicate to an object
  boolean apply(T t);
}

class SameNode<T> implements IPred<T> {
  T object;

  /*
   * TEMPLATE: Fields: ... this.object... -- T Methods: ... apply(T)... -- boolean
   */

  SameNode(T s) {
    this.object = s;
  }

  public boolean apply(T t) {
    return this.object.equals(t);
  }
}

class IsCDE implements IPred<String> {
  public boolean apply(String t) {
    return t.equals("cde");
  }
}

//Abstract Class ANode
abstract class ANode<T> {
  /*
   * TEMPLATE: Fields: ... this.next... -- ANode<T> ... this.prev... --- ANode<T>
   * Methods: ... countNodes()... -- int ... findHelper(IPred<T>)... -- ANode<T>
   * ... remove()... -- T ... addNode(T)... -- void Methods on fields: ...
   * this.prev.countNodes()... -- int ... this.prev.findHelper(IPred<T>)... --
   * ANode<T> ... this.prev.remove()... -- T ... this.prev.addNode(T)... -- void
   * ... this.next.countNodes()... -- int ... this.next.findHelper(IPred<T>)... --
   * ANode<T> ... this.next.remove()... -- T ... this.next.addNode(T)... -- void
   */

  ANode<T> next;
  ANode<T> prev;

// Counts how many nodes are present
  abstract int countNodes();

// Helps find the wanted node
  abstract ANode<T> findHelper(IPred<T> p);

// Removes a node
  abstract T remove();

// Adds a node to the linked list
  void addNode(T t) {
    new Node<T>(t, this, this.prev);
  }

}

class Node<T> extends ANode<T> {

  /*
   * TEMPLATE: Fields: ... this.data... -- T ... this.next... -- ANode<T> ...
   * this.prev... --- ANode<T> Methods: ... countNodes()... -- int ...
   * findHelper(IPred<T>)... -- ANode<T> ... remove()... -- T Methods on fields:
   * ... this.prev.countNodes()... -- int ... this.prev.findHelper(IPred<T>)... --
   * ANode<T> ... this.prev.remove()... -- T ... this.prev.addNode(T)... -- void
   * ... this.next.countNodes()... -- int ... this.next.findHelper(IPred<T>)... --
   * ANode<T> ... this.next.remove()... -- T ... this.next.addNode(T)... -- void
   */

  T data;

  Node(T t) {
    this.next = null;
    this.prev = null;
    this.data = t;
  }

  Node(T t, ANode<T> nextNode, ANode<T> prevNode) {
    this.data = t;
    this.next = nextNode;
    this.prev = prevNode;

    if (nextNode == null || prevNode == null) {
      throw new IllegalArgumentException("Can't be null");
    } else {
      prevNode.next = this;
      nextNode.prev = this;
    }
  }

// counts the number of nodes
  public int countNodes() {
    return 1 + this.next.countNodes();
  }

// Checks to see if this is the node we want
  ANode<T> findHelper(IPred<T> p) {
    if (p.apply(this.data)) {
      return this;
    } else {
      return this.next.findHelper(p);
    }
  }

// Removes this node
  public T remove() {
    this.prev.next = this.next;
    this.next.prev = this.prev;
    return this.data;
  }
}

class Sentinel<T> extends ANode<T> {

  /*
   * TEMPLATE: Fields: ... this.next... -- ANode<T> ... this.prev... --- ANode<T>
   * Methods: ... countNodes()... -- int ... findHelper(IPred<T>)... -- ANode<T>
   * ... remove()... -- T Methods on fields: ... this.prev.countNodes()... -- int
   * ... this.prev.findHelper(IPred<T>)... -- ANode<T> ... this.prev.remove()...
   * -- T ... this.prev.addNode(T)... -- void ... this.next.countNodes()... -- int
   * ... this.next.findHelper(IPred<T>)... -- ANode<T> ... this.next.remove()...
   * -- T ... this.next.addNode(T)... -- void
   */

  Sentinel() {
    this.next = this;
    this.prev = this;
  }

// Counts the number of nodes, but are none in header so zero
  int countNodes() {
    return 0;
  }

// Cannot remove a Sentinel so throw an error
  public T remove() {
    throw new RuntimeException("It's empty, can't remove");
  }

// Just returns this
  public ANode<T> findHelper(IPred<T> pred) {
    return this;
  }

}

class Deque<T> {

  /*
   * TEMPLATE: Fields: ... this.header... -- Sentinel<T> Methods: ... size()... --
   * int ... addAtHead(T)... -- void ... addAtTail(T)... -- void ...
   * removeFromHead()... -- T ... removeFromTail()... -- T ... find(IPred<T>)...
   * -- ANode<T> ... removeNode(ANode<T>)... -- void Methods on fields: ...
   * this.header.countNodes()... -- int ... this.header.findHelper(IPred<T>)... --
   * ANode<T> ... this.header.remove()... -- T ... this.header.addNode(T)... --
   * void
   */

  Sentinel<T> header;

// Creates new sentinel
  Deque() {
    this.header = new Sentinel<T>();
  }

// Creates specific sentinel
  Deque(Sentinel<T> s) {
    this.header = s;
  }

// Counts the number of nodes in a list Deque, but does not include the header
  int size() {
    return this.header.next.countNodes();
  }

// Takes a value of type T and puts it at the front of the list
  void addAtHead(T t) {
    this.header.next.addNode(t);
  }

//Takes a value of type T and puts it at the back of the list
  void addAtTail(T t) {
    this.header.addNode(t);
  }

// Removes the first node from, but throws a run time error if empty
  T removeFromHead() {
    return this.header.next.remove();
  }

// Removes the last node, but throws a run time error if empty
  T removeFromTail() {
    return this.header.prev.remove();
  }

// Takes a predicate and produces the first node that satisfies it
  ANode<T> find(IPred<T> pred) {
    return this.header.next.findHelper(pred);
  }

// Removes a node from the Deque
  void removeNode(ANode<T> node) {
    this.find(new SameNode<T>(((Node<T>) node).data)).remove();
  }
}

class ExamplesDeque {

  Deque<String> deque1;
  Deque<String> deque2;
  Deque<String> deque3;
  Deque<String> deque4;

// Deque 2
  Node<String> n1;
  Node<String> n2;
  Node<String> n3;
  Node<String> n4;

  IPred<String> cdeCheck = new IsCDE();

  void initStringData() {
    this.deque1 = new Deque<String>();
    this.deque2 = new Deque<String>();
    this.deque3 = new Deque<String>();
    this.deque4 = new Deque<String>();

    // Deque 2
    this.n1 = new Node<String>("abc", this.deque2.header, this.deque2.header);
    this.n2 = new Node<String>("bcd", this.deque2.header, this.n1);
    this.n3 = new Node<String>("cde", this.deque2.header, this.n2);
    this.n4 = new Node<String>("def", this.deque2.header, this.n3);

    // Deque 3
    this.deque3.addAtHead("hello");
    this.deque3.addAtHead("world");
    this.deque3.addAtTail("hi");
    this.deque3.addAtTail("planet");

    // Deque 4
    this.deque4.addAtHead("bcd");
    this.deque4.addAtTail("cde");
  }

  void addFrontNodes() {
    this.deque2.addAtHead(new String("def"));
    this.deque2.addAtHead(new String("cde"));
  }

  void addEndNodes() {
    this.deque2.addAtTail("two");
    this.deque3.addAtTail("one");
    this.deque1.addAtTail("three");
  }

  void removeNodes() {
    this.deque2.removeNode(this.n1);
    this.deque2.removeNode(this.n4);
  }

  void modifySize() {
    this.deque1.addAtHead("earth");
    this.deque1.addAtHead("mars");
    this.deque2.addAtHead("jupiter");
  }

  boolean testSizeInit(Tester t) {
    this.initStringData();
    return t.checkExpect(this.deque1.size(), 0) && t.checkExpect(this.deque2.size(), 4);
  }

  boolean testSizeCheck(Tester t) {
    this.initStringData();
    this.modifySize();
    return t.checkExpect(this.deque1.size(), 2) && t.checkExpect(this.deque2.size(), 5);
  }

  boolean testAddHead(Tester t) {
    this.initStringData();
    this.addFrontNodes();
    return t.checkExpect(this.deque2.removeFromHead(), "cde")
        && t.checkExpect(this.deque2.removeFromHead(), "def");
  }

  boolean testAddTail(Tester t) {
    this.initStringData();
    this.addEndNodes();
    return t.checkExpect(this.deque2.removeFromTail(), "two")
        && t.checkExpect(this.deque1.size(), 1)
        && t.checkExpect(this.deque1.removeFromHead(), "three");
  }

  boolean testRemoveHead(Tester t) {
    this.initStringData();
    return t.checkException(new RuntimeException("It's empty, can't remove"), this.deque1,
        "removeFromHead") && t.checkExpect(this.deque3.removeFromHead(), "world");
  }

  boolean testRemoveTail(Tester t) {
    this.initStringData();
    return t.checkExpect(this.deque2.removeFromTail(), "def")
        && t.checkExpect(this.deque2.removeFromTail(), "cde")
        && t.checkExpect(this.deque2.size(), 2);
  }

  boolean testRemoveNode(Tester t) {
    this.initStringData();
    this.removeNodes();
    return t.checkExpect(this.deque2, this.deque4);
  }

  boolean testFind(Tester t) {
    this.initStringData();
    return t.checkExpect(this.deque2.find(new SameNode<String>("cde")), this.n3)
        && t.checkExpect(this.deque2.find(new SameNode<String>("def")), this.n4);
  }

  boolean testCDE(Tester t) {
    this.initStringData();
    return t.checkExpect(this.cdeCheck.apply("dec"), false)
        && t.checkExpect(this.cdeCheck.apply("cde"), true);
  }
}