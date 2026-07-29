
package com.example.Spectacle_phase1.Model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne   //Each cart item belongs to one user, but a user can have many cart items.
    @JoinColumn(name = "user_id")   //Creates a column user_id in the cart table that stores the foreign key to User.id
    private User user;    //A Java reference to the User entity.

    @ManyToOne   //Each cart item references one product, but a product can appear in many carts.
    @JoinColumn(name = "product_id")  //Creates a column product_id in the cart table that stores the foreign key to product.id
    private Product product;  //Java reference to the product entity.

    private Integer quantity = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
