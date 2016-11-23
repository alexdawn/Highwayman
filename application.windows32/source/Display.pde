float s2mX(float x){
  return x*zoom+pan.x;
}

float s2mY(float y){
  return y*zoom+pan.y;
}

float m2sX(float x){
  return (x-pan.x)/zoom;
}

float m2sY(float y){
  return (y-pan.y)/zoom;
}

void drawRunInfo(){
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