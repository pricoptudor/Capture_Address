package tudors.administratives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Scores class contains constant score lists
 * for all region types, according to the input fields
 * where the tokens have been found.
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
public class Scores {
    public static final List<Integer> COUNTRY_SCORES = new ArrayList<>(Arrays.asList(100, 10, 10, 10, 10));
    public static final List<Integer> STATE_SCORES = new ArrayList<>(Arrays.asList(10, 100, 10, 10, 10));
    public static final List<Integer> CITY_SCORES = new ArrayList<>(Arrays.asList(10, 10, 100, 10, 10));
}
