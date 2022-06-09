package tudors.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import tudors.dtos.InputAddress;

/**
 * @author Pricop Tudor-Constantin 2A2
 * @author Tudose Tudor-Cristian 2A2
 * <p>
 * Empty mapping returns an HTML view.
 * The form's submit is linked with the controller that has a solver service, as parameters is given an Input Address object;
 */
@Controller
public class InputController {
    @GetMapping("/")
    public ModelAndView getInputs() {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("inputAddress", new InputAddress());
        return modelAndView;
    }
}
