package tudors.solver;

import lombok.extern.java.Log;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.geonames.*;
import org.springframework.stereotype.Component;
import tudors.administratives.Country;
import tudors.administratives.Region;
import tudors.administratives.ScoreRegion;
import tudors.administratives.State;
import tudors.dtos.AddressResponse;
import tudors.tools.CSVReader;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @author Pricop Tudor-Constantin 2A2
 * @author Tudose Tudor-Cristian 2A2
 * <p>
 * Solver service that has the resulted found regions as field(reset after every request).
 * Also a Thread Pool is instantiated at program start-up so the requests are light.
 * A LevenshteinDistance object is also instantiated to ease requests' load.
 */
@Log
@Component
public class Solver {

    private List<ScoreRegion> scoreRegions = new ArrayList<>();

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(20);
    private final LevenshteinDistance levDist = new LevenshteinDistance();

    /**
     * Calls GeoNames webservice to find a location from the given postal code.
     */
    public List<AddressResponse> searchPostalCode(String postalCode, Integer score) throws Exception {
        List<AddressResponse> found = new ArrayList<>();

        WebService.setUserName("tudor007"); // add your username here

        ToponymSearchCriteria toponymSearchCriteria = new ToponymSearchCriteria();
        toponymSearchCriteria.setStyle(Style.FULL);
        toponymSearchCriteria.setFeatureClass(FeatureClass.P);

        PostalCodeSearchCriteria searchCriteria = new PostalCodeSearchCriteria();
        searchCriteria.setStyle(Style.FULL);
        searchCriteria.setPostalCode(postalCode);

        WebService.postalCodeSearch(searchCriteria);
        List<PostalCode> searchResult = WebService.postalCodeSearch(searchCriteria);

        for (PostalCode code : searchResult) {
            //System.out.println(code.getPlaceName()+" : "+code.getCountryCode()+" : "+code.getAdminCode1());

            toponymSearchCriteria.setAdminCode1(code.getAdminCode1());
            toponymSearchCriteria.setCountryCode(code.getCountryCode());
            toponymSearchCriteria.setNameEquals(code.getPlaceName());

            ToponymSearchResult toponyms = WebService.search(toponymSearchCriteria);
            for (Toponym toponym : toponyms.getToponyms()) {
                found.add(new AddressResponse(toponym.getCountryName(), toponym.getAdminName1(), toponym.getName()));
                //System.out.println(toponym.getName()+" : "+toponym.getAdminName1()+" : "+toponym.getCountryName());
            }
        }
        return found;
    }

    /**
     * Checks whether the found object is already in the list of found regions and adds it if the answer is false
     * or the found score is greater(that address is more suitable as an answer).
     */
    private void addOrReplace(ScoreRegion currentRegion) {
        boolean found = false;
        for (ScoreRegion region : scoreRegions) {
            if (currentRegion.getRegion().equals(region.getRegion())) {
                if (currentRegion.getScore() > region.getScore()) {
                    region.setScore(currentRegion.getScore());
                }
                found = true;
            }
        }
        if (!found) {
            scoreRegions.add(currentRegion);
        }
    }

