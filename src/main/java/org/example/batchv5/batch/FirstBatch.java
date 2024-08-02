package org.example.batchv5.batch;

import org.example.batchv5.entity.AfterEntity;
import org.example.batchv5.entity.BeforeEntity;
import org.example.batchv5.repository.AfterEntityRepository;
import org.example.batchv5.repository.BeforeEntityRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class FirstBatch {

    private final JobRepository jobRepository; // 스프링배치쪽에서 알아서 정의한 클레스임
    private final PlatformTransactionManager platformTransactionManager; //스프링배치쪽에서 알아서 정의한 클레스임
    private final BeforeEntityRepository beforeRepository;
    private final AfterEntityRepository afterRepository;

    public FirstBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, BeforeEntityRepository beforeRepository, AfterEntityRepository afterRepository) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.beforeRepository = beforeRepository;
        this.afterRepository = afterRepository;
    }

    @Bean
    public Job firstJob() {

        System.out.println("first job");

        return new JobBuilder("firstJob", jobRepository)
                .start("firstStep") //첫 스텝들어갈자리
                //.next() // 그 다음 스텝
                //.next()
                .build();
    }
    @Bean
    public Step firstStep() {

        System.out.println("first step");

        return new StepBuilder("firstStep", jobRepository)
                // <reader에 쓰일 엔티티(BeforeEntity), writer에 쓰일 엔티티(AfterEntity)>
                .<BeforeEntity, AfterEntity> chunk(10, platformTransactionManager)  // 청크 => 10개씩 가져와서 처리한다. 한번에 처리할 단위, -> 너무 적게하면 I/O가 많아지고, 너무 크게하면 리소스와 실패시 리스크가 큼
                .reader(beforeReader()) //읽는메소드자리
                .processor(middleProcessor()) // 처리메소드자리
                .writer(afterWriter()) //쓰기메소드자리
                .build();
    }

    @Bean
    public RepositoryItemReader<BeforeEntity> beforeReader() { //RepositoryItemReader => JPA 기반 타입이다.  JDBC 등등 많은 타입들이 있다.

        return new RepositoryItemReaderBuilder<BeforeEntity>() // read
                .name("beforeReader") // reader에 대한 이름 정의
                .pageSize(10) // findAll 말고 10개씩 가져오도록 -> chunk 와 같게
                .methodName("findAll")  // JPA 메소드 쿼리를 날릴수 있다.(findAll 을 해도 청크 단위로 가져옴)
                .repository(beforeRepository)  // jpa repository
                .sorts(Map.of("id", Sort.Direction.ASC)) // 10개씩 가져올때, 순서가 겹쳐질수도 있음, 따라서 정렬을 해서 가져오도록 하는게 좋다. 그리고 설정 안하면 청크 단위마다 정렬을 하거나 체크를 하기 때문에 리소스 낭비가 있음
                                                            // 이 기준으로 10개씩 가져옴
                .build();
    }

    @Bean
    public ItemProcessor<BeforeEntity, AfterEntity> middleProcessor() { // 읽어온 데이터를 처리하는 과정인데, 생략해도 가능, 단순 이동같은 경우 필요없음

        return new ItemProcessor<BeforeEntity, AfterEntity>() {

            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {

                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUsername(item.getUsername());

                return afterEntity;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<AfterEntity> afterWriter() {  // RepositoryItemWriter 여러 구현체와 다양한 인터페이스가 있다.

        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }
}
