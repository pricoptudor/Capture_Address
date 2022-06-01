package tudors.administratives;

import lombok.Data;

@Data
public abstract class Region implements Comparable<String>{
    protected String name;

    @Override
    public int compareTo(String o) {
        return this.getName().compareTo(o);
    }
}
