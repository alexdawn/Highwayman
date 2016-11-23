boolean up=false;
boolean down=false;
boolean left=false;
boolean right=false;
boolean zoomIn=false;
boolean zoomOut=false;
boolean twoWay=false;
int sfcSelection=1;

void keyPressed(){
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

void keyReleased(){
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