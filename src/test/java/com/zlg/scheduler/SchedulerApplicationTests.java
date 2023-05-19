package com.zlg.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchedulerApplicationTests {

    @Test
    void contextLoads() {
        int deviceNumber = 1253;
        int executeTotal = 5;
        int excess = deviceNumber % executeTotal;
        int bucket = (deviceNumber - excess) / executeTotal;

        //excess: 3 bucket: 250
        System.out.println("excess: "+ excess+" bucket: "+bucket);




    }

}
