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
     sfcs.add(new SpeedFlowCurve("Motorway",sfcs.size(), 112/30, 72, 6000, 2.7, 3)); //112
     sfcs.add(new SpeedFlowCurve("Dual Carridgeway",sfcs.size(), 104/30, 64, 4000, 2.75, 2)); //104
     sfcs.add(new SpeedFlowCurve("Nation Speed",sfcs.size(), 96/30, 50, 2000, 3, 1)); //96
     sfcs.add(new SpeedFlowCurve("30 Zone",sfcs.size(), 48/30, 25, 1500, 3, 1)); //48
   }
   
   void display(){
      for(Node n : networkNodes.nodes){
         n.display(labelNodes,showTripEnds); 
      }
      for(Link l: networkLinks.links){
         l.display(labelLinks,showArrows,showFlow); 
      }      
   }
   
   void updateTripMatrices(){
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
   
   void assignAllNothing(){
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
   
   void updatePaths(){
      pathfinder = new PathFinder(networkLinks,networkNodes.nodes.size());
      pathfinder.runFinder();
   }
}

class Nodes{
  ArrayList <Node> nodes; 
  
  Nodes(){
    nodes = new ArrayList<Node>();
  }
  Node add(float X,float Y){
    int id = nodes.size();
    Node newNode = new Node(X,Y,id);
    nodes.add(newNode);
    return newNode;
  }
  
  //also need to update ids
  void remove(Node node){
    for(int i=nodes.size()-1;i>node.id;i--){
      Node temp=nodes.get(i);
      temp.id--;
    }
    nodes.remove(node);
  }
  
  Node get(int id){
    return nodes.get(id);
  }
  
  //get nearest node in Cartesian distance
  Node getNearest(float x,float y){
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
  
  Link add(Node a,Node b,SpeedFlowCurve sfc){
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
  
  void removeLinksOn(Node node){
      for(int i=links.size()-1;i>=0;i--){
        Link temp = links.get(i);
        if(temp.a == node || temp.b == node){
          links.remove(i);
        }
      }
  }
  
  boolean linkExists(Node a, Node b) {
   boolean result=false;
   for (Link checkLink : links){
      if(checkLink.a == a && checkLink.b == b){
         result = true; 
      }
   }
   return result;
  }
  
  Object getNearest(PVector position){
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
  Object get(Node a, Node b) {
   Object result= null; //new Link(a,b,-1,new SpeedFlowCurve("",0,0,0,0,0,0));
   for (Link checkLink : links){
      if(checkLink.a == a && checkLink.b == b){
         result = checkLink; 
      }
   }
   return result;
  }
  
  Link get(int id){
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
   
   void display(Boolean label,Boolean showTripEnds){
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
   
   void updateSFC(){
    distance = sqrt(pow(b.x - a.x,2)+pow(b.y - a.y,2));
    time = distance/sfc.updateSpeed();
    genCost = distance + time;
    speed = distance/time;
   }
   
   void display(Boolean label, Boolean arrow, Boolean showFlow){
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
   
   float updateSpeed(){
    speed = 1/((1/freeSpeed)+(1/capSpeed-1/freeSpeed)*pow(flow/capacity,power));
    return speed;
   }
   
   String toString(){
      return name+" "+speed; 
   }
}