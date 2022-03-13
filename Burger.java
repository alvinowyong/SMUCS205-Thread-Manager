/**
 * The constructor {@link #Burger(id, machine)} which create a Burger 🍔 object
 * 
 * @param id  The unique identifier of Burger 🍔 object
 * @param machine  The id of Burger 🍔 Producer which produced the Burger object
 *
 */
public class Burger {
    int id;
    String burgMach;

    public Burger(int id, String burgMach) {
        this.id = id;
        this.burgMach = burgMach;
    }

    @Override
    public String toString() {
        return "Burger [id=" + id + "]";
    }
}