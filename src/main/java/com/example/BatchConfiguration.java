package com.example;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    //reader
    @Bean
    public FlatFileItemReader<Person> reader(){
       return new FlatFileItemReaderBuilder<Person>()
               .name("personItemReader")
               .resource(new ClassPathResource("sample-data.csv"))
               .delimited()
               .names("firstName", "lastName")
               .targetType(Person.class)
               .build();
    }
    //processor
    @Bean
    public PersonItemProcessor processor(){
        return new PersonItemProcessor();
    }
    //writer
    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource){
        return new JdbcBatchItemWriterBuilder<Person>()
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .beanMapped()
                .build();

    }

    //step1
    @Bean
    public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
                      FlatFileItemReader<Person> reader, PersonItemProcessor processor, JdbcBatchItemWriter<Person> writer){
        return new StepBuilder("step1",jobRepository)
                .<Person,Person>chunk(3,transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    //job
    @Bean
    public Job job(JobRepository jobRepository,Step step1,JobCompletionNotificationListener listener){
        return new JobBuilder("importUserJob",jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }


}
