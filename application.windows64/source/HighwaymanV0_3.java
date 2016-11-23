import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class HighwaymanV0_3 extends PApplet {

//Master loop, and global variables

Network network = new Network();
Visualiser vis =new Visualiser(network);

PVector oldMouse = new PVector();
PVector pan = new PVector(0,0);

float zoom=1;
float scrollSpeed=5;
float zoomSpeed=0.1f;
float snap = 30; //square of the number of pixels drawing a link will snap to
boolean networkUpdate; //logs if changes have been made to the network
boolean paused;
long runtime;
long lastCheck;
int mode=2; //1 select drag, 2 add, 3 pan?


public void setup() {
  
  //fullScreen(P2D); //P2D
  
}

public void draw() {
  background(255);
  
  //Manage user input
  if(up) pan.y-=scrollSpeed*zoom;
  if(down) pan.y+=scrollSpeed*zoom;
  if(left) pan.x-=scrollSpeed*zoom;
  if(right) pan.x+=scrollSpeed*zoom;
  if(zoomIn) zoom-=zoomSpeed*zoom;
  if(zoomOut) zoom+=zoomSpeed*zoom;
  
  //only update network if change made
  if(networkUpdate){
     updateNetwork();
  }
  
  //display network
  network.display();
  
  //run visualiser
  if(!paused){
    vis.runGenerator();
    vis.updateEntities();
      //get time elapsed
    runtime+=millis()-lastCheck;
  }
  lastCheck=runtime;
  vis.display();
  
  //add display to top left
  drawRunInfo();
  
  //draw drag line
  if(mousePressed&&mouseButton==LEFT){line(m2sX(oldMouse.x),m2sY(oldMouse.y),mouseX,mouseY);}
  
}

public void updateNetwork(){
     network.updatePaths();
     network.updateTripMatrices();
     network.assignAllNothing();
     vis =new Visualiser(network);
     networkUpdate=false;
}

//when mouse pressed store coordinates
public void mousePressed(){
  oldMouse.x = s2mX(mouseX);
  oldMouse.y = s2mY(mouseY);
}

public void mouseReleased(){
   //if mouse has click dragged
   if (mouseButton==LEFT) {
     addParts();
   }
   if (mouseButton==RIGHT){
     //moveNode(); 
   }
}

public void mouseClicked(){
  if(mouseButton==LEFT){
    network.networkNodes.add(s2mX(mouseX),s2mY(mouseY));
  } else if(mouseButton==RIGHT){
     Node node=network.networkNodes.getNearest(s2mX(mouseX),s2mY(mouseY));
     Link link=(Link)network.networkLinks.getNearest(new PVector(s2mX(mouseX),s2mY(mouseY)));
     
     float distNode = PVector.dist(node.position,new PVector(s2mX(mouseX),s2mY(mouseY)));
     float distLink;
     if (link!=null) {
        distLink =distanceFromLine(link.a.position, link.b.position,new PVector(s2mX(mouseX),s2mY(mouseY)));
     } else {
        distLink=MAX_FLOAT; 
     }
     if(distNode<distLink && distNode < snap){
       network.networkLinks.removeLinksOn(node);
       network.networkNodes.remove(node);
       updateNetwork();
     } else if (distLink < snap && link!=null) {
       network.networkLinks.links.remove(link);
       updateNetwork();
     }
  }
}

public void moveNode(){
  if(mouseX!=m2sX(oldMouse.x) || mouseY!=m2sY(oldMouse.y)){
    
  }
}

public void addParts(){
 if(mouseX!=m2sX(oldMouse.x) || mouseY!=m2sY(oldMouse.y)){
     Node a=network.networkNodes.getNearest(oldMouse.x,oldMouse.y); 
     Node b=network.networkNodes.getNearest(s2mX(mouseX),s2mY(mouseY));
     Link la=(Link)network.networkLinks.getNearest(new PVector(oldMouse.x,oldMouse.y));
     Link lb=(Link)network.networkLinks.getNearest(new PVector(s2mX(mouseX),s2mY(mouseY)));
     float distA = sqrt(pow(m2sX(a.x)-m2sX(oldMouse.x),2)+pow(m2sY(a.y)-m2sY(oldMouse.y),2));
     float distB = sqrt(pow(m2sX(b.x)-mouseX,2)+pow(m2sY(b.y)-mouseY,2));
     float distla,distlb;
     if (la!=null){
        println("Nearest from link: "+la.a.id+"-"+la.b.id);
        distla=distanceFromLine(la.a.position, la.b.position,new PVector(oldMouse.x,oldMouse.y));
     } else {
        distla = MAX_FLOAT; 
     }
     if (lb!=null){
        println("Nearest to link: "+lb.a.id+"-"+lb.b.id);
        distlb=distanceFromLine(lb.a.position, lb.b.position,new PVector(s2mX(mouseX),s2mY(mouseY)));
     } else {
        distlb = MAX_FLOAT; 
     }
     
     println("Distances are: "+distA+","+distB);
     println("Distances are: "+distla+","+distlb);
     
     //if out of snapping distance make new nodes
     if(distA >= snap) {
       if(distla<snap&&la!=null){
         println("split from");
         PVector point = closestPointOnLine(la.a.position, la.b.position, new PVector(oldMouse.x,oldMouse.y));
         a=network.networkNodes.add(oldMouse.x,oldMouse.y);
       } else {
         a=network.networkNodes.add(oldMouse.x,oldMouse.y);
       }
     } 
     if(distB >= snap) {
       if(distlb<snap&&lb!=null){
         println("split to");
         PVector point = closestPointOnLine(lb.a.position, lb.b.position, new PVector(s2mX(mouseX),s2mY(mouseY)));
         b=network.networkNodes.add(s2mX(mouseX),s2mY(mouseY));
       } else {
         b=network.networkNodes.add(s2mX(mouseX),s2mY(mouseY));
       }
     }
     if(distA >= snap && distla < snap && la!=null){
         network.networkLinks.add(la.a,a,la.sfc);
         network.networkLinks.add(a,la.b,la.sfc);
         //check opposite direction too
         if(network.networkLinks.linkExists(la.b,la.a)){
           Link rev=(Link)network.networkLinks.get(la.b,la.a);
           network.networkLinks.add(a,la.a,rev.sfc);
           network.networkLinks.add(la.b,a,rev.sfc);
           network.networkLinks.links.remove(rev);
         }
         network.networkLinks.links.remove(la);

     }
     if(distB >= snap && distlb < snap && lb!=null){
         network.networkLinks.add(lb.a,b,lb.sfc);
         network.networkLinks.add(b,lb.b,lb.sfc);
         //check opposite direction too
         if(network.networkLinks.linkExists(lb.b,lb.a)){
           Link rev=(Link)network.networkLinks.get(lb.b,lb.a);
           network.networkLinks.add(b,lb.a,rev.sfc);
           network.networkLinks.add(lb.b,b,rev.sfc);
           network.networkLinks.links.remove(rev);
         }
         network.networkLinks.links.remove(lb); 
     }
     network.networkLinks.add(a,b,network.sfcs.get(sfcSelection-1));
     //draw both directions
     if(twoWay==true){
       network.networkLinks.add(b,a,network.sfcs.get(sfcSelection-1));
     }
   }
   networkUpdate=true; 
}
public float s2mX(float x){
  return x*zoom+pan.x;
}

public float s2mY(float y){
  return y*zoom+pan.y;
}

public float m2sX(float x){
  return (x-pan.x)/zoom;
}

public float m2sY(float y){
  return (y-pan.y)/zoom;
}

public void drawRunInfo(){
  pushStyle();
    fill(0);
    text("FrameRate: "+round(frameRate)+"fps",5,25);
    text("Entities:  "+vis.Entities.size(),5,50);
    text("Coordinates:"+round(s2mX(mouseX))+","+round(s2mY(mouseY)),5,75);
    text("SFC Type:"+sfcSelection +" "+  network.sfcs.get(sfcSelection-1).name,5,100);
    text("Nodes: "+network.networkNodes.nodes.size() + " Links:"+network.networkLinks.links.size(),5,125);
    text("Time: "+year()+"/"+month()+"/"+day()+" "+hour()+":"+minute()+":"+second(),5,150);
    text("Runtime: "+runtime,5,175);
  popStyle();
}
//Class for moving objects

class Entity{
  Network network;
  PVector position;
  PVector velocity;
  int routeIndex;
  int[] route; //list of node ids to travel through
  int paint;
  Boolean finished; //flag for deletion
  Node target; //next node
  Node last; //previous node reached
  Link link;
  
  Entity(Network _network, int o, int d){
    network = _network;
    //println("New Entity " + o + "to" + d);
    paint = color(random(0,255),random(0,255),random(0,255)); //gives it a random color
    finished = false;
    routeIndex=1;
    route = network.pathfinder.getPath(o,d);
    //-printArray(route);
    position = new PVector( network.networkNodes.nodes.get(o).x , network.networkNodes.nodes.get(o).y);
    last = network.networkNodes.nodes.get(o);
    target = network.networkNodes.nodes.get(route[routeIndex]);
    if (route[0]==-1){finished=true;} //if there is no route, flag for deletion
  }
  
  public void run(){
    if (target == null){
       paused=true; 
       println("Route Found non existing node! "+route[routeIndex]);
       return;
    }
    
    link = (Link)network.networkLinks.get(last,target);
    if(link == null){
      paused=true;
      println("Route is not valid! "+last.id+"-"+target.id);
      println("Route is: routeIndex is "+routeIndex);
      printArray(route);
      return;
    }
    velocity = PVector.sub(new PVector(target.x,target.y),position);
    float d = PVector.dist(position,new PVector(target.x,target.y));
    velocity.normalize();
    velocity.mult(link.speed);
    //if close to nodes, slow down to avoid overshoot
    if(d<2*velocity.mag()){
     velocity.mult(map(d,0,2*velocity.mag(),0,1));
    }
    position.add(velocity);
    //if at node, look for next one on route, if there is one
    if (d<1) {
      if(routeIndex<route.length-1){
         last = network.networkNodes.nodes.get(route[routeIndex]);
         target = network.networkNodes.nodes.get(route[routeIndex+1]);
         routeIndex++;
      } else {
         finished=true; 
      }
    }
  }
  
  public void display(){
    pushStyle();
      fill(paint);
      rectMode(RADIUS);
      pushMatrix();
        translate(m2sX(position.x),m2sY(position.y));
        rotate(velocity.heading());
        rect(0,-2*5/zoom,20/zoom,10/zoom,5/zoom);
      popMatrix();
    popStyle();
  }
  
  public String toString(){
    return route[routeIndex]+" "+position.x+" "+position.y;
  }
}
boolean up=false;
boolean down=false;
boolean left=false;
boolean right=false;
boolean zoomIn=false;
boolean zoomOut=false;
boolean twoWay=false;
int sfcSelection=1;

public void keyPressed(){
      if (key == CODED){
         if (keyCode == UP) {
             up=true;
         }
         if (keyCode == DOWN) {
             down=true;
         }
         if (keyCode == LEFT) {
             left=true;
         }
         if (keyCode == RIGHT) {
             right=true;
         }
      }
      if(key == '='||key=='+') {
        zoomIn=true;
      }
      if(key == '-'){
         zoomOut=true; 
      }
}

public void keyReleased(){
    if (key == CODED){
       if (keyCode == UP) {
           up=false;
       }
       if (keyCode == DOWN) {
           down=false;
       }
       if (keyCode == LEFT) {
           left=false;
       }
       if (keyCode == RIGHT) {
           right=false;
       }
    }
    if(key == '='||key=='+') {
      zoomIn=false;
    }
    if(key == '-'){
      zoomOut=false; 
    }
    if(key == 't'){
        twoWay=toggle(twoWay,"Two Way","One Way");
    }
    if(key == 'p'){
        paused=toggle(paused,"Paused","Unpaused");
    }
    if(key >= 48 && key <= 57){
        sfcSelection=(int)key-48;
    }
}
class Matrix{
  String name;
  String units;
  String dimensions;
  
  float[][] values;
  float[] rowTotals;
  float[] targetRows;
  float[] columnTotals;
  float[] targetColumns;
  
  Matrix(float[][] _values){
    values=_values;
    rowTotals = new float[values.length];
    columnTotals = new float[values[0].length];
    updateTotals();
  }
  
  public void updateTotals(){
    updateRowTotal();
    updateColumnTotal();
  }
  
  public float[] updateRowTotal(){
    for(int i=0;i<values.length;i++){
      float total=0;
      for(int j=0;j<values[i].length;j++){
        total+=values[i][j];
      }
      rowTotals[i]=total;
    }
    return rowTotals;
  }
  
  //assuming second level is constant size
  public float[] updateColumnTotal(){
    for(int i=0;i<values.length;i++){
      float total=0;
      for(int j=0;j<values[0].length;j++){
        total+=values[j][i];
      }
      columnTotals[i]=total;
    }
    return columnTotals;
  }
  
  //factors matrix by a value/matrix
  public void mult(){
    
  }
  
  public void div(){
    
  }
  
  public void add(){
    
  }
  
  public void sub(){
    
  }
  
  public void avg(){
    
  }
  
  public void weightedAvg(){
    
  }
  
  //method of successive averages
  public void msa(){
    
  }
  
  //adds a small values to zero elements
  public void seed(){
    
  }
  
  //returns transposed matrix
  public void transpose(){
    
  }
  
  public void readSATURN(){
    
  }
  
  public void writeSATURN(){
    
  }
  
  public void readTUBA(int version){
    
  }
  
  public void writeTUBA(int version){
    
  }
  
  public void readEMME(int version){
    
  }
  
  public void writeEMME(int version){
    
  }
  
  public void furness(float targetError){
    float maxDiff=0;
    do {
      //fit to row totals;
      for(int i=0;i<values.length;i++){
        for(int j=0;j<values[i].length;j++){
          values[i][j]*=targetRows[i]/rowTotals[i];
        }
      }
      updateTotals();
      //find largest column difference
      for(int j=0;j<values[0].length;j++){
          if(columnTotals[j]>maxDiff){
             maxDiff=columnTotals[j];
          }
       }
      
      //fit to column totals;
      for(int i=0;i<values.length;i++){
        for(int j=0;j<values[i].length;j++){
          values[i][j]*=targetColumns[j]/columnTotals[j];
        }
      }
      updateTotals();
      //find largest row difference
      for(int i=0;i<values.length;i++){
          if(rowTotals[i]>maxDiff){
             maxDiff=rowTotals[i];
          }
       }
    } while(maxDiff>targetError);
  }
  
  
  
}
// A set of network objects:
  //Network
  //Node
  //Link

class Network{
   Nodes networkNodes;
   Links networkLinks;
   PathFinder pathfinder;
   ArrayList<SpeedFlowCurve> sfcs= new ArrayList<SpeedFlowCurve>();
   float[][] tripMatrix;
   float[]origins;
   float[]destinations;
   Boolean labelLinks,labelNodes,showArrows,showFlow,showTripEnds;
   Network(){
     networkNodes = new Nodes();
     networkLinks = new Links();
     tripMatrix = new float[networkNodes.nodes.size()][networkNodes.nodes.size()];
     initalizeArray(tripMatrix,0);
     pathfinder = new PathFinder(networkLinks,networkNodes.nodes.size());
     labelLinks=false;
     labelNodes=true;
     showArrows=false;
     showFlow=true;
     showTripEnds=false;
     sfcs.add(new SpeedFlowCurve("Motorway",sfcs.size(), 112/30, 72, 6000, 2.7f, 3)); //112
     sfcs.add(new SpeedFlowCurve("Dual Carridgeway",sfcs.size(), 104/30, 64, 4000, 2.75f, 2)); //104
     sfcs.add(new SpeedFlowCurve("Nation Speed",sfcs.size(), 96/30, 50, 2000, 3, 1)); //96
     sfcs.add(new SpeedFlowCurve("30 Zone",sfcs.size(), 48/30, 25, 1500, 3, 1)); //48
   }
   
   public void display(){
      for(Node n : networkNodes.nodes){
         n.display(labelNodes,showTripEnds); 
      }
      for(Link l: networkLinks.links){
         l.display(labelLinks,showArrows,showFlow); 
      }      
   }
   
   public void updateTripMatrices(){
     int s = networkNodes.nodes.size();
     origins = new float[s];
     destinations = new float[s];
     tripMatrix = new float[s][s];
     for(Node n:networkNodes.nodes){
        origins[n.id]=n.tripOrigins;
        destinations[n.id]=n.tripDestinations;
     }
     for(int i=0;i<s;i++){
       for(int j=0;j<s;j++){
         tripMatrix[i][j]=origins[i]/s;
       }
     }
   }
   
   public void assignAllNothing(){
     int s = networkNodes.nodes.size();
     //set flows to zero
     for(Link l:networkLinks.links){
        l.sfc.flow=0; 
     }
     
     for(int i=0;i<s;i++){
       for(int j=0;j<s;j++){
         if(i!=j){
           int[] path = pathfinder.getPath(i,j);
           //if(path.length>1){
             for(int n=0;n<path.length-1;n++){
                Node tempA = networkNodes.nodes.get(path[n]);
                Node tempB = networkNodes.nodes.get(path[n+1]);
                Link pathSegment = (Link)networkLinks.get(tempA,tempB);
                pathSegment.sfc.flow += tripMatrix[i][j];
              }
           //}
         }
       }
     }
     //update speed of links
     for(Link l:networkLinks.links){
       l.updateSFC(); 
     }
   }
   
   public void updatePaths(){
      pathfinder = new PathFinder(networkLinks,networkNodes.nodes.size());
      pathfinder.runFinder();
   }
}

class Nodes{
  ArrayList <Node> nodes; 
  
  Nodes(){
    nodes = new ArrayList<Node>();
  }
  public Node add(float X,float Y){
    int id = nodes.size();
    Node newNode = new Node(X,Y,id);
    nodes.add(newNode);
    return newNode;
  }
  
  //also need to update ids
  public void remove(Node node){
    for(int i=nodes.size()-1;i>node.id;i--){
      Node temp=nodes.get(i);
      temp.id--;
    }
    nodes.remove(node);
  }
  
  public Node get(int id){
    return nodes.get(id);
  }
  
  //get nearest node in Cartesian distance
  public Node getNearest(float x,float y){
    Node nearest = new Node(0,0,0);
    float minDist = MAX_FLOAT;
    for(Node n : nodes){
       float dist2 = pow(n.x-x,2)+pow(n.y-y,2);
       if (dist2 < minDist) {
         minDist = dist2;
         nearest = n;
       }
    }
    
    return nearest;
  }
  
}
 class Links{
  ArrayList <Link> links; 
  
  Links(){
    links  =new ArrayList<Link>();
  }
  
  public Link add(Node a,Node b,SpeedFlowCurve sfc){
    if (!linkExists(a,b) && a != b) {
       int id = links.size();
       Link newLink = new Link(a,b,id,sfc);
       links.add(newLink);
       a.connectedLinks.add(newLink);
       b.connectedLinks.add(newLink);
       return newLink;
    } else {
      return null; 
    }
  }
  
  public void removeLinksOn(Node node){
      for(int i=links.size()-1;i>=0;i--){
        Link temp = links.get(i);
        if(temp.a == node || temp.b == node){
          links.remove(i);
        }
      }
  }
  
  public boolean linkExists(Node a, Node b) {
   boolean result=false;
   for (Link checkLink : links){
      if(checkLink.a == a && checkLink.b == b){
         result = true; 
      }
   }
   return result;
  }
  
  public Object getNearest(PVector position){
    Link nearest=null;
    float minDist = MAX_FLOAT;
    for(Link l : links){
       float dist = distanceFromLine(new PVector(l.a.x,l.a.y), new PVector(l.b.x,l.b.y), position);
       if (dist < minDist) {
         minDist = dist;
         nearest = l;
       }
    }
    
    return nearest;
  }
  
  //find via node object
  public Object get(Node a, Node b) {
   Object result= null; //new Link(a,b,-1,new SpeedFlowCurve("",0,0,0,0,0,0));
   for (Link checkLink : links){
      if(checkLink.a == a && checkLink.b == b){
         result = checkLink; 
      }
   }
   return result;
  }
  
  public Link get(int id){
     return links.get(id); 
  }
 }


class Node{
   ArrayList <Link> connectedLinks; 
   PVector position;
   float x,y;
   int id;
   float tripOrigins, tripDestinations;
   
   Node(float X,float Y, int Id){
     connectedLinks  = new ArrayList<Link>();
     x=X;
     y=Y;
     position =new PVector(x,y);
     id=Id;
     tripOrigins=random(10);
     tripDestinations=0;
   }
   
   public void display(Boolean label,Boolean showTripEnds){
     pushStyle();
       textSize(12);
       pushMatrix();
          translate(m2sX(x),m2sY(y));
          ellipse(0,0,4,4);
          translate(0,5);
          fill(50);
          if (label) text(str(id),0,0);
          if(showTripEnds){
            rect(-12,tripOrigins,4,tripOrigins);
            text(str(round(tripOrigins)),-12,-5);
            rect(+12,tripDestinations,4,tripDestinations);
            text(str(round(tripDestinations)),12,-5);
          }
       popMatrix();
     popStyle();
   }
}

class Link{
   int id;
   Node a,b; 
   float distance;
   float time;
   float genCost;
   float speed;
   SpeedFlowCurve sfc;
   
   Link(Node A,Node B, int Id, SpeedFlowCurve _sfc){
      a=A;
      b=B;
      id=Id;
      sfc=new SpeedFlowCurve(_sfc.name,_sfc.index,_sfc.freeSpeed,_sfc.capSpeed,_sfc.capacity,_sfc.power,_sfc.lanes);
      //println("New SFC is: "+sfc);
      distance = sqrt(pow(B.x-A.x,2)+pow(B.y-A.y,2));
      time = distance/sfc.updateSpeed();
      genCost = distance + time;
      speed = distance/time;
   }
   
   public void updateSFC(){
    distance = sqrt(pow(b.x - a.x,2)+pow(b.y - a.y,2));
    time = distance/sfc.updateSpeed();
    genCost = distance + time;
    speed = distance/time;
   }
   
   public void display(Boolean label, Boolean arrow, Boolean showFlow){
      //draw link
      pushStyle();
        strokeWeight(1/zoom);
        line(m2sX(a.x),m2sY(a.y),m2sX(b.x),m2sY(b.y));
        //draw arrow
        pushMatrix();
          //translate(m2sX(a.x),m2sY(a.y));
          translate(m2sX((b.x-a.x)/2+a.x),m2sY((b.y-a.y)/2+a.y));
          textSize(12);
          fill(50);
          if(label) text(str(a.id) + "-" + str(b.id),0,5);
          rotate(atan2((b.y-a.y),(b.x-a.x))-PI/2);
          if(arrow)triangle(0,0,-4,-4,4,-4);
          if(showFlow)text(str(round(sfc.speed)),10,0);
        popMatrix();
      popStyle();
   }
}

class SpeedFlowCurve{
   String name;
   int index;
   float freeSpeed;
   float capSpeed;
   float speed;
   float time;
   float genCost;
   float flow;
   float capacity;
   int lanes;
   float power;
   
   SpeedFlowCurve(String _name,int _index, float ff, float capS, float cap, float n, int l){
     name=_name;
     index=_index;
     freeSpeed=ff;
     capSpeed=capS;
     capacity=cap;
     power=n;
     flow=0;
     lanes=l;
     speed = updateSpeed();
   }
   
   public float updateSpeed(){
    speed = 1/((1/freeSpeed)+(1/capSpeed-1/freeSpeed)*pow(flow/capacity,power));
    return speed;
   }
   
   public String toString(){
      return name+" "+speed; 
   }
}
//fills the arrays with starting values
public void initalizeArray(float[][] thisArray, float value){

  
     for(int i=0;i<thisArray.length;i++){
       for(int j=0;j<thisArray[i].length;j++){
         thisArray[i][j]=value;
       }
     }
}

public void initalizeArray(int[][] thisArray, int value){
  //initiate the arrays with starting values
  
     for(int i=0;i<thisArray.length;i++){
       for(int j=0;j<thisArray[i].length;j++){
         thisArray[i][j]=value;
       }
     }
}

//swaps state, and sends message
public Boolean toggle(Boolean value,String on, String off){
     if (value==true) {
     value=false; 
     println(off);
    } else {
     value=true; 
     println(on);
    } 
    return value;
}
// get distance of a point from a line defined by two points p1 and p2
public float distanceFromLine(PVector p1, PVector p2, PVector point){
        float p12=dist2(p1,p2);
        if(p12==0){
          return dist2(point,p1);
        } else {
          float t=((point.x-p1.x)*(p2.x-p1.x)+(point.y-p1.y)*(p2.y-p1.y))/p12;
          println(t+"/"+p12);
          t = max(0.0f,min(1.0f,t));
          println(t);
          return dist2(point,new PVector(p1.x+t*(p2.x-p1.x),p1.y+t*(p2.y-p1.y)));
        }
}

public float dist2(PVector v,PVector w){
   return pow(v.x-w.x,2)+pow(v.y-w.y,2); 
}

//get closest point on link, not working? maybe infinite line
public PVector closestPointOnLine(PVector p1, PVector p2, PVector point){
    float a = p2.y-p1.y; //interceptXFromPoints(p1,p2);
    float b = p2.x-p1.x; //interceptYFromPoints(p1,p2);
    float c = p2.x*p1.y-p2.y*p1.x; //a*b;
    float x = (b*(b*point.x-a*point.y)-a*c)/(pow(a,2)+pow(b,2));
    float y = (a*(-b*point.x+a*point.y)-b*c)/(pow(a,2)+pow(b,2));
    return new PVector(x,y);
}

public float gradientFromPoints(PVector p1, PVector p2){
    float gradient = (p2.y-p1.y)/(p2.x-p1.x);
    return gradient;
}

public float interceptYFromPoints(PVector p1, PVector p2){
   float intercept=p2.y-p2.x*gradientFromPoints(p1,p2);
   return intercept;
}

public float interceptXFromPoints(PVector p1, PVector p2){
   float intercept=-interceptYFromPoints(p1,p2)/gradientFromPoints(p1,p2);
   return intercept;
}
class Visualiser {
  ArrayList<Entity> Entities;
  Network network;
  
  Visualiser(Network _network){
    Entities = new ArrayList<Entity>();
    network= _network;
  }
  
  public void runGenerator(){
    
    int s = network.tripMatrix.length;
    for(int i=0;i<s;i++){
      for(int j=0;j<s;j++){
          if(random(0,1)*360 < network.tripMatrix[i][j] && i != j && network.pathfinder.indexMatrix[i][j]!=-1&&network.pathfinder.getPath(i,j)[0]!=-1){
            Entity temp = new Entity(network,i,j);
            if(!temp.finished){
              Entities.add(temp);
            }
          }
      }
    }
  }
  
  public void updateEntities(){
    for(int i = Entities.size() -1;i>=0;i--){
        Entity temp = Entities.get(i);
        //println("Entity "+ i + " "+temp);
        temp.run();
        if(temp.finished){Entities.remove(i);}
    }
  }
  
  public void display(){
    for(Entity e : Entities){
       e.display(); 
    }
  }
  
}
//uses Floyd-Warshall
class PathFinder{
  float[][] distanceMatrix;
  int[][] indexMatrix;
  int arraySize;
  
  PathFinder(Links edges, int _arraySize){
     arraySize = _arraySize;
     indexMatrix = new int[arraySize][arraySize];
     distanceMatrix = new float[arraySize][arraySize];
     
     //initiate the arrays with starting values
     //for(int i=0;i<arraySize;i++){
       //for(int j=0;j<arraySize;j++){
         initalizeArray(indexMatrix,-1);
         initalizeArray(distanceMatrix,MAX_FLOAT);
       //}
     //}
     
     for (Link edge : edges.links){
       distanceMatrix[edge.a.id][edge.b.id]=edge.genCost;
       indexMatrix[edge.a.id][edge.b.id]=edge.b.id;
     }
   }
   
  public void runFinder(){
    for(int k=0;k<arraySize;k++){
       for(int i=0;i<arraySize;i++){
          for(int j=0;j<arraySize;j++){
              if (distanceMatrix[i][k]+distanceMatrix[k][j]<distanceMatrix[i][j]) {
                distanceMatrix[i][j]=distanceMatrix[i][k]+distanceMatrix[k][j];
                indexMatrix[i][j]=indexMatrix[i][k];
              }
          }
       }
    }
  }
  
  public int[] getPath(int u, int v) {
     int[] path= new int[0];
     if (indexMatrix[u][v] != -1) {
       path=append(path,u);
       while (u != v) {
           u = indexMatrix[u][v];
           path = append(path,u);
       }
     } else{
      path=append(path,-1); 
     }
      return path;
    }
}
  public void settings() {  size(750,750); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "HighwaymanV0_3" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
