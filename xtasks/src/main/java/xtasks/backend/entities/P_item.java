package xtasks.backend.entities;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.persistence.*;


@Entity
@Table(name="past_purchases_list")
public class P_item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @CsvBindByName(column = "name")
    @Column(name="name")
    private String name;
    @CsvCustomBindByName(column = "date",converter = LocalDateConverter.class)
    @Column(name="date")
    private LocalDate addDate;
    @CsvBindByName(column = "quantity")
    @Column(name="quantity")
    private int quantity;
    @CsvBindByName(column = "unit")
    @Column(name="unit")
    private String unit;
    @CsvBindByName(column = "category")
    @Column(name="category",nullable = false)
    private String category;





    //@Column(name="listId")
    //private long listId;

    public P_item(String name, String unit, String category, int quantity, LocalDate addDate) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.quantity = quantity;
        this.addDate = addDate;
    }
 //   public P_item(String name, String unit, String category, int quantity, LocalDate addDate ,G_item catalogueItem) {
 //       this.name = name;
 //       this.category = category;
 //       this.unit = unit;
 //       this.quantity = quantity;
 //       this.addDate = addDate;
 //       this.catalogueItem = catalogueItem;
 //   }


    @Override
    public String toString() {
        return "G_item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", category=" + category +
                ", quantity='" + quantity + '\'' +
                ", addDate='" + addDate + '\'' +

//                ", listId='" + listId + '\'' +
                '}';
    }
    public P_item(){super();}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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

    public Integer getQuantity(){
        return quantity;
    }

    public void setQuantity (Integer quantity){ this.quantity = quantity; }

    public LocalDate getAddDate(){return addDate;}

    public void setAddDate(LocalDate addDate){ this.addDate = addDate; }
    public static class LocalDateConverter extends AbstractBeanField<P_item,LocalDate> {
        @Override
        protected Object convert(String value) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(value, formatter);
        }
    }
//    public void setCatalogueItem(G_item item) {
//        this.catalogueItem = item;
//    }
//
//    public G_item getCatalogueItem() {
//        return catalogueItem;
//    }

//    public void setlistId(Long listId) {
//        this.listId = listId;
//    }
//
//    public Long getlistId() {
//        return listId;
//    }

}
