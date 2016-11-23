//Class for moving objects

class Entity{
  Network network;
  PVector position;
  PVector velocity;
  int routeIndex;
  int[] route; //list of node ids to travel through
  color paint;
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
  
  void run(){
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
  
  void display(){
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
  
  String toString(){
    return route[routeIndex]+" "+position.x+" "+position.y;
  }
}