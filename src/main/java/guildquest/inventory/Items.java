package guildquest.inventory;

/**
 * Legacy item model — ported from Python (Assignment X).
 *
 * <p>Original Python class stored name, type, and description with
 * setters for name and description. This Java port preserves that
 * mutable design exactly.</p>
 */
public class Items {

    private String name;
    private String type;
    private String description;

    public Items(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Items(String name) {
        this(name, "", "");
    }

    public String getName()        { return name; }
    public String getType()        { return type; }
    public String getDescription() { return description; }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }
}
