package ma.nexotek.HireCraft.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
public class HealthCheck {

    @GetMapping("/check")
    public String check(){
        return "ok";
    }
    @GetMapping("/public")
    public String publicCheck(){
        return "public";
    }
}
