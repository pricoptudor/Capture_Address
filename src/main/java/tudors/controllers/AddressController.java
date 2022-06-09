package tudors.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tudors.dtos.AddressResponse;
import tudors.services.AddressService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pricop Tudor-Constantin 2A2
 * @author Tudose Tudor-Cristian 2A2
 * <p>
 * Application entry-point:
 * - has a service that computes a result for the given address input;
 * - logs request time metrics;
 * - returns a JSON-formatted list of Address DTO-s (country name, state name, city name) for the most probable results;
 */
@Log
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
        List<AddressResponse> responses = new ArrayList<>();
        long stamp1 = System.currentTimeMillis();
        if (postalCode != null) {
            responses = addressService.findSolutionByPostalCode(postalCode);
        }
        log.info("Time for postal code search: " + (System.currentTimeMillis() - stamp1));

        stamp1 = System.currentTimeMillis();
        responses.addAll(addressService.findSolutionByName(Arrays.asList(country, state, city, postalCode, streetLine)));
        log.info("Time for search everything: " + (System.currentTimeMillis() - stamp1));

        return responses;
    }
}
