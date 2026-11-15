package xtasks.backend.entities;

import javax.persistence.*;

@Entity
@Table(name="grocery_list")
public class L_item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="list_id")
    private Long listItemId;

    @OneToOne
    @JoinColumn(name = "catalogue_id")
    private G_item catalogueItem;


    @Column(name="quantity")
    int quantity;


    public L_item(G_item catalogueItem, int quantity) {
        this.catalogueItem = catalogueItem;
        this.quantity = quantity;
    }
    @Override
    public String toString() {
        return "L_item{" +
                "id=" + listItemId +
                ", quantity='" + quantity + '\'' +
                ", G_item =" + catalogueItem.toString() +
                '}';
    }

    public int getQuantity() { return quantity;}

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public L_item() {super();}


    public void setId(Long id) {
        this.listItemId = id;
    }

    public Long getId() {
        return listItemId;
    }

    public void setCatalogueItem(G_item item) {
        this.catalogueItem = item;
    }

    public G_item getCatalogueItem() {
        return catalogueItem;
    }



}
