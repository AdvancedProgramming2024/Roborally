package dk.dtu.compute.se.pisd.roborally;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.System.out;

@SpringBootApplication
public class SpringStart {
    public static void main(String[] args) {

        SpringApplication.run(SpringStart.class, args);
        out.print("Executed successfully");
    }

}