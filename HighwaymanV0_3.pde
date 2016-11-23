//Master loop, and global variables

Network network = new Network();
Visualiser vis =new Visualiser(network);

PVector oldMouse = new PVector();
PVector pan = new PVector(0,0);

float zoom=1;
float scrollSpeed=5;
float zoomSpeed=0.1;
float snap = 30; //square of the number of pixels drawing a link will snap to
boolean networkUpdate; //logs if changes have been made to the network
boolean paused;
long runtime;
long lastCheck;
int mode=2; //1 select drag, 2 add, 3 pan?


void setup() {
  size(750,750);
  //fullScreen(P2D); //P2D
  
}

void draw() {
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

void updateNetwork(){
     network.updatePaths();
     network.updateTripMatrices();
     network.assignAllNothing();
     vis =new Visualiser(network);
     networkUpdate=false;
}

//when mouse pressed store coordinates
void mousePressed(){
  oldMouse.x = s2mX(mouseX);
  oldMouse.y = s2mY(mouseY);
}

void mouseReleased(){
   //if mouse has click dragged
   if (mouseButton==LEFT) {
     addParts();
   }
   if (mouseButton==RIGHT){
     //moveNode(); 
   } //<>//
}

void mouseClicked(){
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

void moveNode(){
  if(mouseX!=m2sX(oldMouse.x) || mouseY!=m2sY(oldMouse.y)){
    
  }
}

void addParts(){
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
     network.networkLinks.add(a,b,network.sfcs.get(sfcSelection-1)); //<>//
     //draw both directions
     if(twoWay==true){
       network.networkLinks.add(b,a,network.sfcs.get(sfcSelection-1));
     }
   }
   networkUpdate=true; 
}