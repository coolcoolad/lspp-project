package generator2;

import java.util.TimerTask;

/**
 * Created by yangjie on 2017/1/3.
 */
public class TimeFlowTask extends TimerTask {
    private DataGenerator generator = null;

    public TimeFlowTask(DataGenerator generator){ this.generator = generator; }
    @Override
    public void run(){
        generator.timeFlowAndRepaint();
    }
}
