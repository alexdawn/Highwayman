//fills the arrays with starting values
void initalizeArray(float[][] thisArray, float value){

  
     for(int i=0;i<thisArray.length;i++){
       for(int j=0;j<thisArray[i].length;j++){
         thisArray[i][j]=value;
       }
     }
}

void initalizeArray(int[][] thisArray, int value){
  //initiate the arrays with starting values
  
     for(int i=0;i<thisArray.length;i++){
       for(int j=0;j<thisArray[i].length;j++){
         thisArray[i][j]=value;
       }
     }
}

//swaps state, and sends message
Boolean toggle(Boolean value,String on, String off){
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
float distanceFromLine(PVector p1, PVector p2, PVector point){
        float p12=dist2(p1,p2);
        if(p12==0){
          return dist2(point,p1);
        } else {
          float t=((point.x-p1.x)*(p2.x-p1.x)+(point.y-p1.y)*(p2.y-p1.y))/p12;
          println(t+"/"+p12);
          t = max(0.0,min(1.0,t));
          println(t);
          return dist2(point,new PVector(p1.x+t*(p2.x-p1.x),p1.y+t*(p2.y-p1.y)));
        }
}

float dist2(PVector v,PVector w){
   return pow(v.x-w.x,2)+pow(v.y-w.y,2); 
}

//get closest point on link, not working? maybe infinite line
PVector closestPointOnLine(PVector p1, PVector p2, PVector point){
    float a = p2.y-p1.y; //interceptXFromPoints(p1,p2);
    float b = p2.x-p1.x; //interceptYFromPoints(p1,p2);
    float c = p2.x*p1.y-p2.y*p1.x; //a*b;
    float x = (b*(b*point.x-a*point.y)-a*c)/(pow(a,2)+pow(b,2));
    float y = (a*(-b*point.x+a*point.y)-b*c)/(pow(a,2)+pow(b,2));
    return new PVector(x,y);
}

float gradientFromPoints(PVector p1, PVector p2){
    float gradient = (p2.y-p1.y)/(p2.x-p1.x);
    return gradient;
}

float interceptYFromPoints(PVector p1, PVector p2){
   float intercept=p2.y-p2.x*gradientFromPoints(p1,p2);
   return intercept;
}

float interceptXFromPoints(PVector p1, PVector p2){
   float intercept=-interceptYFromPoints(p1,p2)/gradientFromPoints(p1,p2);
   return intercept;
}