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
  
  void updateTotals(){
    updateRowTotal();
    updateColumnTotal();
  }
  
  float[] updateRowTotal(){
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
  float[] updateColumnTotal(){
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
  void mult(){
    
  }
  
  void div(){
    
  }
  
  void add(){
    
  }
  
  void sub(){
    
  }
  
  void avg(){
    
  }
  
  void weightedAvg(){
    
  }
  
  //method of successive averages
  void msa(){
    
  }
  
  //adds a small values to zero elements
  void seed(){
    
  }
  
  //returns transposed matrix
  void transpose(){
    
  }
  
  void readSATURN(){
    
  }
  
  void writeSATURN(){
    
  }
  
  void readTUBA(int version){
    
  }
  
  void writeTUBA(int version){
    
  }
  
  void readEMME(int version){
    
  }
  
  void writeEMME(int version){
    
  }
  
  void furness(float targetError){
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