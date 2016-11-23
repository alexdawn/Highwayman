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
   
  void runFinder(){
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
  
  int[] getPath(int u, int v) {
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