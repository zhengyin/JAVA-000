public class Test3{
    public static void main(String[] args){
        Ponit a = new Ponit(1,1);
        Ponit b = new Ponit(5,3);
        int c = a.area(b);
    }

    static class Ponit {
        int x,y;
        Ponit(int x, int y){
            this.x = x;
            this.y = y;
        }

        public int area(Ponit b){
            int length = Math.abs(b.y - this.y);
            int width = Math.abs(b.x - this.x);
            return length * width;
        }
    }
}