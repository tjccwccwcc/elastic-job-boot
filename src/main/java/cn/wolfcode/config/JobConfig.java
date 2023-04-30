package cn.wolfcode.config;

import cn.wolfcode.job.FileCustomElasticJob;
import cn.wolfcode.job.FileDateflowJob;
import cn.wolfcode.job.MyElasticJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;


@Configuration
public class JobConfig {
    @Bean
    //注册中心配置
    public CoordinatorRegistryCenter registryCenter
            (@Value("${zookeeper.url}") String url,@Value("${zookeeper.groupName}") String groupName) {
        ZookeeperConfiguration zookeeperConfiguration =
                new ZookeeperConfiguration(url, groupName);
        // 设置节点超时时间
        zookeeperConfiguration.setSessionTimeoutMilliseconds(100);
        // ZookeeperConfiguration("zookeeper地址", "项目名")
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        regCenter.init();
        return regCenter;
    }


    //定时任务类配置
    public LiteJobConfiguration createJobConfiguration
    (Class clazz, String cron, int shardingCout, String shardingParam, boolean isDataFlowJob) {
        JobCoreConfiguration.Builder jobBuilder =
                JobCoreConfiguration.newBuilder(clazz.getSimpleName(), cron, shardingCout);
        if (!StringUtils.isEmpty(shardingParam)) jobBuilder.shardingItemParameters(shardingParam);
        // 定义作业核⼼配置 newBuilder("任务名称", "cron表达式", "分片数量")
        JobCoreConfiguration simpleCoreConfig = jobBuilder.build();
        // 定义SIMPLE类型配置 cn.wolfcode.myElasticJob 一
        JobTypeConfiguration jobConfiguration;
        if (isDataFlowJob){
            jobConfiguration = new DataflowJobConfiguration
                    (simpleCoreConfig, clazz.getCanonicalName(), true);
        }
        else {
            jobConfiguration = new SimpleJobConfiguration
                    (simpleCoreConfig, clazz.getCanonicalName());
        }
        // 定义Lite作业根配置 .overwrite(true)允许覆盖配置，"0/10 * * * * ?"才能生效 二
        return LiteJobConfiguration.newBuilder(jobConfiguration).overwrite(true).build();
    }


    // 运维管理
    @Autowired
    private DataSource dataSource;


    //测试的调度
/*    @Bean(initMethod = "init")
    public SpringJobScheduler testScheduler(MyElasticJob job, CoordinatorRegistryCenter registryCenter){
        LiteJobConfiguration jobConfiguration =
                createJobConfiguration(job.getClass(), "0/5 * * * * ?", 1, null, false);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
    }*/
/*    @Bean(initMethod = "init")
    // fileScheduler不要命名为 fileCustomElasticJob，因为bean将方法名作为id，
    // 而FileCustomElasticJob中的bean读入类开头小写作为id，造成id冲突
    public SpringJobScheduler fileScheduler
            (FileCustomElasticJob job, CoordinatorRegistryCenter registryCenter){
        LiteJobConfiguration jobConfiguration =
                createJobConfiguration(job.getClass(), "0 0/1 * * * ?",
                        4, "0=text,1=image,2=radio,3=vedio", false);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
    }*/
    @Bean(initMethod = "init")
    public SpringJobScheduler fileDataFlowScheduler
            (FileDateflowJob job, CoordinatorRegistryCenter registryCenter){
        LiteJobConfiguration jobConfiguration =
                createJobConfiguration(job.getClass(), "0 0/1 * * * ?",
                        1, null, true);
        // 配置会在任务执行的时间将任务执行的情况存储到数据源中
        JobEventConfiguration jobEventConfiguration = new JobEventRdbConfiguration(dataSource);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration, jobEventConfiguration);
    }
}

// 定义SIMPLE类型配置 cn.wolfcode.myElasticJob 一
/*        SimpleJobConfiguration simpleJobConfig =
                new SimpleJobConfiguration(simpleCoreConfig, clazz.getCanonicalName());*/
// 定义Lite作业根配置 .overwrite(true)允许覆盖配置，"0/10 * * * * ?"才能生效 二
/*        return LiteJobConfiguration.newBuilder(simpleJobConfig).overwrite(true).build();*/
