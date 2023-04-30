package cn.wolfcode.job;

import cn.wolfcode.domain.FileCustom;
import cn.wolfcode.mapper.FileCustomMapper;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FileCustomElasticJob implements SimpleJob {
    @Autowired
    private FileCustomMapper fileCustomMapper;
    @Override
    public void execute(ShardingContext shardingContext) {
        long threadId = Thread.currentThread().getId();
        log.info("线程ID:{},任务的名称:{},任务的参数:{},分片的个数:{},分片索引号:{},分片的参数:{}",
                threadId,
                shardingContext.getJobName(),
                shardingContext.getJobParameter(),
                shardingContext.getShardingTotalCount(),
                shardingContext.getShardingItem(),
                shardingContext.getShardingParameter()
        );
        doWork(shardingContext.getShardingParameter());
    }

    private void doWork(String shardingParameter) {
        // 查询出所有的备份任务
        List<FileCustom> fileCustoms = fileCustomMapper.selectByType(shardingParameter);
        for(FileCustom custom:fileCustoms){
            backUp(custom);
        }
    }

    private void backUp(FileCustom custom) {
        System.out.println("备份的方法名：" + custom.getName()
                + "备份的类型：" + custom.getType());
        System.out.println("-----------------------------");
        // 模拟备份操作
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileCustomMapper.changeState(custom.getId(), 1);
    }
}
