package tudors.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import tudors.administratives.InputAddress;

@Controller
public class InputController {
    @GetMapping("/")
    public ModelAndView getInputs() {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("inputAddress", new InputAddress());
        return modelAndView;
    }
}
