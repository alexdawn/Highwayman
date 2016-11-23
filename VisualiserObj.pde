class Visualiser {
  ArrayList<Entity> Entities;
  Network network;
  
  Visualiser(Network _network){
    Entities = new ArrayList<Entity>();
    network= _network;
  }
  
  void runGenerator(){
    
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
  
  void updateEntities(){
    for(int i = Entities.size() -1;i>=0;i--){
        Entity temp = Entities.get(i);
        //println("Entity "+ i + " "+temp);
        temp.run();
        if(temp.finished){Entities.remove(i);}
    }
  }
  
  void display(){
    for(Entity e : Entities){
       e.display(); 
    }
  }
  
}