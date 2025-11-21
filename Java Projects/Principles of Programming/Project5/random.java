import java.util.*;
import java.util.Random;


class Main {
    public static void main(String[] args) {
        Random random = new Random();
        List<String> songs = new ArrayList<>();
        songs.add("a");
        songs.add("b");
        songs.add("c");
        List<String> shuffled = new ArrayList<>();
        int i = 0;
        while(i < songs.size()){
            int r = random.nextInt(songs.size());
            shuffled.add(songs.remove(r));
            i++;       
        }
        for(String j: shuffled){
            System.out.print(j);
        }
        
    }
}