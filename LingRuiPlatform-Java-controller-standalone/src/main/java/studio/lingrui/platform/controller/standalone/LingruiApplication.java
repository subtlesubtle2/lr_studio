package studio.lingrui.platform.controller.standalone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("studio.lingrui.platform.common.dao.**.mapper")
@EnableTransactionManagement //开启注解方式的事务管理
public class LingruiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LingruiApplication.class, args);
    }
}
