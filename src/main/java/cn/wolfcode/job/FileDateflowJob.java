package cn.wolfcode.job;

import cn.wolfcode.domain.FileCustom;
import cn.wolfcode.mapper.FileCustomMapper;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class FileDateflowJob implements DataflowJob<FileCustom> {
    @Autowired
    private FileCustomMapper fileCustomMapper;
    //抓取数据
    @Override
    public List<FileCustom> fetchData(ShardingContext shardingContext) {
        System.out.println("开始抓取数据.........");
        List<FileCustom> fileCustoms = fileCustomMapper.selectLimit(2);
        return fileCustoms;
    }
    //处理数据
    @Override
    public void processData(ShardingContext shardingContext, List<FileCustom> data) {
        for(FileCustom custom:data){
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
