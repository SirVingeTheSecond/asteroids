package dk.sdu.mmmi.cbse.score;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@CrossOrigin
public class ScoreService {

    private int score = 0;

    public static void main(String[] args) {
        SpringApplication.run(ScoreService.class, args);
    }

    @GetMapping("/score/get")
    public int getScore() {
        return this.score;
    }

    @PutMapping("/score/add/{score}")
    public int addToScore(@PathVariable(value = "score") int score) {
        this.score += score;
        return this.score;
    }

    @PutMapping("/score/set/{score}")
    public int updateScore(@PathVariable(value = "score") int score) {
        this.score = score;
        return this.score;
    }
}