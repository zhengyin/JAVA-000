public class Test4{
    public static void main(String[] args){
        java.util.stream.IntStream.range(0,5)
                .forEach(System.out::println);
    }
}