    /**
     * Search in parallel for a given input address in a list of regions(with a distance for levenshtein and a score
     * for results filtering).
     * The resulted list is filtered to have unique objects with the greatest found score and is returned sorted.
     */
    public List<ScoreRegion> findRegions(String input, List<? extends Region> list, int distance, int score) {
        scoreRegions = new ArrayList<>();
        List<ScoreRegion> leetRegions = null;
        List<ScoreRegion> formattedRegions = null;
        try {
            leetRegions = forkJoinPool.submit(
                    () -> findFormattedRegions(leetFormattedInput(input).toLowerCase(), list, distance, score)).get();
            formattedRegions = forkJoinPool.submit(
                    () -> findFormattedRegions(input.toLowerCase(), list, distance, score)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        assert leetRegions != null;
        leetRegions.forEach(this::addOrReplace);

        assert formattedRegions != null;
        formattedRegions.forEach(this::addOrReplace);

        scoreRegions.sort(Comparator.comparing(ScoreRegion::getScore).reversed());
        return scoreRegions;
    }

    /**
     * Search for the formatted input in a list of regions with a given levenshtein(misspelling) distance and a score.
     * First the search is done binary, with the greatest score, then the region is searched by tokenizing the input and
     * lastly the input is checked entirely(misspelling included).
     * The search is stopped after the first step that returns a non-empty list and the time required to search the lists
     * is logged on the screen.
     */
    private List<ScoreRegion> findFormattedRegions(String input, List<? extends Region> list, int distance, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();

        long stamp1 = System.currentTimeMillis();
        foundRegions.addAll(findBinaryRegions(input, list, score * 10));
        long stamp2 = System.currentTimeMillis();
        if (foundRegions.isEmpty()) {
            foundRegions.addAll(findTokenizedRegions(input, list, distance, score * 4));
        }
        long stamp3 = System.currentTimeMillis();
        if (foundRegions.isEmpty()) {
            foundRegions.addAll(findEditDistanceRegions(input, list, distance, score * 2));
        }
        long stamp4 = System.currentTimeMillis();

        log.info("binary search: " + (stamp2 - stamp1));
        log.info("tokenize search: " + (stamp3 - stamp2));
        log.info("levenshtein search: " + (stamp4 - stamp3));

        return foundRegions;
    }

    /**
     * Search in the given region list for every token found in the input string(separated by ',./ '), with misspellings
     * included(levenshtein distance).
     */
    private List<ScoreRegion> findTokenizedRegions(String input, List<? extends Region> list, int distance, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(input, ",.-/ ");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            foundRegions.addAll(findEditDistanceRegions(name, list, distance, score));
        }
        return foundRegions;
    }

    /**
     * In earlier versions this function searched for every input contained within the input string.
     * The results were not relevant(ex: 'i','ee' as cities) so the function will be removed in future versions.
     */
    @Deprecated
    private List<ScoreRegion> findContainedRegions(String input, List<? extends Region> list, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        for (Region region : list) {
            if (input.contains(region.getName())) {
                foundRegions.add(new ScoreRegion(region, score));
            }
        }
        return foundRegions;
    }

    /**
     * Search for an input region name in a given list of regions, misspelling included(edit distance) and the lowest score.
     * The search is done using parallel stream on the collection of regions and a pool of threads(thread initialization
     * is quite expensive).
     */
    private List<ScoreRegion> findEditDistanceRegions(String input, List<? extends Region> list, int distance, int score) {
        List<ScoreRegion> foundRegions;
        try {
            foundRegions = forkJoinPool.submit(() ->
                    list.parallelStream()
                            .filter(region -> levDist.apply(region.getName(), input) <= distance)
                            .map(region -> new ScoreRegion(region, score / (levDist.apply(region.getName(), input) + 1)))
                            .collect(Collectors.toList())
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            return new ArrayList<>();
        }
//        List<ScoreRegion> foundRegions = list.parallelStream()
//                .filter(region -> levDist.apply(region.getName(),input) <= distance)
//                .map(region -> new ScoreRegion(region,levDist.apply(region.getName(),input)))
//                .collect(Collectors.toList());
//        int currentDistance;
//        for (Region region : list) {
//            currentDistance = levDist.apply(region.getName(), input);
//            if (currentDistance <= distance) {
//                foundRegions.add(new ScoreRegion(region, score / (currentDistance + 1)));
//            }
//        }
        return foundRegions;
    }

    /**
     * Search for an input region name in a given list of regions in binary method(the list was previously sorted).
     * It returns all regions with the same name as the given input.
     */
    private List<ScoreRegion> findBinaryRegions(String input, List<? extends Region> list, int score) {
        List<ScoreRegion> foundRegions = new ArrayList<>();
        int startIndex = Collections.binarySearch(list, input);
        if (startIndex < 0) return foundRegions;
        int listSize = list.size();
        for (int i = startIndex - 1; i >= 0 && list.get(i).getName().equals(input); i--) {
            foundRegions.add(new ScoreRegion(list.get(i), score));
        }
        for (int i = startIndex; i < listSize && list.get(i).getName().equals(input); i++) {
            foundRegions.add(new ScoreRegion(list.get(i), score));
        }
        return foundRegions;
    }

    /**
     * Formats a given string input with the leet alphabet(hackers alphabet).
     */
    private String leetFormattedInput(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '0' -> builder.append('o');
                case '1' -> builder.append('i');
                case '2' -> builder.append('r');
                case '3' -> builder.append('e');
                case '4' -> builder.append('a');
                case '5' -> builder.append('s');
                case '6', '8' -> builder.append('b');
                case '7' -> builder.append('t');
                case '9' -> builder.append('g');
                default -> builder.append(input.charAt(i));
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Solver solver = new Solver();
        CSVReader csvReader = new CSVReader();

        List<ScoreRegion> countries = solver.findRegions("Romania", csvReader.getGlobe().getCountries(), 1, 10);
        List<ScoreRegion> states = new ArrayList<>();
        List<ScoreRegion> cities = new ArrayList<>();

        if (countries.size() > 0) {
            for (ScoreRegion country : countries) {
                solver.findRegions("Bacau", ((Country) country.getRegion()).getBaseCountry().getStateList(), 1, 10);
                states.addAll(solver.findRegions("Bacau", ((Country) country.getRegion()).getBaseCountry().getStateList(), 1, 10));
            }

            if (states.size() > 0) {
                for (ScoreRegion state : states) {
                    solver.findRegions("Onesti", ((State) state.getRegion()).getBaseState().getCityList(), 1, 10)
                            .forEach(System.out::println);
                }
            }

            cities.forEach(System.out::println);
            System.out.println("--------------------------------------");

            solver.findRegions("Onesti", csvReader.getGlobe().getCities(), 1, 10).forEach(System.out::println);
        }
    }
}
