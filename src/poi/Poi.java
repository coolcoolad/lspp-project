package poi;

import java.io.Serializable;

/**
 * Created by yangjie on 2017/1/9.
 */
public class Poi implements Comparable<Poi>, Serializable {
    public String name = "";
    public double locX = 0;
    public double locY = 0;

    public Poi(String name, double x, double y){
        this.name = name;
        this.locX = x;
        this.locY = y;
    }

    @Override
    public int compareTo(Poi o) {
        if(this.locX == o.locX && this.locY == o.locY)
            return 0;
        return 1;
    }
}
