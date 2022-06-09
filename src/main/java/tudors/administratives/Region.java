package tudors.administratives;

import lombok.Data;

/**
 * Region abstract representation having a name,
 * to be extended by all classes representing regions
 * (country, state, city).
 *
 * @author Pricop Tudor-Constatin 2A2
 * @author Tudose Tudor-Cristian 2A2
 */
@Data
public abstract class Region implements Comparable<String>{
    protected String name;

    @Override
    public int compareTo(String o) {
        return this.getName().compareTo(o);
    }
}
