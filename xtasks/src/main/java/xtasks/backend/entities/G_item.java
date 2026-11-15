package xtasks.backend.entities;

import javafx.beans.property.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="items")
public class G_item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="name", nullable = false)
    String name;
    @Column(name="unit", nullable = false)
    String unit;
    @Column(name="category", nullable = false)
    String category;
    @Column(name="image", nullable = false)
    String image;
    @Transient
    private IntegerProperty quantity = new SimpleIntegerProperty(1);
    @OneToOne(mappedBy = "catalogueItem", orphanRemoval = true)
    L_item items ;

    @OneToMany(mappedBy = "catalogueItem", orphanRemoval = true)
    List<S_item> sitem ;

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }


    public L_item getItems() {
        return items;
    }

    public void setItems(L_item items) {
        this.items = items;
    }

    public List<S_item> getSitem() {
        return sitem;
    }

    public void setSitem(List<S_item> items) {
        this.sitem = items;
    }

    public G_item(String unit, String category, String image, String name) {
        this.name = name;
        this.image = image;
        this.category = category;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "G_item{" +
                "id=" + id +
                ", name='" + name +
                ", unit='" + unit +
                ", category=" + category +
                ", image='" + image +
                '}';
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public G_item(){super();}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public final Integer getQuantity(){
        return quantity.get();
    }

    public final Integer getDefaultQuantity() {
        Map<String, Integer> defaultQuantities = new HashMap<>();
        defaultQuantities.put("Piece", 1);
        defaultQuantities.put("Gram", 1000);
        defaultQuantities.put("Milliliter", 1000);
        defaultQuantities.put("milliliter", 1000);
        defaultQuantities.put("Liter", 1);
        defaultQuantities.put("gram", 1000);

        // Retrieve the default quantity based on the unit, or return 0 for unknown units. TODO: Java DOC.
        return defaultQuantities.getOrDefault(unit, 1);
    }

    public final void setQuantity(IntegerProperty quantity){
        this.quantity.set(quantity.get());
    }

    public IntegerProperty quantityProperty(){
        return quantity;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return true;
        if (getClass() != obj.getClass()) return false;

        G_item other = (G_item) obj;

        if (id == null) {
            return other.id == null;
        } else if (!id.equals(other.id)) {
            return false;
        }

        if (unit == null) {
            return other.unit == null;
        } else if (!unit.equals(other.unit)) {
            return false;
        }

        if (category == null) {
            return other.category == null;
        } else if (!category.equals(other.category)) {
            return false;
        }

        if (name == null) {
            return other.name == null;
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (image == null) {
            return other.image == null;
        } else {
            return image.equals(other.image);
        }

    }
}
