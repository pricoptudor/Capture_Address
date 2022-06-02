package tudors.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tudors.administratives.ScoreRegion;
import tudors.model.AddressResponse;
import tudors.services.AddressService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin
@Tag(name = "Address Controller")
public class AddressController {
    private AddressService addressService;

    @Autowired
    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/addresses")
    public List<AddressResponse> getAddresses(@RequestParam(defaultValue = "") String country,
                                              @RequestParam(defaultValue = "") String state,
                                              @RequestParam(defaultValue = "") String city,
                                              @RequestParam(defaultValue = "") String postalCode,
                                              @RequestParam(defaultValue = "") String streetLine) {
        List<AddressResponse> responses = addressService.findSolutionByPostalCode(postalCode);
        responses.addAll(addressService.findSolutionByName(Arrays.asList(country, state, city, postalCode, streetLine)));
        return responses;
    }
}
