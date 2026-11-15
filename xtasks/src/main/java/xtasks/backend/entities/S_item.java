package xtasks.backend.entities;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javax.persistence.*;

@Entity
@Table(name="suggestions")
public class S_item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long suggestionId;

    @ManyToOne
    @JoinColumn(name = "catalogue_id")
    private G_item catalogueItem;

    @Column(name = "reason", nullable = false)
    String reason;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "is_reject", nullable = false)
    private Boolean isReject = false;


    public S_item(G_item catalogueItem, int quantity, String reason) {
        this.catalogueItem = catalogueItem;
        this.quantity = quantity;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "S_item{" +
                "id=" + suggestionId +
                ", quantity0='" + quantity + '\'' +
                ", G_item =" + catalogueItem.toString() + '\'' +
                ", reason =" + reason +
                '}';
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public S_item() {
        super();
    }


    public void setId(Long id) {
        this.suggestionId = id;
    }

    public Long getId() {
        return suggestionId;
    }

    public void setCatalogueItem(G_item item) {
        this.catalogueItem = item;
    }

    public G_item getCatalogueItem() {
        return catalogueItem;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public Boolean getIsReject() {
        return isReject;
    }

    public void setIsReject(Boolean isReject) {
        this.isReject = isReject;
    }
}
