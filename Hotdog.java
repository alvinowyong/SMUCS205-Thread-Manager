/**
 * The constructor {@link #Hotdog(id, machine)} which create a Hotdog 🌭 object
 * 
 * @param id  The unique identifier of Hotdog 🌭 object
 * @param machine  The id of Hotdog 🌭 Producer which produced the Hotdog object
 *
 */
public class Hotdog {
    int id;
    String hotdMach;

    public Hotdog(int id, String hotdMach) {
        this.id = id;
        this.hotdMach = hotdMach;
    }

    @Override
    public String toString() {
        return "Hotdog [id=" + id + "]";
    }
}