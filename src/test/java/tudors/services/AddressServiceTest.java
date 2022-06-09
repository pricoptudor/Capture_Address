package tudors.services;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressServiceTest {
    private final Gson gson = new Gson();
    private final RestTemplate restTemplate = new RestTemplate();
    private String uri = "https://address-capture.herokuapp.com/addresses";

    @Test
    void testSolution() {
        assertEquals(findSolutionByName("Romania", "", "Onesti", "", ""), "{country=Romania, state=Bacau, city=Onesti}");
        assertEquals(findSolutionByName("United States", "Florida", "Tamarac", "", ""), "{country=United States, state=Florida, city=Tamarac}");
        assertEquals(findSolutionByName("United States", "Kansas", "Moundridge", "", ""), "{country=United States, state=Kansas, city=Moundridge}");
        assertEquals(findSolutionByName("United States", "New Jersey", "Belvidere", "", ""), "{country=United States, state=New Jersey, city=Belvidere}");
        assertEquals(findSolutionByName("United States", "New Jersey", "Belvidere", "", ""), "{country=United States, state=New Jersey, city=Belvidere}");
        assertEquals(findSolutionByName("Romania", "Targu Mures", "MureŞ", "", ""), "{country=Romania, state=Mures, city=Targu-mures}");
        assertEquals(findSolutionByName("Romania", "Bacau", "Casin", "", ""), "{country=Romania, state=Bacau, city=Casin}");
        assertEquals(findSolutionByName("Pakistan", "Punjab", "Sialkot", "", ""), "{country=Pakistan, state=Punjab, city=Sialkot}");
        assertEquals(findSolutionByName("", "", "", "601132", ""), "{country=Romania, state=Bacău, city=Oneşti}");
        assertEquals(findSolutionByName("", "Chihuahua", "", "", "Centro"), "{country=Mexico, state=Chihuahua, city=Chihuahua}");
        assertEquals(findSolutionByName("", "", "", "Mumbai", "Maharashtra"), "{country=India, state=Maharashtra, city=Mumbai}");
        assertEquals(findSolutionByName("Архангельск", "", "", "", ""), "{country=Russia, state=Arkhangelskaya, city=Arkhangel'sk}");
        assertEquals(findSolutionByName("", "Каменск-Шахтинский", "", "Russia", ""), "{country=Russia, state=Rostov, city=Kamensk-shakhtinskiy}");
        assertEquals(findSolutionByName("", "", "", "", "Paphos"), "{country=Cyprus, state=Pafos, city=Paphos}");
        assertEquals(findSolutionByName("", "辽宁省", "沈阳市-皇姑区", "", ""), "{country=China, state=Liaoning, city=Shenyang}");
    }

    String findSolutionByName(String country, String state, String city, String postalCode, String streetLine) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String urlTemplate = UriComponentsBuilder.fromHttpUrl(uri)
                .queryParam("country", "{country}")
                .queryParam("state", "{state}")
                .queryParam("city", "{city}")
                .queryParam("postalCode", "{postalCode}")
                .queryParam("streetLine", "{streetLine}")
                .encode()
                .toUriString();

        Map<String, String> params = new HashMap<>();
        params.put("country", country);
        params.put("state", state);
        params.put("city", city);
        params.put("postalCode", postalCode);
        params.put("streetLine", streetLine);

        HttpEntity<String> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                String.class,
                params
        );

        List<?> list = gson.fromJson(response.getBody(), List.class);
        return list.get(0).toString();
//        for(var scoreRegion:list){
//            //System.out.println(gson.fromJson(scoreRegion.toString(),ScoreRegion.class));
//            System.out.println(scoreRegion.toString());
//        }
    }
}