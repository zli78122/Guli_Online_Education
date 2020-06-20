package com.atguigu.canal;

import com.atguigu.canal.client.CanalClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

@SpringBootApplication
public class CanalApplication implements CommandLineRunner {

    @Resource
    private CanalClient canalClient;

    @Override
    public void run(String... strings) throws Exception {
        canalClient.run();
    }

    public static void main(String[] args) {
        SpringApplication.run(CanalApplication.class, args);
    }
}
